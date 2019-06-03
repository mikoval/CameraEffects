package com.ShaderProjects.shadercam.Shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class RippleShader extends Shader {
    final static String frag = "ripple.frag.glsl";
    final static String vert = "basic.vert.glsl";

    int textureCurrent;
    int textureOld;

    int textureCamera;
    int textureCameraOld;

    int width, height;

    float touchX= 0, touchY=0;

    public RippleShader(Context context, VideoRenderer renderer, int w, int h) {
        super(context, renderer, frag, vert);
        width = w;
        height = h;
        touchX = -w / 2;
        touchY = -h / 2;
    }

    @Override
    public void setUniformsAndAttribs() {
        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, width, height);

        int touchLoc = GLES20.glGetUniformLocation(program, "touch");
        GLES20.glUniform2f(touchLoc, touchX, touchY);


        int textureCurrentParamHandle = GLES20.glGetUniformLocation(program, "textureCurrent");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureCurrent);
        GLES20.glUniform1i(textureCurrentParamHandle, 0);

        int textureOldParamHandle = GLES20.glGetUniformLocation(program, "textureOld");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureOld);
        GLES20.glUniform1i(textureOldParamHandle, 1);

        int textureCameraParamHandle = GLES20.glGetUniformLocation(program, "textureCamera");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureCamera);
        GLES20.glUniform1i(textureCameraParamHandle, 2);

        int textureCameraOldParamHandle = GLES20.glGetUniformLocation(program, "textureCameraOld");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureCameraOld);
        GLES20.glUniform1i(textureCameraOldParamHandle, 3);


        super.setUniformsAndAttribs();

        touchX = -width / 2;
        touchY = -height / 2;;
    }



    public void draw(int textureCurrent, int textureOld, int textureCamera, int textureCameraOld){
        this.textureCurrent = textureCurrent;
        this.textureOld = textureOld;

        this.textureCamera = textureCamera;
        this.textureCameraOld = textureCameraOld;

        super.draw();
    }

    @Override
    public void setTouch(float x, float y) {
        this.touchX = x;
        this.touchY = y;

        Log.d("FINDME" , "SETTING TOUCH :  " + x + ", " + y);

    }

}
