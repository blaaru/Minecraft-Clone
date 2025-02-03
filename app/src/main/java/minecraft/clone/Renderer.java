package minecraft.clone;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.glDepthFunc;
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
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.system.MemoryUtil;

public class Renderer {
    private int shaderProgram;
    private int projectionLocation;
    private World world;
    private final Movement movement;
    private Camera camera;
    private Skybox skybox;
    private Matrix4f projection;

    public Renderer(Movement movement) {
        this.movement = movement;
    }

    public void init() {
        // init world shader
        shaderProgram = createShaderProgram();

        // init camera with shared movement state
        camera = new Camera(shaderProgram, movement);
        
        // init skybox 
        skybox = new Skybox();
        skybox.init();

        // init world
        world = new World();
        world.init();

        // Projection matrix - increased far plane to 1000.0 to include skybox vertices
        projection = new Matrix4f().perspective(
            (float) Math.toRadians(70.0f), // normal fov baby
            800.0f / 600.0f, // aspect ratio type shit
            0.1f,
            1000.0f // nearrrrrrrrrr farrrrrrrr wheree evvveerrrrr you areeeeeeeeeeeeeee
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
        // updating camera (updates view matrix)
        camera.update();

        // --- rendering world ---
        glUseProgram(shaderProgram);
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
        world.render();

        // --- rendering skybox ---
        glDepthFunc(GL_LEQUAL); // depth testing for skybox
        glUseProgram(skybox.getShaderProgram());

        // Render skybox using the shared projection and camera view
        skybox.render(projection, camera.getViewMatrix());
        glDepthFunc(GL_LESS); // restore depth test for world
    }

    public void cleanup() {
        world.cleanup();
        skybox.cleanup();
        glDeleteProgram(shaderProgram);
    }

    private int createShaderProgram() { // dont ask me wtf this part is this some googly moogly shit
        // vertex
        int vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, """
            #version 330 core
            layout(location = 0) in vec3 aPos;

            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;

            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """);
        glCompileShader(vertex);
        checkShaderCompile(vertex, "VERTEX");

        // gragment
        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, """
            #version 330 core
            out vec4 FragColor;
            void main() {
                FragColor = vec4(1.0, 0.5, 0.2, 1.0);
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
}
