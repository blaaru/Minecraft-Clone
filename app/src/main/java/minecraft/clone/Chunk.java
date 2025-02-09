package minecraft.clone;

public class Chunk {
    // Constants for chunk size
    public static final int CHUNK_SIZE_X = 16; // width
    public static final int CHUNK_SIZE_Y = 128; // height
    public static final int CHUNK_SIZE_Z = 16; // depth

    // Terrain generator
    private final Terrain terrain;

    // 3D array for block data
    private final int[][][] blocks;

    // Location for chunk position
    private final int chunkX, chunkZ;

    // constructor
    public Chunk(int chunkX, int chunkZ, Terrain terrain) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.terrain = terrain;
        this.blocks = new int[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
        generateTerrain();
    }

    // Generate terrain using Perlin noise
    private void generateTerrain() {
        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                int worldX = chunkX * CHUNK_SIZE_X + x;
                int worldZ = chunkZ * CHUNK_SIZE_Z + z;
                int worldY = (int) terrain.generateHeight(worldX, worldZ);




                for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                    if (y <= worldY) {
                        blocks[x][y][z] = 1; // 1 = block
                    }
                }
            }
        }
    }


    // Getter for block data
    public int getBlock(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE_X && y >= 0 && y < CHUNK_SIZE_Y && z >= 0 && z < CHUNK_SIZE_Z) {
            return blocks[x][y][z];
        }
        return -1; // invalid coords
    }

    // getter for chunk sizes
    public int getChunkSizeX() {
        return CHUNK_SIZE_X;
    }

    public int getChunkSizeY() {
        return CHUNK_SIZE_Y;
    }

    public int getChunkSizeZ() {
        return CHUNK_SIZE_Z;
    }

    // Setter for block data
    public void setBlock(int x, int y, int z, int blockType) {
        if (x >= 0 && x < CHUNK_SIZE_X && y >= 0 && y < CHUNK_SIZE_Y && z >= 0 && z < CHUNK_SIZE_Z) {
            blocks[x][y][z] = blockType;
        }
    }



}
