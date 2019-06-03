package com.androidexperiments.shadercam.gl;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.androidexperiments.shadercam.utils.ShaderUtils;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ShortBuffer;

public abstract class Shader {
    protected FirebaseVisionImage image;
    protected int imageWidth;
    protected int imageHeight;
    public int program;

    public String frag;
    public String vert;

    protected VideoRenderer renderer;

    private WeakReference<Context> mContextWeakReference;


    private static final String TAG = Shader.class.getSimpleName();

    /**
     * utility for checking GL errors
     */
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    public Shader(Context context, VideoRenderer renderer, String fragPath, String vertPath) {
        this.renderer = renderer;
        this.mContextWeakReference = new WeakReference<>(context);

        loadFromShadersFromAssets(fragPath, vertPath);

    }

    public void update() {
    }

    private void loadFromShadersFromAssets(String pathToFragment, String pathToVertex) {
        try {
            frag = ShaderUtils
                    .getStringFromFileInAssets(mContextWeakReference.get(), pathToFragment);
            vert = ShaderUtils
                    .getStringFromFileInAssets(mContextWeakReference.get(), pathToVertex);
        } catch (IOException e) {
            Log.e(TAG, "loadFromShadersFromAssets() failed. Check paths to assets.\n" + e
                    .getMessage());
        }
    }

    public void setRenderer(VideoRenderer renderer){
        this.renderer = renderer;

    }

    public static int createProgram (Context context, String vert, String frag) {
        int program;

        try {
            frag = ShaderUtils
                    .getStringFromFileInAssets(context, frag);
            vert = ShaderUtils
                    .getStringFromFileInAssets(context, vert);
        } catch (IOException e) {

            Log.e(TAG, "FINDME: " + frag + ", " + vert);
        }




        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vert);
        GLES20.glCompileShader(vertexShaderHandle);


        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, frag);
        GLES20.glCompileShader(fragmentShaderHandle);

        program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShaderHandle);
        GLES20.glAttachShader(program, fragmentShaderHandle);
        GLES20.glLinkProgram(program);

        return program;
    }

    public void init(){





        program = createProgram(mContextWeakReference.get(), vert, frag);




        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(program);
            Log.e("SurfaceTest", "FINDME : Error while linking program:\n" + error);
        }

    }

    public void draw() {
        GLES20.glUseProgram(program);
        setUniformsAndAttribs();
        renderer.drawElements();
    }

    protected void setUniformsAndAttribs() {
        int positionHandle = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                renderer.vertexBuffer);
    }

    public void setTouch(float x, float y) {}
    public void onDestroy() {}

    public void setVideoImage(FirebaseVisionImage image, int width, int height){
       // Log.d("FINDME: ", "SETTING VIDEO IMAGE IM SHADER");
        this.image = image;
        this.imageWidth = width;
        this.imageHeight = height;
    }
}
