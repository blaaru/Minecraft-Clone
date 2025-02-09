package minecraft.clone;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.system.MemoryUtil;

public class Renderer {

    private double lastFrameTime = GLFW.glfwGetTime();

    static final float BASE_FOV = 70.0f;
    private static final float SPRINT_FOV = 95.0f;
    private static final float FOV_SPEED = 10.0f; // Adjusting FOV for a sprinting effect

    private float currentFOV = BASE_FOV; // Game starts with normal FOV and after done sprinting
    private int shaderProgram;
    private int projectionLocation;
    private World world;
    private final Movement movement;
    public Camera camera;
    private Skybox skybox;
    private Matrix4f projection;

    private final float[] vertices = {
        // Positions          // Texture Coords (u, v)
    
        // Front face
        -0.5f, -0.5f,  0.5f,   0.0f, 0.0f,
         0.5f, -0.5f,  0.5f,   1.0f, 0.0f,
         0.5f,  0.5f,  0.5f,   1.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,   0.0f, 1.0f,
    
        // Back face
        -0.5f, -0.5f, -0.5f,   1.0f, 0.0f,
         0.5f, -0.5f, -0.5f,   0.0f, 0.0f,
         0.5f,  0.5f, -0.5f,   0.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,   1.0f, 1.0f,
    
        // Left face
        -0.5f, -0.5f, -0.5f,   0.0f, 0.0f,
        -0.5f, -0.5f,  0.5f,   1.0f, 0.0f,
        -0.5f,  0.5f,  0.5f,   1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,   0.0f, 1.0f,
    
        // Right face
         0.5f, -0.5f, -0.5f,   1.0f, 0.0f,
         0.5f, -0.5f,  0.5f,   0.0f, 0.0f,
         0.5f,  0.5f,  0.5f,   0.0f, 1.0f,
         0.5f,  0.5f, -0.5f,   1.0f, 1.0f,
    
        // Bottom face
        -0.5f, -0.5f, -0.5f,   0.0f, 0.0f,
         0.5f, -0.5f, -0.5f,   1.0f, 0.0f,
         0.5f, -0.5f,  0.5f,   1.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,   0.0f, 1.0f,
    
        // Top face
        -0.5f,  0.5f, -0.5f,   0.0f, 0.0f,
         0.5f,  0.5f, -0.5f,   1.0f, 0.0f,
         0.5f,  0.5f,  0.5f,   1.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,   0.0f, 1.0f
    };
    
    
    
    private final int[] indices = {
        // Front face
        0, 1, 2, 2, 3, 0,
        // Back face
        4, 5, 6, 6, 7, 4,
        // Left face
        8, 9, 10, 10, 11, 8,
        // Right face
        12, 13, 14, 14, 15, 12,
        // Bottom face
        16, 17, 18, 18, 19, 16,
        // Top face
        20, 21, 22, 22, 23, 20
    };
    
    
    
    

    public Renderer(Movement movement) {
        this.movement = movement;
    }

    public void init() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        // init world shader
        shaderProgram = createShaderProgram();

        int textureUniform = glGetUniformLocation(shaderProgram, "texture1");
        glUseProgram(shaderProgram);
        glUniform1i(textureUniform, 0);

        // init the player camera with movement bindings
        camera = new Camera(shaderProgram, movement);
        
        // init skybox 
        skybox = new Skybox();
        skybox.init();

        // init terrain
        Terrain terrain = new Terrain();
        int worldWidth = 100;
        int worldDepth = 100;

        // init world
        world = new World(terrain, worldWidth, worldDepth);
        world.init(vertices, indices);

        // Projection matrix - increased far plane to 1000.0 to include skybox vertices
        projection = new Matrix4f().perspective(
            (float) Math.toRadians(BASE_FOV), 
            800.0f / 600.0f, 
            0.1f,
            1000.0f
        );

        // set projection uniform for the world shader
        glUseProgram(shaderProgram);
        projectionLocation = glGetUniformLocation(shaderProgram, "projection");
        FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
        projection.get(projectionBuffer);
        glUniformMatrix4fv(projectionLocation, false, projectionBuffer);
        MemoryUtil.memFree(projectionBuffer);
    }

    public void render() {
        // calculating delta time
        double currentTime = GLFW.glfwGetTime();
        double deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;

        // fov interpolation using delta time
        float targetFOV = movement.isSprinting() ? SPRINT_FOV : BASE_FOV;
        currentFOV += (targetFOV - currentFOV) * FOV_SPEED * (float) deltaTime;

        // update projection matrix
        updateProjectionMatrix();

        // re upload projection matrix to world shader
        glUseProgram(shaderProgram);
        FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
        projection.get(projectionBuffer);
        glUniformMatrix4fv(projectionLocation, false, projectionBuffer);
        MemoryUtil.memFree(projectionBuffer);

        // updating camera (updates view matrix)
        camera.update();

        // --- rendering world ---
        // update view uniform for the world shader
        int viewLocation = glGetUniformLocation(shaderProgram, "view");
        FloatBuffer viewBuffer = MemoryUtil.memAllocFloat(16);
        camera.getViewMatrix().get(viewBuffer);
        glUniformMatrix4fv(viewLocation, false, viewBuffer);
        MemoryUtil.memFree(viewBuffer);

        // **** FIX: Set model uniform to identity ****
        int modelLocation = glGetUniformLocation(shaderProgram, "model");
        Matrix4f model = new Matrix4f().identity();  // identity matrix for no transformation
        FloatBuffer modelBuffer = MemoryUtil.memAllocFloat(16);
        model.get(modelBuffer);
        glUniformMatrix4fv(modelLocation, false, modelBuffer);
        MemoryUtil.memFree(modelBuffer);
        
        // now render the world (the cube)
        world.render(shaderProgram, camera.getPosition(), projection, camera.getViewMatrix());

        // --- rendering skybox ---
        glDepthFunc(GL_LEQUAL); // depth testing for skybox
        glUseProgram(skybox.getShaderProgram());

        // Render skybox using the shared projection and camera view
        skybox.render(projection, camera.getViewMatrix());
        glDepthFunc(GL_LESS); // restore depth test for world
    }

    // updating fov baby
    public void updateProjectionMatrix() {
        projection.identity().perspective(
            (float) Math.toRadians(currentFOV),
            800.0f / 600.0f,
            0.1f,
            1000.0f
        );
    }

    public void cleanup() {
        world.cleanup();
        skybox.cleanup();
        glDeleteProgram(shaderProgram);
    }

    private int createShaderProgram() {
        // vertex
        int vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, """
            #version 330 core
            layout(location = 0) in vec3 aPos;      // Position
            layout(location = 1) in vec2 aTexCoord; // Texture Coordinates

            out vec2 TexCoord; // Pass to fragment shader

            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;

            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
                TexCoord = aTexCoord; 
            }
            """);
        glCompileShader(vertex);
        checkShaderCompile(vertex, "VERTEX");

        // fragment
        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, """
            #version 330 core
            out vec4 FragColor;


            in vec2 TexCoord; 

            uniform sampler2D texture1; 

            void main() {
                FragColor = texture(texture1, TexCoord); 
            }
            """);
        glCompileShader(fragment);
        checkShaderCompile(fragment, "FRAGMENT");

        // Shader Program
        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        return program;
    }

    private void checkShaderCompile(int shader, String type) {
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            System.err.println("ERROR::SHADER::" + type + "::COMPILATION_FAILED\n" + log);
        }
    }

    public World getWorld() {
        return world;
    }

    public Camera getCamera() {
        return camera;
    }

}
