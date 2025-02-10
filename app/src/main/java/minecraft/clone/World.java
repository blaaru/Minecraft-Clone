package minecraft.clone;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.system.MemoryUtil;

public class World {
    private int vao, vbo, ebo;
    private int indicesCount;

    private final int renderDistance = 2;

    private final int worldWidth = 100;
    private final int worldDepth = 100;

    private final Terrain terrain;
    private final Chunk[][] chunk;
    private final Frustum frustum = new Frustum();

    // texture loading
    private Texture grassTopTexture;
    private Texture grassSideTexture;
    private Texture dirtTexture;

    public World(Terrain terrain, int worldWidth, int worldDepth) {
        this.terrain = terrain;
        this.chunk = new Chunk[worldWidth][worldDepth];
    }

    public void init(float[] vertices, int[] indices) {        
        indicesCount = indices.length;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Position attribute (location = 0)
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Texture coordinate attribute (location = 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Index buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);



        grassTopTexture = new Texture("app/src/main/resources/textures/blocks/grass_top.png");
        grassSideTexture = new Texture("app/src/main/resources/textures/blocks/grass_side.png");
        dirtTexture = new Texture("app/src/main/resources/textures/blocks/dirt.png");
    }

    public void update(Vector3f playerPos) {
        int currentChunkX = (int) (playerPos.x / Chunk.CHUNK_SIZE_X);
        int currentChunkZ = (int) (playerPos.z / Chunk.CHUNK_SIZE_Z);
    
        for (int x = currentChunkX - renderDistance; x <= currentChunkX + renderDistance; x++) {
            for (int z = currentChunkZ - renderDistance; z <= currentChunkZ + renderDistance; z++) {
                if (x >= 0 && x < worldWidth && z >= 0 && z < worldDepth) {
                    if (chunk[x][z] == null) {
                        chunk[x][z] = new Chunk(x, z, terrain);
                    }
                }
            }
        }
    }

    public void render(int shaderProgram, Vector3f playerPos, Matrix4f projection, Matrix4f view) {
        glBindVertexArray(vao);
        glDisable(GL_CULL_FACE);
        int modelLocation = glGetUniformLocation(shaderProgram, "model");
        Matrix4f model = new Matrix4f();
        frustum.update(projection, view);
    
        FloatBuffer modelBuffer = MemoryUtil.memAllocFloat(16); 
    
        int currentChunkX = (int) (playerPos.x / Chunk.CHUNK_SIZE_X);
        int currentChunkZ = (int) (playerPos.z / Chunk.CHUNK_SIZE_Z);
    
        for (int cx = currentChunkX - renderDistance; cx <= currentChunkX + renderDistance; cx++) {
            for (int cz = currentChunkZ - renderDistance; cz <= currentChunkZ + renderDistance; cz++) {
                if (cx >= 0 && cx < worldWidth && cz >= 0 && cz < worldDepth) {
                    Chunk currentChunk = chunk[cx][cz];
                    if (currentChunk != null) {
                        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                            for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
                                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                                    if (currentChunk.getBlock(x, y, z) != 0) {
                                        float worldX = (cx * Chunk.CHUNK_SIZE_X) + x;
                                        float worldY = y - (Chunk.CHUNK_SIZE_Y / 5.2f);
                                        float worldZ = (cz * Chunk.CHUNK_SIZE_Z) + z;
    
                                        if (frustum.isBlockVisible(worldX, worldY, worldZ)) {
                                            model.identity().translate(worldX, worldY, worldZ);
                                            model.get(modelBuffer);
                                            glUniformMatrix4fv(modelLocation, false, modelBuffer);

                                            glActiveTexture(GL_TEXTURE0);

                                            if (grassTopTexture == null || grassSideTexture == null || dirtTexture == null) {
                                                System.out.println("Texture not initialized!");
                                                return;
                                            }
                                            

                                            for (int face = 0; face < 6; face++) {
                                                switch (face) {
                                                    case 5 -> // Top face
                                                        grassTopTexture.bind();
                                                    case 4 -> // Bottom face
                                                        dirtTexture.bind();
                                                    default -> // Side faces
                                                        grassSideTexture.bind();
                                                }
                                                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, face * 6 * Integer.BYTES);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        MemoryUtil.memFree(modelBuffer);
        glBindVertexArray(0);
    }
    
    
    
    public int getVao() { 
        return vao; 
    }

    public int getVbo() { 
        return vbo; 
    }

    public int getEbo() { 
        return ebo; 
    }
    
    public int getIndicesCount() { 
        return indicesCount; 
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }
}
