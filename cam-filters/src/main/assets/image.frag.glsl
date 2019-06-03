#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform sampler2D texture;



varying vec2 v_CamTexCoordinate;



void main ()
{

    gl_FragColor =  texture2D(texture, v_CamTexCoordinate);

}