package zln.shadowdemo.Utils;

/**
 * Created by zln on 04/01/2017.
 */
public class Vec3 {

    public final float x;
    public final float y;
    public final float z;


    public Vec3() {
        x = 0;
        y = 0;
        z = 0;
    }


    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(String[] tokens) {
        this.x = Float.parseFloat(tokens[0]);
        this.y = Float.parseFloat(tokens[1]);
        this.z = Float.parseFloat(tokens[2]);
    }

    public Vec3 normalize() {
        float length = length();
        return this.mul(1.0f / length);
    }

    public Vec3 subtract(Vec3 v) {
        return new Vec3(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vec3 mid(Vec3 v) {
        return this.add(v).mul(0.5f);
    }

    public Vec3 min(Vec3 v) {
        return new Vec3(Math.min(this.x, v.x),
                Math.min(this.y, v.y),
                Math.min(this.z, v.z));
    }

    public Vec3 max(Vec3 v) {
        return new Vec3(Math.max(this.x, v.x),
                Math.max(this.y, v.y),
                Math.max(this.z, v.z));
    }

    public Float maxComponent() {
        return Math.max(Math.max(x, y), z);
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vec3 mul(float v) {
        return new Vec3(this.x * v, this.y * v, this.z * v);
    }

    public Vec3 div(float v) {
        return new Vec3(this.x / v, this.y / v, this.z / v);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

}
