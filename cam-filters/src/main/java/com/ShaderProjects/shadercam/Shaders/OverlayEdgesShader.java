package com.ShaderProjects.shadercam.Shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;

public class OverlayEdgesShader extends Shader {
    final static String frag = "overlayEdges.frag.glsl";
    final static String vert = "basic.vert.glsl";

    int image = 0, edgesImage =0;

    public OverlayEdgesShader(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
    }


    public void draw(int image, int edgesImage){
        this.image = image;
        this.edgesImage = edgesImage;
        super.draw();
    }

    @Override
    protected void setUniformsAndAttribs() {



        int textureCoordinateHandle = GLES20
                .glGetAttribLocation(program, "camTexCoordinate");
        int positionHandle = GLES20.glGetAttribLocation(program, "position");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                renderer.vertexBuffer);



        int imageHandle = GLES20.glGetUniformLocation(program, "image");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, image);
        GLES20.glUniform1i(imageHandle, 0);


        int edgeHandle = GLES20.glGetUniformLocation(program, "edges");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, edgesImage);
        GLES20.glUniform1i(edgeHandle, 1);


        int resLoc = GLES20.glGetUniformLocation(program, "res");
        GLES20.glUniform2f(resLoc, renderer.getWidth(), renderer.getHeight());



    }

}
