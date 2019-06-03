#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform sampler2D texture;

varying vec2 v_CamTexCoordinate;

uniform vec2 res;

mat3 k = mat3(1., 1., 1.,
           1., 1., 1.,
           1., 1., 1.);

void main ()
{
    vec4 cameraColor = texture2D(texture, v_CamTexCoordinate);


    vec4 color = vec4(0.0);
    for(int i = -1; i <= 1; i++) {
        for(int j = -1; j <= 1; j++) {
            color += k[i + 1][j + 1] * texture2D(texture, v_CamTexCoordinate + vec2(float(i) * 1.0, float(j) * 1) / res);

        }
    }
    color /= 9.0;




    gl_FragColor = vec4(color.rgb, 1.0);

}