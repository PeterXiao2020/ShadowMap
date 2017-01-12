package zln.shadowdemo.Utils;

import java.nio.ByteBuffer;

/**
 * Created by zln on 04/01/2017.
 */

public class Point {

    private Vec3 position;
    private Vec3 normal;
    private Vec2 texture;
    public static final int SIZE_AS_FLOAT = 8;
    public static final int SIZE_AS_BYTE = SIZE_AS_FLOAT * Constant.FLOAT_BYTE_SIZE;


    public Point() {

    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Vec3 getNormal() {
        return normal;
    }

    public void setNormal(Vec3 normal) {
        this.normal = normal;
    }

    public Vec2 getTexture() {
        return texture;
    }

    public void setTexture(Vec2 texture) {
        this.texture = texture;
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer bb = Constant.genDirectBuffer(SIZE_AS_BYTE);
        bb.putFloat(position.x);
        bb.putFloat(position.y);
        bb.putFloat(position.z);
        bb.putFloat(texture.x);
        bb.putFloat(normal.x);
        bb.putFloat(normal.y);
        bb.putFloat(normal.z);
        bb.putFloat(texture.y);
        bb.flip();
        return bb;
    }
}
