package com.ShaderProjects.shadercam.ShaderPrograms;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.ShaderProjects.shadercam.Shaders.BlurShader;
import com.ShaderProjects.shadercam.Shaders.EdgeShader;
import com.ShaderProjects.shadercam.Shaders.OverlayEdgesShader;
import com.androidexperiments.shadercam.gl.BasicShader;
import com.androidexperiments.shadercam.gl.FrameBuffer;
import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class CartoonShaderProgram extends Shader {
    final static String frag = "edge.frag.glsl";
    final static String vert = "basic.vert.glsl";

    private FrameBuffer fbo;
    private FrameBuffer fbo2;

    private FrameBuffer fboEdges;

    private Context context;

    private BlurShader blurShader;
    private BasicShader basicShader;
    private EdgeShader edgeShader;

    private OverlayEdgesShader overlayEdgesShader;

    public CartoonShaderProgram(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
        this.context = context;

    }

    @Override
    public void init(){
        super.init();


        fbo = new FrameBuffer(renderer.getWidth(), renderer.getHeight());
        fbo2 = new FrameBuffer(renderer.getWidth(), renderer.getHeight());
        fboEdges = new FrameBuffer(renderer.getWidth(), renderer.getHeight());


        blurShader = new BlurShader(context, renderer);
        blurShader.init();

        basicShader = new BasicShader(context, renderer);
        basicShader.init();

        edgeShader = new EdgeShader(context, renderer);
        edgeShader.init();

        overlayEdgesShader = new OverlayEdgesShader(context, renderer);
        overlayEdgesShader.init();
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

        for(int i = 0; i < 0; i++){

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo.fbo);
            blurShader.draw(fbo2.texture);
            swapBuffers();

        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboEdges.fbo);
        edgeShader.draw(fbo2.texture);


        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        overlayEdgesShader.draw(fbo2.texture, fboEdges.texture);

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

