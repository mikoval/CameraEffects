package com.ShaderProjects.shadercam.ShaderPrograms;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.ShaderProjects.shadercam.Shaders.EdgeShader;
import com.androidexperiments.shadercam.gl.BasicShader;
import com.androidexperiments.shadercam.gl.FrameBuffer;
import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class EdgeShaderProgram extends Shader {
    final static String frag = "edge.frag.glsl";
    final static String vert = "basic.vert.glsl";

    private FrameBuffer fbo;
    private FrameBuffer fbo2;

    private Context context;

    private BasicShader basicShader;
    private EdgeShader edgeShader;

    public EdgeShaderProgram(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
        this.context = context;
    }

    @Override
    public void init(){
        super.init();


        fbo = new FrameBuffer(renderer.getWidth(), renderer.getHeight());
        fbo2 = new FrameBuffer(renderer.getWidth(), renderer.getHeight());

        basicShader = new BasicShader(context, renderer);
        basicShader.init();

        edgeShader = new EdgeShader(context, renderer);
        edgeShader.init();
    }

    @Override
    public void setUniformsAndAttribs() {
        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, renderer.getWidth(), renderer.getHeight());

        super.setUniformsAndAttribs();
    }


    @Override
    public void draw() {
        if(fbo == null) {
            return;
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo.fbo);
        basicShader.draw();
        swapBuffers();

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        edgeShader.draw(fbo2.texture);
        swapBuffers();

    }

    private void swapBuffers() {
        int tmpfbo = fbo.fbo;
        int tmptexture = fbo.texture;
        fbo.fbo = fbo2.fbo;
        fbo.texture = fbo2.texture;
        fbo2.fbo = tmpfbo;
        fbo2.texture = tmptexture;

    }

}

