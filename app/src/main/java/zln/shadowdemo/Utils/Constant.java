package zln.shadowdemo.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by zln on 04/01/2017.
 */

public class Constant {

    //size or offset
    public static final int FLOAT_BYTE_SIZE = 4;
    public static final int POINT_SIZE = 32;
    public static final int POINT_SIZE_POS_OFFSET = 0;
    public static final int POINT_SIZE_NOR_OFFSET = 16;

    //shader filename
    public static final String SHADOW_MAP_VERTEX = "vertex.glsl";
    public static final String SHADOW_MAP_FRAGMENT = "fragment.glsl";
    public static final String DEPTH_MAP_VERTEX = "depth_vs.glsl";
    public static final String DEPTH_MAP_FRAGMENT= "depth_fs.glsl";

    //shader variable name
    public static final String U_MV_MATRIX = "uMVMatrix";
    public static final String U_MVP_MATRIX = "uMVPMatrix";
    public static final String U_SHADOW_PROJ_MATRIX = "uShadowProjMatrix";
    public static final String A_POSITION = "aPosition";
    public static final String A_NORMAL = "aNormal";
    public static final String U_NORMAL_MATRIX = "uNormalMatrix";
    public static final String U_TEXTURE = "uTexture";
    public static final String U_SHADOW_TEXTURE = "uShadowTexture";
    public static final String U_DIFFUSE = "uDiffuse";
    public static final String U_SPECULAR = "uSpecular";
    public static final String U_LIGHT_POS = "uLightPos";

    //generate buffer
    public static ByteBuffer genDirectBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
}
