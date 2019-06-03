package com.ShaderProjects.shadercam.Shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import org.joml.Matrix4f;
import android.util.Log;

import com.androidexperiments.shadercam.gl.Shader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class glRect {
    private IntBuffer VAO;
    private int program;
    private Context context;
    private Matrix4f transform;
    private int texture;

    static final float vertices[] = {
            1.0f,  1.0f, 0.0f,  // top right
            1.0f, -1.0f, 0.0f,  // bottom right
            -1.0f, -1.0f, 0.0f,  // bottom left
            -1.0f,  1.0f, 0.0f   // top left
    };
    static final float uvs[] = {
            1.0f,  1.0f,  // top right
            1.0f,  0.0f,   // bottom right
            0.0f,  0.0f,  // bottom left
            0.0f,  1.0f  // top left
    };


    static final int indices[] = {  // note that we start from 0!
            0, 1, 3,   // first triangle
            1, 2, 3    // second triangle
    };

    public glRect(Context context) {
        this.context = context;
    }

    public void init() {

        program = Shader.createProgram(context, "rect.vert.glsl", "rect.frag.glsl");

        GLES30.glUseProgram(program);

        VAO = IntBuffer.allocate(1);
        IntBuffer VBO = IntBuffer.allocate(2);
        IntBuffer EBO = IntBuffer.allocate(1);

        GLES30.glGenVertexArrays(2, VAO);


        GLES30.glGenBuffers(2, VBO);
        GLES30.glGenBuffers(1, EBO);

        GLES30.glBindVertexArray(VAO.get(0));



        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBO.get(0));
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, IntBuffer.wrap(indices), GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO.get(0));
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.length * 4, FloatBuffer.wrap(vertices), GLES30.GL_STATIC_DRAW);
        int positionHandle = GLES20.glGetAttribLocation(program, "position");
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO.get(1));
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, uvs.length * 4, FloatBuffer.wrap(uvs), GLES30.GL_STATIC_DRAW);
        int uvHandle = GLES20.glGetAttribLocation(program, "uv");
        GLES30.glEnableVertexAttribArray(uvHandle);
        GLES30.glVertexAttribPointer(uvHandle, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);


        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    public void draw() {

        GLES30.glEnable(GLES30.GL_BLEND);

        GLES30.glUseProgram(program);

        GLES30.glBindVertexArray(VAO.get(0));

        if(transform != null ){
            int transformLocation = GLES30.glGetUniformLocation(program, "transform");

            FloatBuffer fb = ByteBuffer.allocateDirect(16 << 2).asFloatBuffer();
            transform.get(fb);
            GLES30.glUniformMatrix4fv(transformLocation, 1, false, fb);
        }
        {
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            int textureParamHandle = GLES20.glGetUniformLocation(program, "texture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glUniform1i(textureParamHandle, 0);
        }

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glBindVertexArray(0);

        GLES30.glDisable(GLES30.GL_BLEND);

    }
    public void setTransform(Matrix4f mat){
        transform = mat;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }
}
