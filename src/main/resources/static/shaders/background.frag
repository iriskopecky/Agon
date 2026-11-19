precision highp float;
uniform float u_time,u_speed,u_scale,u_warp,u_grain,u_flow,u_warpOn,u_grainOn;
uniform vec2 u_resolution;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);float a=hash(i),b=hash(i+vec2(1.,0.)),c=hash(i+vec2(0.,1.)),d=hash(i+vec2(1.,1.));vec2 u=f*f*(3.-2.*f);return mix(mix(a,b,u.x),mix(c,d,u.x),u.y);}
float fbm(vec2 p){float v=0.,a=0.5;for(int i=0;i<4;i++){v+=a*noise(p);p*=2.0;a*=0.5;}return v;}
void main(){
    vec2 uv=gl_FragCoord.xy/u_resolution.xy; uv.x*=u_resolution.x/u_resolution.y;
    float t=u_time*u_speed*u_flow;
    vec2 p=uv*u_scale;
    vec2 q=vec2(fbm(p+t*0.1),fbm(p+vec2(5.2,1.3)-t*0.12));
    vec2 w=p+(q-0.5)*u_warp*u_warpOn*2.0;
    float n=fbm(w+t*0.15);
    vec3 dark=vec3(0.12,0.04,0.04),vio=vec3(0.04,0.10,0.12),pink=vec3(0.04,0.10,0.12);
    vec3 col=mix(dark,vio,smoothstep(0.15,0.6,n));
    col=mix(col,pink,smoothstep(0.55,0.92,n));
    col+=(hash(gl_FragCoord.xy+u_time)-0.5)*u_grain*u_grainOn;
    gl_FragColor=vec4(col,1.0);
}