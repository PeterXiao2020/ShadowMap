package zln.shadowdemo.Render;
import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zln on 04/01/2017.
 */

public class ShaderProgram {

    private static final String TAG = "zln_shader";

    private Context context;
    private String vertexSource, fragmentSource;
    private int mProgram, mVertexShader, mPixelShader;


    public ShaderProgram(Context c, String vs, String fs) {
        this.context = c;
        initShader(vs, fs);
    }

    public int getProgram() {
        return mProgram;
    }

    private void initShader(String vs, String fs) {
        try {
            vertexSource = convertStreamToString(context.getAssets().open(vs));
            fragmentSource = convertStreamToString(context.getAssets().open(fs));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        if (createProgram() != 1) {
            throw new RuntimeException("Error at creating shaders");
        };

    }

    private int createProgram() {
        // Vertex shader
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (mVertexShader == 0) {
            return 0;
        }

        // pixel shader
        mPixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (mPixelShader == 0) {
            return 0;
        }

        // Create the program
        mProgram = GLES20.glCreateProgram();
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, mVertexShader);
            GLES20.glAttachShader(mProgram, mPixelShader);
            GLES20.glLinkProgram(mProgram);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link _program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
                return 0;
            }
        }
        else
            Log.d(TAG, "Could not create program");

        //GLES20.glDeleteShader(mVertexShader);
        //GLES20.glDeleteShader(mPixelShader);

        return 1;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
