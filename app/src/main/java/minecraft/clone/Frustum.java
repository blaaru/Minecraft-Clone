package minecraft.clone;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Frustum {
    private final Matrix4f mvpMatrix = new Matrix4f();

    public void update(Matrix4f projection, Matrix4f view) {
        mvpMatrix.identity().mul(projection).mul(view);
    }

    // Check if ANY part of the block's bounding box is visible
    public boolean isBlockVisible(float x, float y, float z) {
        // Block dimensions (assuming unit cube of size 1x1x1)
        float size = 1.0f;

        // Check all 8 corners of the bounding box
        return isPointVisible(x, y, z) ||
               isPointVisible(x + size, y, z) ||
               isPointVisible(x, y + size, z) ||
               isPointVisible(x, y, z + size) ||
               isPointVisible(x + size, y + size, z) ||
               isPointVisible(x, y + size, z + size) ||
               isPointVisible(x + size, y, z + size) ||
               isPointVisible(x + size, y + size, z + size);
    }

    private boolean isPointVisible(float x, float y, float z) {
        Vector4f pos = new Vector4f(x, y, z, 1.0f);
        mvpMatrix.transform(pos);

        return pos.x >= -pos.w && pos.x <= pos.w &&
               pos.y >= -pos.w && pos.y <= pos.w &&
               pos.z >= -pos.w && pos.z <= pos.w;
    }
}
