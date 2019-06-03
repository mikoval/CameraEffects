package com.ShaderProjects.shadercam;

import com.androidexperiments.shadercam.gl.Shader;

public class ShaderHandle {
    public String text;
    public Shader shader;
    public int image;

    public ShaderHandle(String text, Shader shader, int image) {
        this.text = text;
        this.shader = shader;
        this.image = image;
    }
}
