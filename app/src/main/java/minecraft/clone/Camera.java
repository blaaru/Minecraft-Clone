package minecraft.clone;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;



public class Camera {
    // instance variables
    private final Vector3f position;
    private Vector3f forward;
    private Vector3f right;
    private final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private final Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

    // movment variables
    private final Movement movement;
    private final float speed = 4.317f; // regular movement speed
    private final float acceleration = 50.0f;


    private final Matrix4f viewMatrix;
    private final float standHeight = 0.0f;

    // jump variables
    private float yVelocity = 0.0f;
    private final float gravity = -32.0f;
    private boolean isJumping = false;

    // crouch variables
    private boolean isCrouching = false;
    private final float crouchAmount = 0.21f;

    // sprinting vairables
    private final float sprintAmount = 1.3f;

    // delta time varirables
    private double lastTime = GLFW.glfwGetTime();



    public Camera(int shaderProgram, Movement movement) {
        this.movement = movement;
        position = new Vector3f(0.0f, 0.0f, 3.0f);
        viewMatrix = new Matrix4f();
        initCaptureMouse();
    }

    private void initCaptureMouse() {
        System.out.println("Capturing mouse...");
        glfwSetInputMode(App.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPos(App.getWindow(), 800 / 2, 600 / 2);

        glfwSetCursorPosCallback(App.getWindow(), (window, xpos, ypos) -> {
            //System.out.println("Mouse moved: " + xpos + ", " + ypos);
            movement.updateMouse(xpos, ypos);
        });
    }

    public boolean isOnGround() {
        return position.y <= 0.0f;
    }

    public double getCurrentTime() {
        return GLFW.glfwGetTime();
    }

    public double getDelta() {
        double currentTime = getCurrentTime();
        double delta = currentTime - lastTime;
        lastTime = currentTime;
        return delta;
    }

    // jumping logic: update jump physics
    public void jumpUpdate(double deltaTime) {
        // if jump is pressed: init jump
        if (movement.isJumpTriggered() && !isJumping) {
            yVelocity = 9.81f; // adding jump velocity
            isJumping = true; // jump becomes true
        }

        // jump phsycis
        if (isJumping) {
            yVelocity += gravity * deltaTime; // applying grazvity
            position.y += yVelocity * deltaTime; // update vertical position

            // simple ground collision
            if (position.y < 0.0f) {
                position.y = 0.0f;
                yVelocity = 0.0f;
                isJumping = false;
            }
        }
    }

    public void crouchUpdate() {
        // if crouch is pressed: init crouch
        if (!isJumping) {
            isCrouching = movement.isCrouching();

            // sprinting physics
            if (isCrouching) {
                // lower the camera relative to standing height
                position.y = standHeight - crouchAmount;
            } else {
                position.y = standHeight;
            }
        }
    }



    // new version -- combining viewmatrix in update movement!
    public void update() {
        //System.out.println("Player Y: " + getPosition().y);
        double deltaTime = getDelta();

        // calculate forward vector from yaw and pitch
        float yaw = movement.getYaw();
        float pitch = movement.getPitch();
        forward = new Vector3f(
            (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
            (float) Math.sin(Math.toRadians(pitch)),
            (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
        ).normalize();
        
        // calculate right vector as cross product of forward and up
        right = new Vector3f(forward).cross(up).normalize();
        
        // move camera based on input
        Vector3f moveDir = new Vector3f();
        Vector3f horizontalForward = new Vector3f(forward.x, 0, forward.z).normalize();
        Vector3f horizontalRight = new Vector3f(right.x, 0, right.z).normalize();
        
        // simple movement math
        if (movement.isForward()) {
            moveDir.add(horizontalForward);
        } 

        if (movement.isBackward()) {
            moveDir.sub(horizontalForward);
        }

        if (movement.isLeft()) {
            moveDir.sub(horizontalRight);
        }

        if (movement.isRight()) {
            moveDir.add(horizontalRight);
        }
        
        // only update if there is movement
        // new version - replacing scalars to vectors -- adding velocity, acceleration.
        float maxSpeed = movement.isSprinting() ? speed * sprintAmount : speed;
        if (moveDir.lengthSquared() > 0) {
            Vector3f normalize = moveDir.normalize();
            velocity.add(new Vector3f(normalize).mul(acceleration * (float) deltaTime));
        } else {
            float friction = 15.0f;
            float decay = 1 - friction * (float)deltaTime;

            if (decay < 0) {
                decay = 0;
            }

            velocity.mul(decay);
        }

        // speed is max velocity (clamping velocity)
        if (velocity.length() > maxSpeed) {
            velocity.set(velocity.normalize().mul(maxSpeed));
        }

        // updating velocity with acceleration
        position.add(new Vector3f(velocity).mul((float)deltaTime));

        jumpUpdate(deltaTime);
        crouchUpdate();

        // Update view matrix
        viewMatrix.identity();
        viewMatrix.lookAt(position, new Vector3f(position).add(forward), up);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
    public Vector3f getPosition() {
        return position;
    }
}
