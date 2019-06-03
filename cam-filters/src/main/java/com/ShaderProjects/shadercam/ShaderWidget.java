package com.ShaderProjects.shadercam;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidexperiments.shadercam.gl.Shader;

public class ShaderWidget extends RelativeLayout {
    private String text;
    private Context context;
    private int color;
    private ShaderWidget widget;

    private Shader shader;
    private int image;

    private TextView textView;

    public ShaderWidget(Context context) {
        super(context);
        this.context = context;
        this.widget = this;

        init();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void init() {
        textView = new TextView(context);
        textView.setText(text);
        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
       // textParams.setMargins(20, 20, 20, 20);
        textView.setLayoutParams(textParams);
        textView.setBackgroundColor(Color.LTGRAY);
       // textView.setPadding(10, 10, 10, 10);
        textView.setTypeface(null, Typeface.BOLD);

        this.addView(textView);

        LayoutParams params = new LayoutParams(250, 250);
        params.setMargins(20, 20, 20, 20);

        setLayoutParams(params);
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }
    public void setImage(int img) {
        this.image = img;

        this.setBackgroundResource(img);
    }

    public String getText() {return text;}
    public Shader getShader() {return shader;}
    public int getImage() {return image;}
}
