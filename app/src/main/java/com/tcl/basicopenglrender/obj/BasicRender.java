package com.tcl.basicopenglrender.obj;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.tcl.basicopenglrender.utils.tLog;

import java.io.InputStream;

/**
 * 项目名：   BasicOpenglRender
 * 包名：     com.tcl.basicopenglrender.obj
 * 文件名：   BasicRender
 * 创建者：   root
 * 创建时间： 17-4-28 上午11:26
 * 描述：     TODO
 */

public abstract class BasicRender {

    private static final String TAG = BasicRender.class.getSimpleName();

    protected Resources mRes;
    protected int mProgram;
    protected int mHPosition;
    protected int mHCoord;
    protected int mHNormal;
    protected int mHKa;
    protected int mHKd;
    protected int mHKs;
    protected int mHMatrix;

    protected int mHTexture;
    protected int textureType = 0;
    protected int textureId = 0;

    protected float[] matrix = new float[16];

    protected String vertexFileName;
    protected String fragmentFileName;

    protected String pathName;

    public BasicRender(Resources mRes) {
        this.mRes = mRes;
        tLog.i(TAG,"super Res: "+ mRes);
        initBuffer();
    }

    public int getTextureType() {
        return textureType;
    }

    public void setTextureType(int textureType) {
        this.textureType = textureType;
    }

    public final int getTextureId() {
        return textureId;
    }

    public final void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public float[] getMatrix() {
        return matrix;
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }


    public final void create() {
        tLog.i(TAG,"super create()");
        onCreate();
    }

    public final void setSize(int width, int height) {
        tLog.i(TAG,"super setSize()");
        onSizeChanged(width,height);
    }

    public void draw() {
        tLog.i(TAG,"super draw()");
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    protected void initBuffer(){
        tLog.i(TAG,"super initBuffer()");
    }

    protected void onClear(){
        tLog.i(TAG,"super onClear()");
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void onUseProgram(){
        tLog.i(TAG,"super onUseProgram()");
        GLES20.glUseProgram(mProgram);
    }

    protected void onBindTexture(){
        tLog.i(TAG,"super onBindTexture()");
    }

    protected void onSetExpandData(){
        tLog.i(TAG,"super onSetExpandData()");
        GLES20.glUniformMatrix4fv(mHMatrix,1,false,matrix,0);
    }

    protected void onDraw(){
        tLog.i(TAG,"super onDraw()");
    }

    protected abstract void onCreate();

    protected abstract void onSizeChanged(int width,int height);

    public static void glError(int code, Object index) {
        tLog.i(TAG,"super glError()");
        if (code != 0) {
            tLog.e(TAG, "glError:" + code + "---" + index);
        }
    }

    protected void createProgramByAssetsFile(String vertex, String fragment) {
        tLog.i(TAG,"super createProgramByAssetsFile()");
        mProgram = createProgram(getShaderCodeFromRes(mRes, vertex), getShaderCodeFromRes(mRes, fragment));
    }

    //通过路径加载Assets中的文本内容
    protected static String getShaderCodeFromRes(Resources mRes, String path) {
        tLog.i(TAG,"super getShaderCodeFromRes()");
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }

    //加载shader
    protected static int loadShader(int shaderType, String source) {
        tLog.i(TAG,"super loadShader()");
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                glError(1, "Could not compile shader:" + shaderType);
                glError(1, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }


    //创建GL程序
    protected static int createProgram(String vertexSource, String fragmentSource) {
        tLog.i(TAG,"super createProgram()");
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertex == 0) return 0;
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                glError(1, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    protected static int createTexture(Bitmap bitmap) {
        tLog.i(TAG,"super createTexture()");
        int[] texture = new int[1];
        if (bitmap != null && !bitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture[0];
        }
        return 0;
    }
}
