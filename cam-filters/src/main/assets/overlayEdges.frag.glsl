#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform sampler2D image;
uniform sampler2D edges;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;
uniform vec2 res;


void main ()
{
    vec4 color = texture2D(image, v_CamTexCoordinate);

    vec4 edgeColor = texture2D(edges, v_CamTexCoordinate);


    color = color * 25.0;
    color.r = floor(color.r);
    color.g = floor(color.g);
    color.b = floor(color.b);
    color /= 25.0;

    vec4 edgePower = vec4(0.0);
    for(float i = -1.0; i <= 1.0; i++) {
        for(float j = -1.; j <= 1.; j++) {
            edgePower +=  texture2D(edges, v_CamTexCoordinate + vec2(i * 1.0, j * 1.0) / res);

        }
    }

    edgePower =  texture2D(edges, v_CamTexCoordinate);



    if(edgePower.r >= 0.5) {
        color = (color * 2.0 + vec4(0.0) * 2.0) / 4.0;
    }
    else {
        color = (color * 6.0 + vec4(1.0) * 1.0) / 7.0;
    }


    //color -= edgePower;


    gl_FragColor = color;

}