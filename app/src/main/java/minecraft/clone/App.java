// version 330 core
// @auth @blakebalbin @github
package minecraft.clone;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import org.lwjgl.system.MemoryUtil;

public class App {
    private static long window;
        private Renderer renderer;
        private Movement movement;

    
        public void run() {
            init();
            loop();
            cleanup();
        }
    
        private void init() {

            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW!");
            }
    
            // Set OpenGL version to 3.3 Core Profile
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    
            System.out.println("Window is initializing....");
            window = glfwCreateWindow(800, 600, "Minecraft Demo", MemoryUtil.NULL, MemoryUtil.NULL);
            if (window == MemoryUtil.NULL) {
                throw new RuntimeException("GLFW window creation failed: Check display settings or OpenGL drivers.");
            } else {
                System.out.println("Window is initialized!");
            }

            // Center window
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidMode.width() - 800) / 2, (vidMode.height() - 600) / 2);
    
            glfwMakeContextCurrent(window);
            GL.createCapabilities(); // Init OpenGL
    
            glEnable(GL_DEPTH_TEST); // init depth testing
            glViewport(0, 0, 800, 600);

            
            movement = new Movement();
            renderer = new Renderer(movement);
            renderer.init();
            System.out.println("Game has started!");
        }
    
        private void loop() {
            while (!glfwWindowShouldClose(window)) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                // sync the player movement with the current frames input
                movement.update();

                // render and update the camera for every frame
                renderer.getWorld().update(renderer.getCamera().getPosition());
                renderer.render();  

                glfwSwapBuffers(window);
                glfwPollEvents();
            }
        }
    
        private void cleanup() {
            renderer.cleanup();
            glfwDestroyWindow(window);
            glfwTerminate();
        }
    
        public static long getWindow() {
            return window;
    }

    public static void main(String[] args) {
        System.out.println("Starting Game.....");
        new App().run();
        System.out.println("Game has ended.");
    }
}
