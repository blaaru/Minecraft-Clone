package minecraft.clone;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.system.MemoryUtil;

public class Skybox {
    private int vao, vbo, ebo, shaderProgram;
    private int projectionLocation, viewLocation;
    
    private static final float[] skyboxVertices = {
        -500.0f,  500.0f, -500.0f, 
        -500.0f, -500.0f, -500.0f,
         500.0f, -500.0f, -500.0f,
         500.0f,  500.0f, -500.0f,
    
        -500.0f,  500.0f,  500.0f,
        -500.0f, -500.0f,  500.0f,
         500.0f, -500.0f,  500.0f,
         500.0f,  500.0f,  500.0f,
    };

    private static final int[] skyboxIndices = {
        0, 1, 2, 2, 3, 0,  // Back face
        4, 5, 6, 6, 7, 4,  // Front face
        1, 5, 6, 6, 2, 1,  // Right face
        0, 4, 7, 7, 3, 0,  // Left face
        3, 2, 6, 6, 7, 3,  // Top face
        0, 1, 5, 5, 4, 0   // Bottom face
    };

    
        public void init() {
        
        // openGL + GLSL version debugging, don't remove
        System.out.println("OpenGL Version: " + org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VERSION));
        System.out.println("GLSL Version: " + org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION));
            
        shaderProgram = createShaderProgram();
        projectionLocation = glGetUniformLocation(shaderProgram, "projection");
        viewLocation = glGetUniformLocation(shaderProgram, "view");
    
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        // init debug, don't remove
        System.out.println("Skybox VAO: " + vao);
        System.out.println("Skybox VBO: " + vbo);
        System.out.println("Skybox EBO: " + ebo);
        System.out.println("Skybox Shader Program: " + shaderProgram);

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, skyboxIndices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);


        glBindVertexArray(0);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        Matrix4f skyboxView = new Matrix4f(view);
        skyboxView.setTranslation(0, 0, 0);

        FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
        FloatBuffer viewBuffer = MemoryUtil.memAllocFloat(16);

        projection.get(projectionBuffer);
        skyboxView.get(viewBuffer);

        glUniformMatrix4fv(projectionLocation, false, projectionBuffer);
        glUniformMatrix4fv(viewLocation, false, viewBuffer);

        // freeing memory
        MemoryUtil.memFree(projectionBuffer);
        MemoryUtil.memFree(viewBuffer);

        glDisable(GL_CULL_FACE);
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, skyboxIndices.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glEnable(GL_CULL_FACE);
        
    }

    private int createShaderProgram() {
        // vertex shader for skybox
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            out vec3 TexCoords;
            uniform mat4 projection;
            uniform mat4 view;
            void main(void) {
                mat4 skyboxView = mat4(mat3(view));
                gl_Position = projection * skyboxView * vec4(aPos, 1.0);
                TexCoords = aPos;
            }
            """);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("ERROR::SKYBOX_VERTEX_SHADER::COMPILATION_FAILURE");
            System.err.println(glGetShaderInfoLog(vertexShader));
        }
        
        // fragment shader for gradient skybox
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, """
            #version 330 core
            in vec3 TexCoords;
            out vec4 FragColor;
            void main(void) {
                // normalize and use the y component for gradient
                float t = (normalize(TexCoords).y + 1.0) / 2.0;
                // when looking up: light blue; when looking down: dark blue
                vec3 topColor = vec3(0.6, 0.8, 1.0);    // light blue
                vec3 bottomColor = vec3(0.04, 0.3, 0.55); // dark blue
                vec3 color = mix(bottomColor, topColor, t);
                FragColor = vec4(color, 1.0);
            }
            """);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("ERROR::SKYBOX_FRAGMENT_SHADER::COMPILATION_FAILURE");
            System.err.println(glGetShaderInfoLog(fragmentShader));
        }
        
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("ERROR::SKYBOX_SHADER_PROGRAM::LINKING_FAILURE");
            System.err.println(glGetProgramInfoLog(program));
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        return program;
    }

    public int getShaderProgram() {
        return shaderProgram;
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteProgram(shaderProgram);
    }
}
