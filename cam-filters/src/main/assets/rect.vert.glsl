attribute vec3 position;
attribute vec2 uv;

uniform mat4 transform;
varying vec2 vuv;

void main()
{
    vuv = uv;
    gl_Position = transform *  vec4(position.x, position.y, 0.0, 1.0);
}