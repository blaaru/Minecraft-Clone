package minecraft.clone;

import org.lwjgl.glfw.GLFW;

public class Movement {
    public Camera camera;
    private boolean forward, backward, left, right, space, crouch, escape;
    private boolean sprinting = false;
    private boolean jumpTriggered = false;
    private double lastX, lastY;
    private float yaw, pitch;
    private boolean mouse = true;

    private long lastTime = 0;
    private final long jumpCooldown = 15; // ms

    public Movement() {
        lastX = 800 / 2;
        lastY = 600 / 2;
        yaw = -90.0f;
        pitch = 0.0f;

        // init camera (if removed, NullPointerException error occurs because camera cannot be null)
        camera = new Camera(0, this);
    }

    public void updateMouse(double xpos, double ypos) {
        if (mouse) {
            lastX = xpos;
            lastY = ypos;
            mouse = false;

            // init camera
            camera = new Camera(0, this);
        }
    
        double xOffset = xpos - lastX;
        double yOffset = lastY - ypos;
        lastX = xpos;
        lastY = ypos;
    
        float sensitivity = 0.1f; 
        xOffset *= sensitivity;
        yOffset *= sensitivity;
    
        yaw += xOffset;
        pitch += yOffset;
    
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }
    

    public void update() {
        // input states
        forward = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        backward = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
        left = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
        right = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
        space = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        crouch = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        escape = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;


        // sprint toggle
        sprinting = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)  == GLFW.GLFW_PRESS && forward;
        
        long currentTime = System.currentTimeMillis();
        if (space && camera.isOnGround() && (currentTime - lastTime >= jumpCooldown)) {
            jumpTriggered = true;
            lastTime = currentTime;
        }        
        
    }


    public boolean isJumpTriggered() {
        boolean triggered = jumpTriggered;
        jumpTriggered = false;
        return triggered;
    }

    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(App.getWindow(), key) == GLFW.GLFW_PRESS;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isBackward() {
        return backward;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isCrouching() {
        return crouch;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public boolean isEscapePressed() {
        return escape;
    }

}
