package com.androidexperiments.shadercam.gl;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;

public class FrameBuffer {
    public int fbo, texture;

    public FrameBuffer(int width, int height) {
        this(width, height, GLES30.GL_UNSIGNED_BYTE);
    }

    public FrameBuffer(int width, int height, int type) {
        // The framebuffer, which regroups 0, 1, or more textures, and 0 or 1 depth buffer.
        int[] frameBuffers = new int[1];
        int[] renderedTexture = new int[1];;
        GLES30.glGenFramebuffers(1, frameBuffers, 0);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[0]);

        GLES30.glGenTextures(1, renderedTexture, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderedTexture[0]);

        int format = GLES30.GL_RGBA;

        if(type == GLES30.GL_FLOAT) {
            format = GLES30.GL_RGBA32F;
        }
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, format, width, height, 0,GLES30.GL_RGBA, type, null);

        Log.d("FINDME-INIT: ", "CREATING TEX IMAGE: " + GLES30.glGetError());



        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, renderedTexture[0], 0);




        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("FINDME-INIT: ", "FAILED TO GENERATE FBO: " + GLES30.glGetError() + " , " + GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER));

        } else {
            Log.d("FINDME-INIT: ", "SUCCESSFULLY TO GENERATE FBO");

        }


        fbo = frameBuffers[0];
        texture = renderedTexture[0];


        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);


        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("FINDME-INIT: ", "INVALID FBO");

        } else {
            Log.d("FINDME-INIT: ", "SUCCESSFUL FBO SET");
        }


        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);



    }



}
