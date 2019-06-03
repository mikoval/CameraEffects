uniform vec4 color;
varying vec2 vuv;
uniform sampler2D texture;

void main ()
{
    gl_FragColor = texture2D(texture, vec2(vuv.x, 1.0 - vuv.y));
}