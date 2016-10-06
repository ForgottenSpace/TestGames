uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
uniform vec2 m_posDelta;
uniform float m_parallaxScale;
 
const float pi = 3.14159;
 
#ifdef HAS_COLORMAP
    attribute vec2 inTexCoord;
    varying vec2 texCoord1;
#endif
 
void main(){
    #ifdef HAS_COLORMAP
        texCoord1 = inTexCoord;
        texCoord1 += m_posDelta * m_parallaxScale;
    #endif
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}