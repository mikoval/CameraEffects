package com.ShaderProjects.shadercam.Shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class BlurShader extends Shader {
    final static String frag = "blur.frag.glsl";
    final static String vert = "basic.vert.glsl";

    int texture = 0;

    public BlurShader(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
    }


    @Override
    public void setUniformsAndAttribs() {


        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, renderer.getWidth(), renderer.getHeight());

        super.setUniformsAndAttribs();
    }


    public void draw(int texture){
        this.texture = texture;
        super.draw();
    }




}
