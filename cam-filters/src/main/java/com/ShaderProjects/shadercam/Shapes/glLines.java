package com.ShaderProjects.shadercam.Shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.androidexperiments.shadercam.gl.Shader;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class glLines {
    private IntBuffer VAO;
    private int program;
    private Context context;
    private Matrix4f transform;
    private ArrayList<Point> vertices;
    private IntBuffer VBO;

    public float points[];



    public static class Point {
        public float x;
        public float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    public glLines(Context context) {
        this.context = context;
    }

    public void init() {
        program = Shader.createProgram(context, "line.vert.glsl", "rect.frag.glsl");
        GLES30.glUseProgram(program);

        VAO = IntBuffer.allocate(1);
        VBO = IntBuffer.allocate(1);
        GLES30.glGenVertexArrays(1, VAO);
        GLES30.glGenBuffers(1, VBO);

        GLES30.glBindVertexArray(VAO.get(0));

        //Log.d("FINDME:" , "PRINTING VERTICES: " + Arrays.toString(points));
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO.get(0));

        //GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, points.length * 4, FloatBuffer.wrap(points), GLES30.GL_STATIC_DRAW);

        int positionHandle = GLES20.glGetAttribLocation(program, "position");
        GLES30.glEnableVertexAttribArray(positionHandle);

        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);

        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    public void draw() {

        GLES30.glUseProgram(program);

        GLES30.glBindVertexArray(VAO.get(0));
/*
        if(transform != null ){
            int transformLocation = GLES30.glGetUniformLocation(program, "transform");

            FloatBuffer fb = ByteBuffer.allocateDirect(16 << 2).asFloatBuffer();
            transform.get(fb);
            GLES30.glUniformMatrix4fv(transformLocation, 1, false, fb);
        }

 */
        GLES30.glLineWidth(10);
        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, points.length/2);
        //Log.d("FINDME", "DRAWING LINE OF LENGTH: " + points.length);
        //GLES30.glDrawElements(GLES30.GL_LINE_LOOP, 6, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glBindVertexArray(0);
    }

    private void bindVertices() {

        GLES30.glUseProgram(program);

        GLES30.glBindVertexArray(VAO.get(0));

        //Log.d("FINDME:" , "PRINTING VERTICES: " + Arrays.toString(points));
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO.get(0));
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, points.length * 4, FloatBuffer.wrap(points), GLES30.GL_STATIC_DRAW);
        int positionHandle = GLES20.glGetAttribLocation(program, "position");
        GLES30.glEnableVertexAttribArray(positionHandle);

        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);

        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }
    public void setVertices(ArrayList<Point> vertices) {
        this.vertices = vertices;
        this.points = convertVertices();
        bindVertices();
    }

    public void setTransform(Matrix4f mat){
        transform = mat;
    }

    public float[] convertVertices() {
        float ret[] = new float[vertices.size() * 2];
        for(int i = 0; i < vertices.size(); i++) {
            ret[2 * i] = vertices.get(i).x;
            ret[2 * i + 1] = vertices.get(i).y;
        }

        //Log.d("FINDME:" , "VERTICES: " + Arrays.toString(ret));
        return ret;
    }

}
