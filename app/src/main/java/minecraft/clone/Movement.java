package minecraft.clone;

import org.lwjgl.glfw.GLFW;

public class Movement {
    private boolean forward, backward, left, right, space, crouch;
    private boolean wasSpacePressed = false;
    private boolean jumpTriggered = false;
    private boolean sprintToggeled = false; // curent toggle state
    private boolean wasSprintKeyPressed = false; // to toggle
    private double lastX, lastY;
    private float yaw, pitch;
    private boolean mouse = true;

    public Movement() {
        lastX = 800 / 2;
        lastY = 600 / 2;
        yaw = -90.0f;
        pitch = 0.0f;
    }

    public void updateMouse(double xpos, double ypos) {
        if (mouse) {
            lastX = xpos;
            lastY = ypos;
            mouse = false;
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
        forward = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        backward = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
        left = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
        right = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
        space = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        crouch = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        
        if (space && !wasSpacePressed) {
            jumpTriggered = true;  
        }
        wasSpacePressed = space;

        // sprint toggle
        boolean currentSprintKey = GLFW.glfwGetKey(App.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)  == GLFW.GLFW_PRESS;
        if (currentSprintKey && !wasSprintKeyPressed) {
            sprintToggeled = !sprintToggeled;
        }

        wasSprintKeyPressed = currentSprintKey;
    }

    public boolean isJumpTriggered() {
        boolean triggered = jumpTriggered;
        jumpTriggered = false;
        return triggered;
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

    public boolean isSprintingToggled() {
        return sprintToggeled;
    }

}
