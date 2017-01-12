package zln.shadowdemo.Utils;

import java.nio.ByteBuffer;

/**
 * Created by zln on 04/01/2017.
 */
public class Index {

    public static final int SIZE_AS_FLOAT = 3;
    public static final int SIZE_AS_BYTE = SIZE_AS_FLOAT * Constant.FLOAT_BYTE_SIZE;

    public int a;
    public int b;
    public int c;
    public int id;

    public Index() {

    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer bb = Constant.genDirectBuffer(SIZE_AS_FLOAT * Constant.FLOAT_BYTE_SIZE);
        bb.putInt(a);
        bb.putInt(b);
        bb.putInt(c);
        bb.flip();
        return bb;
    }
}
