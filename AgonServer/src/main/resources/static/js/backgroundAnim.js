
(async () => {
    const SPEED = 0.40, SCALE = 3, WARP = 1.50;
    const GRAIN = 0.004, FLOW = 1.0;

    const cv = document.getElementById("gsf");
    const gl = cv.getContext("webgl");

    if (!gl) {
        console.error("WebGL is unavailable.");
        return;
    }

    // Actually load the shader source!
    const response = await fetch("/shaders/background.frag");

    if (!response.ok) {
        throw new Error(`Shader fetch failed: ${response.status}`);
    }

    const fragmentSource = await response.text();

    const vertexSource = `
        attribute vec2 p;
        void main() {
            gl_Position = vec4(p, 0.0, 1.0);
        }
    `;

    function compile(type, source) {
        const shader = gl.createShader(type);
        gl.shaderSource(shader, source);
        gl.compileShader(shader);

        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            throw new Error(gl.getShaderInfoLog(shader));
        }

        return shader;
    }

    const prog = gl.createProgram();

    gl.attachShader(prog, compile(gl.VERTEX_SHADER, vertexSource));
    gl.attachShader(prog, compile(gl.FRAGMENT_SHADER, fragmentSource));

    gl.linkProgram(prog);

    if (!gl.getProgramParameter(prog, gl.LINK_STATUS)) {
        throw new Error(gl.getProgramInfoLog(prog));
    }

    gl.useProgram(prog);

    const buf = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buf);
    gl.bufferData(
        gl.ARRAY_BUFFER,
        new Float32Array([-1,-1, 1,-1, -1,1, 1,1]),
        gl.STATIC_DRAW
    );

    const pl = gl.getAttribLocation(prog, "p");
    gl.enableVertexAttribArray(pl);
    gl.vertexAttribPointer(pl, 2, gl.FLOAT, false, 0, 0);

    const U = name => gl.getUniformLocation(prog, name);

    const uT = U("u_time");
    const uR = U("u_resolution");

    gl.uniform1f(U("u_speed"), SPEED);
    gl.uniform1f(U("u_scale"), SCALE);
    gl.uniform1f(U("u_warp"), WARP);
    gl.uniform1f(U("u_grain"), GRAIN);
    gl.uniform1f(U("u_flow"), FLOW);
    gl.uniform1f(U("u_warpOn"), WARP > 0 ? 1 : 0);
    gl.uniform1f(U("u_grainOn"), GRAIN > 0 ? 1 : 0);

    const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
    const dpr = Math.min(1.5, devicePixelRatio || 1);

    function draw(t) {
        const w = Math.max(1, Math.round(cv.clientWidth * dpr));
        const h = Math.max(1, Math.round(cv.clientHeight * dpr));

        if (cv.width !== w || cv.height !== h) {
            cv.width = w;
            cv.height = h;
            gl.viewport(0, 0, w, h);
        }

        gl.uniform1f(uT, t);
        gl.uniform2f(uR, cv.width, cv.height);

        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    }

    if (reduced) {
        draw(2);
    } else {
        const start = performance.now();

        function loop(now) {
            draw((now - start) / 1000);
            requestAnimationFrame(loop);
        }

        requestAnimationFrame(loop);
    }
})().catch(console.error);
