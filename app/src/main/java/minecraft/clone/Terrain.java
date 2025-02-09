package minecraft.clone;

import java.util.Random;

public class Terrain {
    // instance variables
    private final float amplitude = 128.0f;
    private final float scaleCoords = 0.01f; // Controls how big the noise scale is generating per noise image
    private final int numOctaves = 4; // Number of noise layers we combine, 4 is enough to simulate natural looking terrain
    private final double persistence = 0.5f; // The factor we use to reduce the amplitude for each layer so all layers don't blast at max amp

    private final Random random = new Random();
    private final Perlin perlin;
    private final int seed;

    // constructor
    public Terrain() {
        this.seed = random.nextInt(1000000000); // seed generation!
        this.perlin = new Perlin(seed); // init perlin per seed
    }


    // generates vertical height based on x and z coordinate. returns a vertical position (baased on multiplying the amplitude by a noise value created by perlin noise)
    public float generateHeight(int x, int z) {
        double noiseValue = perlin.octavePerlin(x * scaleCoords, 0, z * scaleCoords, numOctaves, persistence);
        return (float) (noiseValue * amplitude);
    }

    // getter methods
    public int getSeed() {
        return seed;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getScaleCoords() {
        return scaleCoords;
    }

    public int getOctaves() {
        return numOctaves;
    }

    public double getPersistence() {
        return persistence;
    }
    
}
