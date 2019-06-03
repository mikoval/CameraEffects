#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform sampler2D texture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

uniform vec2 res;

mat3 k1 = mat3(-1, 0, 1,
           -2, 0, 2,
           -1, 0, 1);

 mat3 k2 = mat3(1, 2, 1,
            0, 0, 0,
           -1,-2,-1);

void main ()
{
    vec4 cameraColor = texture2D(texture, v_CamTexCoordinate);

    vec4 color1 = vec4(0.0);
    vec4 color2 = vec4(0.0);
    for(int i = -1; i <= 1; i++) {
        for(int j = -1; j <= 1; j++) {
            color1 += k1[i + 1][j + 1] * texture2D(texture, v_CamTexCoordinate + vec2(i * 1, j * 1) / res);
            color2 += k2[i + 1][j + 1] * texture2D(texture, v_CamTexCoordinate + vec2(i * 1, j * 1) / res);
        }
    }


    float mag = length(color1) + length(color2);


    gl_FragColor = vec4(smoothstep(0.05, 0.3, mag));

    if(res.x == 0.0 ) {
        gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    }
}