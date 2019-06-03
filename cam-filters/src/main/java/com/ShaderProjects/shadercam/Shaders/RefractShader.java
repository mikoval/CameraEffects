package com.ShaderProjects.shadercam.Shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class RefractShader extends Shader {
    final static String frag = "refract.frag.glsl";
    final static String vert = "basic.vert.glsl";

    int textureImage;
    int textureRefract;

    float width, height;

    public RefractShader(Context context, VideoRenderer renderer, float w, float h) {
        super(context, renderer, frag, vert);
        this.width = w;
        this.height = h;
    }

    @Override
    public void setUniformsAndAttribs() {
        int resLoc = GLES20.glGetUniformLocation(program, "refractRes");
        GLES20.glUniform2f(resLoc, width, height);

        int textureImageParamHandle = GLES20.glGetUniformLocation(program, "textureImage");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureImage);
        GLES20.glUniform1i(textureImageParamHandle, 0);

        int textureRefractParamHandle = GLES20.glGetUniformLocation(program, "textureRefract");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRefract);
        GLES20.glUniform1i(textureRefractParamHandle, 1);


        super.setUniformsAndAttribs();
    }


    public void draw(int textureImage, int textureRefract){
        this.textureImage = textureImage;
        this.textureRefract = textureRefract;
        super.draw();
    }

}
