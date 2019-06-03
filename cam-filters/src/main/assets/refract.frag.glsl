precision mediump float;
uniform sampler2D textureImage;
uniform sampler2D textureRefract;


uniform vec2 refractRes;


varying vec2 v_CamTexCoordinate;



void main ()
{

    vec4 refract =  texture2D(textureRefract, v_CamTexCoordinate);

    float Xoffset = texture2D(textureRefract, v_CamTexCoordinate + vec2(-1.0, 0.0) / refractRes).r -
              texture2D(textureRefract, v_CamTexCoordinate + vec2( 1.0, 0.0) / refractRes).r;

    float Yoffset = texture2D(textureRefract, v_CamTexCoordinate + vec2(0.0, -1.0) / refractRes).r -
              texture2D(textureRefract, v_CamTexCoordinate + vec2(0.0,  1.0) / refractRes).r;

    float shading = Xoffset;

    vec4 t = texture2D(textureImage, v_CamTexCoordinate + vec2(Xoffset, Yoffset ));

    vec4 color = shading + t;


    gl_FragColor = vec4(color.rgb, 1.0);
}