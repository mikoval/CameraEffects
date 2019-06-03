package com.androidexperiments.shadercam.gl;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class InverseShader extends  Shader{
    private static final String TAG = InverseShader.class.getSimpleName();

    final static String frag = "vid.frag.glsl";
    final static String vert = "inv.vert.glsl";

    public InverseShader(Context context, VideoRenderer renderer ) {
        super(context, renderer, frag, vert);
    }

    @Override
    public void setUniformsAndAttribs() {
        GLES20.glUseProgram(program);

        int textureParamHandle = GLES20.glGetUniformLocation(program, "camTexture");

        int textureTranformHandle = GLES20
                .glGetUniformLocation(program, "camTextureTransform");

        int positionMatrixHandle = GLES20
                .glGetUniformLocation(program, "uPMatrix");

        int textureCoordinateHandle = GLES20
                .glGetAttribLocation(program, "camTexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, renderer.mTexturesIds[0]);
        GLES20.glUniform1i(textureParamHandle, 0);


        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                renderer.textureBuffer);


        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, renderer.mCameraTransformMatrix, 0);
        GLES20.glUniformMatrix4fv(positionMatrixHandle, 1, false, renderer.mOrthoMatrix, 0);


        super.setUniformsAndAttribs();
    }
}
