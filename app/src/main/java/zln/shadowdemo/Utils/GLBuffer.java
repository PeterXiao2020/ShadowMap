package zln.shadowdemo.Utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

/**
 * Created by zln on 04/01/2017.
 */
public class GLBuffer {

    public final int bufferId;
    public final int bufferType;
    protected Buffer data;
    protected int length;

    public GLBuffer(int bufferId, int bufferType) {

        this.bufferId = bufferId;
        this.bufferType = bufferType;
    }

    public GLBuffer glSyncWithGPU(ByteBuffer buffer, int length, int usage){
        IntBuffer ib = buffer.asIntBuffer();

        for (int i = 0; i < 3; ++i) {
            System.out.println(ib.get(i));
        }
        this.data = buffer;
        this.length = length;
        glBindBuffer(bufferType, bufferId);
        glBufferData(bufferType, length, data, usage);
        glBindBuffer(bufferType, 0);

        return this;
    }

    static public GLBuffer glGenBuffer(int target) {
        int[] temp = new int[1];
        glGenBuffers(1, temp, 0);

        switch (target){
            case 1:
                return new GLBuffer(temp[0], GL_ARRAY_BUFFER);
            case 2:
                return new GLBuffer(temp[0], GL_ELEMENT_ARRAY_BUFFER);
            default:
                throw new RuntimeException();
        }

    }
}
