#extension GL_OES_EGL_image_external : require

//necessary
precision highp float;
uniform sampler2D texture;

uniform sampler2D textureCurrent;
uniform sampler2D textureOld;

uniform sampler2D textureCamera;
uniform sampler2D textureCameraOld;

varying vec2 v_CamTexCoordinate;

uniform vec2 res;
uniform vec2 touch;


void main ()
{
    vec2 pos = v_CamTexCoordinate * res;

    vec4 color;

    color =
    ((texture2D(textureCurrent, v_CamTexCoordinate + vec2(-1.0 , 0.0) / res) +
         texture2D(textureCurrent, v_CamTexCoordinate + vec2(1.0 , 0.0) / res) +
         texture2D(textureCurrent, v_CamTexCoordinate + vec2(0.0 , 1.0) / res) +
         texture2D(textureCurrent, v_CamTexCoordinate + vec2(0.0 , -1.0) / res) +

        texture2D(textureCurrent, v_CamTexCoordinate + vec2(1.0 , 1.0) / res) +
        texture2D(textureCurrent, v_CamTexCoordinate + vec2(1.0 , -1.0) / res) +
        texture2D(textureCurrent, v_CamTexCoordinate + vec2(-1.0 , 1.0) / res) +
        texture2D(textureCurrent, v_CamTexCoordinate + vec2(-1.0 , -1.0) / res)

    ) / 4.0) -
         texture2D(textureOld, v_CamTexCoordinate);

    color  *= 0.95;

    gl_FragColor = color;


    pos = floor(pos);
    float dist = length(pos - touch);
    if(dist < 1.0){
        gl_FragColor = vec4(1.0);
    }

    vec4 diff = texture2D(textureCamera, v_CamTexCoordinate) - texture2D(textureCameraOld, v_CamTexCoordinate);;

    float len = length(diff);

    if(len > 0.5) {
        gl_FragColor += vec4(len/3.0);

    }
}