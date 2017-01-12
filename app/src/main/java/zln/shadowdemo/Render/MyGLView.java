package zln.shadowdemo.Render;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import zln.shadowdemo.Utils.Constant;
import zln.shadowdemo.Utils.GLBuffer;
import zln.shadowdemo.Utils.Vec2;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFlush;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setRotateM;

/**
 * Created by zln on 04/01/2017.
 */
public class MyGLView extends GLSurfaceView implements GLSurfaceView.Renderer{

    //all kinds of matrix
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mLightViewMatrix = new float[16];
    private final float[] mLightProjectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    //light position
    private final float[] lightPosWorld = new float[]{5.0f, 5.0f, 10.0f, 1.0f};
    private final float[] lightPosView = new float[4];

    //model data
    private LoadModel obj = ReadObjAndMtl("zuozi.obj", "zuozi.mtl");

    //buffer
    private GLBuffer vertexBuffer;
    private GLBuffer indexBuffer;

    //shader program
    private int activeProgram;
    private ShaderProgram depthMapProgram;
    private ShaderProgram shadowMapProgram;

    //screen info
    private int displayWidth;
    private int displayHeight;
    private float aspect;

    //all kinds of buffer id
    private int[] fboId;
    private int[] depthTextureId;
    private int[] renderTextureId;
    private List<Integer> textureId = new ArrayList<>();

    //shader program handles
    private int m_mvMatrixUniform;
    private int m_mvpMatrixUniform;
    private int m_shadowProjMatrixUniform;
    private int m_positionAttribute;
    private int m_normalAttribute;
    private int m_textureUniform;
    private int m_shadowTextureUniform;
    private int m_diffuseUniform;
    private int m_specularUniform;
    private int m_lightPosUniform;
    private int d_mvpMatrixUniform;
    private int d_positionAttribute;


    public MyGLView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setIdentityM(modelMatrix, 0);

    }


    public float[] getM(){
        return modelMatrix;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //init settings
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDepthFunc(GL_LEQUAL);

        //init view matrix
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0f, 0f, -1f, 0.0f, 1.0f, 0.0f);

        //init model buffers
        glInitModelBuffers();
        glInitModelTextures();

        //String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        //Log.v("ZLN_SHADER", extensions);

        depthMapProgram = new ShaderProgram(getContext(), Constant.DEPTH_MAP_VERTEX, Constant.DEPTH_MAP_FRAGMENT);
        shadowMapProgram = new ShaderProgram(getContext(), Constant.SHADOW_MAP_VERTEX, Constant.SHADOW_MAP_FRAGMENT);
        activeProgram = shadowMapProgram.getProgram();
    }

    private void glInitModelTextures() {
        int n = obj.getNum();
        int[] textureIds = new int[n];
        glGenTextures(n, textureIds, 0);
        for(int i = 0; i < n; i ++) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
            textureId.add(textureIds[i]);
            //Log.v("zln-tex: ", obj.getTexFilenames().get(i));
            try {
                GLUtils.texImage2D(GL_TEXTURE_2D, 0, BitmapFactory.decodeStream(getContext().getAssets().open(obj.getTexFilenames().get(i))), 0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        }
    }

    private void glInitModelBuffers() {
        ByteBuffer vertexData = obj.getVertexes();
        vertexBuffer = GLBuffer.glGenBuffer(1).glSyncWithGPU(vertexData, vertexData.limit(), GL_STATIC_DRAW);

        ByteBuffer indexData = obj.getIndices();
        indexBuffer = GLBuffer.glGenBuffer(2).glSyncWithGPU(indexData, indexData.limit(), GL_STATIC_DRAW);

    }

    private final static float TAN_22_5 = 0.40402622583516f;
    private final static float NEAR = 1.0f;
    private final static float FAR = 100.0f;

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        displayWidth = w;
        displayHeight = h;
        glViewport(0, 0, displayWidth, displayHeight);

        //init frame buffer
        initFBO();

        aspect = (float) displayWidth / displayHeight;
        float left = -aspect * TAN_22_5;
        float right = aspect * TAN_22_5;
        float bottom = -TAN_22_5;
        float top = TAN_22_5;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, NEAR, FAR);
        Matrix.frustumM(mLightProjectionMatrix, 0, left, right, bottom, top, NEAR, FAR);

        //Matrix.frustumM(mProjectionMatrix, 0, -aspect, aspect, -1, 1, NEAR, 100);

    }

    private void initFBO() {

        fboId = new int[1];
        depthTextureId = new int[1];
        renderTextureId = new int[1];

        glGenFramebuffers(1, fboId, 0);

        glGenRenderbuffers(1, depthTextureId, 0);
        glBindBuffer(GL_RENDERBUFFER, depthTextureId[0]);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, displayWidth, displayHeight);

        glGenTextures(1, renderTextureId, 0);
        glBindTexture(GL_TEXTURE_2D, renderTextureId[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindFramebuffer(GL_FRAMEBUFFER, fboId[0]);

        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, displayWidth, displayHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, renderTextureId[0], 0);

        int FBOStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(FBOStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        }

    }



    @Override
    public void onDrawFrame(GL10 gl10) {

        //---------bind handles----------

        //shadow map handles
        m_mvMatrixUniform = glGetUniformLocation(activeProgram, Constant.U_MV_MATRIX);
        m_mvpMatrixUniform = glGetUniformLocation(activeProgram, Constant.U_MVP_MATRIX);
        m_shadowProjMatrixUniform = glGetUniformLocation(activeProgram, Constant.U_SHADOW_PROJ_MATRIX);
        m_textureUniform = glGetUniformLocation(activeProgram, Constant.U_TEXTURE);
        m_shadowTextureUniform = glGetUniformLocation(activeProgram, Constant.U_SHADOW_TEXTURE);
        m_diffuseUniform = glGetUniformLocation(activeProgram, Constant.U_DIFFUSE);
        m_specularUniform = glGetUniformLocation(activeProgram, Constant.U_SPECULAR);
        m_lightPosUniform = glGetUniformLocation(activeProgram, Constant.U_LIGHT_POS);
        m_positionAttribute = glGetAttribLocation(activeProgram, Constant.A_POSITION);
        m_normalAttribute = glGetAttribLocation(activeProgram, Constant.A_NORMAL);

        //depth map handles
        int depthProgram = depthMapProgram.getProgram();
        d_mvpMatrixUniform = glGetUniformLocation(depthProgram, Constant.U_MVP_MATRIX);
        d_positionAttribute = glGetAttribLocation(depthProgram, Constant.A_POSITION);

        //load view matrix
        Matrix.setLookAtM(mLightViewMatrix, 0, lightPosWorld[0], lightPosWorld[1], lightPosWorld[2], 0.0f, 0.0f, 0.0f, -5.0f, 5.0f, 0.0f);

        //---------render shadow map---------

        GLES20.glCullFace(GLES20.GL_FRONT);

        renderDepthMap();

        //---------render model---------

        GLES20.glCullFace(GLES20.GL_BACK);

        renderModelAndShadow();


    }

    private void renderModelAndShadow() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        //bind vertex and index buffer
        glUseProgram(activeProgram);
        GLES20.glViewport(0, 0, displayWidth, displayHeight);

        //load light pos
        Matrix.multiplyMV(lightPosView, 0, mViewMatrix, 0, lightPosWorld, 0);
        GLES20.glUniform3f(m_lightPosUniform, lightPosView[0], lightPosView[1], lightPosView[2]);

        //load shadow projection mat
        float bias[] = new float [] {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f};

        float[] depthBiasMVP = new float[16];
        float[] light_MVMatrix = new float[16];
        float[] light_MVPMatrix = new float[16];

        multiplyMM(light_MVMatrix, 0, mLightViewMatrix, 0, getModelMatrix(), 0);
        multiplyMM(light_MVPMatrix, 0, mLightProjectionMatrix, 0, light_MVMatrix, 0);
        Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, light_MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(m_shadowProjMatrixUniform, 1, false, depthBiasMVP, 0);

        //load matrix
        float[] MVPMatrix = new float[16];
        float[] MVMatrix = new float[16];
        multiplyMM(MVMatrix, 0, mViewMatrix, 0, getModelMatrix(), 0);
        multiplyMM(MVPMatrix, 0, mProjectionMatrix, 0, MVMatrix, 0);
        glUniformMatrix4fv(glGetUniformLocation(activeProgram, "uMVMatrix"), 1, false, MVMatrix, 0);
        glUniformMatrix4fv(glGetUniformLocation(activeProgram, "uMVPMatrix"), 1, false, MVPMatrix, 0);

        //bind depth texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
        GLES20.glUniform1i(m_shadowTextureUniform, 0);

        //load model data
        glVertexAttribPointer(glGetAttribLocation(activeProgram, "aPosition"), 4, GL_FLOAT,
                false, Constant.POINT_SIZE, Constant.POINT_SIZE_POS_OFFSET);
        glVertexAttribPointer(glGetAttribLocation(activeProgram, "aNormal"), 4, GL_FLOAT,
                false, Constant.POINT_SIZE, Constant.POINT_SIZE_NOR_OFFSET);

        glEnableVertexAttribArray(glGetAttribLocation(activeProgram, "aPosition"));
        glEnableVertexAttribArray(glGetAttribLocation(activeProgram, "aNormal"));
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);

        //render model
        for(int i = 0; i < obj.getNum(); i++) {
            glUniform3f(glGetUniformLocation(activeProgram, "uDiffuse"), obj.getDiffuses().get(i).x,
                    obj.getDiffuses().get(i).y, obj.getDiffuses().get(i).z);
            glUniform3f(glGetUniformLocation(activeProgram, "uSpecular"), obj.getSpeculars().get(i).x,
                    obj.getSpeculars().get(i).y, obj.getSpeculars().get(i).z);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId.get(i));
            GLES20.glUniform1i(m_textureUniform, 1);
            glDrawElements(GL_TRIANGLES, obj.getCount().get(i), GL_UNSIGNED_INT, obj.getStart().get(i)*4);
            glFlush();

        }

    }

    private void renderDepthMap() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);

        GLES20.glViewport(0, 0, displayWidth, displayHeight);

        // Clear color and buffers
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Start using the shader
        GLES20.glUseProgram(depthMapProgram.getProgram());

        //load light mvp matrix
        float[] light_MVMatrix = new float[16];
        float[] light_MVPMatrix = new float[16];

        multiplyMM(light_MVMatrix, 0, mLightViewMatrix, 0, getModelMatrix(), 0);
        multiplyMM(light_MVPMatrix, 0, mLightProjectionMatrix, 0, light_MVMatrix, 0);
        glUniformMatrix4fv(d_mvpMatrixUniform, 1, false, light_MVPMatrix, 0);

        //load model data
        glVertexAttribPointer(glGetAttribLocation(depthMapProgram.getProgram(), "aPosition"), 4, GL_FLOAT,
                false, Constant.POINT_SIZE, 0);
        glEnableVertexAttribArray(glGetAttribLocation(depthMapProgram.getProgram(), "aPosition"));
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);

        //render model depth
        glDrawElements(GL_TRIANGLES, obj.getIndexNumber() * 3, GL_UNSIGNED_INT, 0);


    }

    private float[] getModelMatrix() {
        float[] translateMatrix = new float[16];
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, 0, 0, -5);
        float[] res = new float[16];
        multiplyMM(res, 0, translateMatrix, 0, modelMatrix, 0);
        return res;
    }


    private LoadModel ReadObjAndMtl(String objFileName, String mtlFileName) {

        InputStream inputStream;
        InputStream inputStream2;
        try {
            inputStream = getContext().getAssets().open(objFileName);
            inputStream2 = getContext().getAssets().open(mtlFileName);

            //如果从网上下载，则用下面导入路径文件流的
            //inputStream = new FileInputStream(new File(getContext().getExternalCacheDir(), objFileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        LoadModel obj;
        try {
            obj = new LoadModel(inputStream, inputStream2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return obj;
    }


    private float lastX;
    private float lastY;
    private float oldDis;
    private int mode = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        event.getAction();
        event.getActionIndex();
        event.getActionMasked();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                mode = 0;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = 1;
                oldDis = spacing(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == 1) {
                    float nowDis = spacing(event);
                    float scale = (float)Math.sqrt(nowDis / oldDis);
                    scale(scale);
                    oldDis = spacing(event);
                    requestRender();

                } else {
                    float deltaX = event.getX() - lastX;
                    float deltaY = event.getY() - lastY;
                    if (deltaX == 0 || deltaY == 0) {
                        break;
                    }
                    rotate(new Vec2(deltaY, deltaX));
                    lastX = event.getX(0);
                    lastY = event.getY(0);
                    requestRender();
                }

                break;
            case MotionEvent.ACTION_UP:
                break;

        }

        return true;
    }

    public void rotate(Vec2 v) {
        float[] sTemp = new float[16];
        setRotateM(sTemp, 0, 6, v.x, v.y, 0);
        multiplyMM(modelMatrix, 0, sTemp, 0, modelMatrix, 0);

    }

    public void scale(float scale) {

        scaleM(modelMatrix, 0, scale, scale, scale);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
