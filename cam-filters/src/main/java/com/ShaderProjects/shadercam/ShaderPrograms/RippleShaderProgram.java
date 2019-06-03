package com.ShaderProjects.shadercam.ShaderPrograms;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.ShaderProjects.shadercam.Shaders.ImageShader;
import com.ShaderProjects.shadercam.Shaders.RefractShader;
import com.ShaderProjects.shadercam.Shaders.RippleShader;
import com.androidexperiments.shadercam.gl.BasicShader;
import com.androidexperiments.shadercam.gl.FrameBuffer;
import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class RippleShaderProgram extends Shader {
    final static String frag = "edge.frag.glsl";
    final static String vert = "basic.vert.glsl";

    private FrameBuffer fboCurrent;
    private FrameBuffer fboOld;
    private FrameBuffer fboExtra;

    static final int SCALE = 4 ;
    private int count;
    int width = 100;
    int height = 100;

    private FrameBuffer fboCamera;
    private FrameBuffer fboCameraOld;


    private Context context;

    private RippleShader rippleShader;
    private BasicShader basicShader;
    private ImageShader imageShader;
    private RefractShader refractShader;


    public RippleShaderProgram(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
        this.context = context;

    }

    @Override
    public void init(){
        super.init();

        count = 0;

        width = renderer.getWidth()/SCALE;
        height = renderer.getHeight()/SCALE;

        fboCurrent = new FrameBuffer(width, height);
        fboOld = new FrameBuffer(width, height);
        fboExtra = new FrameBuffer(width, height);

        fboCamera = new FrameBuffer(renderer.getWidth(), renderer.getHeight());
        fboCameraOld = new FrameBuffer(renderer.getWidth(), renderer.getHeight());


        rippleShader = new RippleShader(context, renderer, width, height);
        rippleShader.init();

        basicShader = new BasicShader(context, renderer);
        basicShader.init();

        imageShader = new ImageShader(context, renderer);
        imageShader.init();

        refractShader = new RefractShader(context, renderer, width, height);
        refractShader.init();
    }

    @Override
    public void setUniformsAndAttribs() {
        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, width, height);

        super.setUniformsAndAttribs();
    }


    @Override
    public void update() {
        if(rippleShader != null){
            rippleShader.update();
        }

    }
    @Override
    public void setTouch(float x, float y){
        y = renderer.getHeight() - y;
        rippleShader.setTouch(x/SCALE, y/SCALE);
    }



        @Override
    public void draw() {
        if(fboCurrent == null) {
            return;
        }



        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboCamera.fbo);
        basicShader.draw();



        GLES30.glViewport(0, 0, width, height);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboExtra.fbo);
        rippleShader.draw(fboCurrent.texture, fboOld.texture, fboCamera.texture, fboCameraOld.texture);
        swapBuffers();


        GLES30.glViewport(0, 0, renderer.getWidth(), renderer.getHeight());

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        refractShader.draw(fboCamera.texture, fboCurrent.texture);


        swapCameraBuffers();
    }


    private void swapBuffers() {
        int tmpfbo = fboCurrent.fbo;
        int tmptexture = fboCurrent.texture;
        fboOld.fbo = fboCurrent.fbo;
        fboOld.texture = fboCurrent.texture;

        fboCurrent.fbo = fboExtra.fbo;
        fboCurrent.texture = fboExtra.texture;

        fboExtra.fbo = tmpfbo;
        fboExtra.texture = tmptexture;
    }

    private  void swapCameraBuffers() {
        int tmpfbo = fboCamera.fbo;
        int tmptexture = fboCamera.texture;
        fboCamera.fbo = fboCameraOld.fbo;
        fboCamera.texture = fboCameraOld.texture;
        fboCameraOld.fbo = tmpfbo;
        fboCameraOld.texture = tmptexture;
    }




}

