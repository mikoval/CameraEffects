package com.ShaderProjects.shadercam.Shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class EdgeShader extends Shader {
    final static String frag = "edge.frag.glsl";
    final static String vert = "basic.vert.glsl";

    int texture;

    public EdgeShader(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
    }

    @Override
    public void setUniformsAndAttribs() {
        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, renderer.getWidth(), renderer.getHeight());

        int textureParamHandle = GLES20.glGetUniformLocation(program, "texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(textureParamHandle, 0);

        super.setUniformsAndAttribs();
    }

    public void draw(int texture){
        this.texture = texture;
        super.draw();
    }
}
