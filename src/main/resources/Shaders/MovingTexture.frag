#ifdef HAS_COLORMAP
    uniform sampler2D m_ColorMap;
    varying vec2 texCoord1;
#endif

#ifdef HAS_VISIBILITY
    uniform float m_visibility;
#endif

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLORMAP
        color *= texture2D(m_ColorMap, texCoord1);
    #endif

    #ifdef HAS_VISIBILITY
        color *= vec4(1.0, 1.0, 1.0, m_visibility);
    #endif



    gl_FragColor = color;
}