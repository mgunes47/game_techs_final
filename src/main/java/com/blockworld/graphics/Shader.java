package com.blockworld.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * OpenGL shader program yönetimi.
 */
public class Shader {

    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private final Map<String, Integer> uniforms;

    public Shader() {
        this.uniforms = new HashMap<>();
    }

    public void init(String vertexPath, String fragmentPath) throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Shader programı oluşturulamadı!");
        }

        // Vertex shader
        vertexShaderId = createShader(loadResource(vertexPath), GL_VERTEX_SHADER);

        // Fragment shader
        fragmentShaderId = createShader(loadResource(fragmentPath), GL_FRAGMENT_SHADER);

        // Program'ı linkle
        link();
    }

    public void initFromSource(String vertexSource, String fragmentSource) throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Shader programı oluşturulamadı!");
        }

        vertexShaderId = createShader(vertexSource, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentSource, GL_FRAGMENT_SHADER);

        link();
    }

    private int createShader(String source, int type) throws Exception {
        int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            throw new Exception("Shader oluşturulamadı! Type: " + type);
        }

        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Shader derleme hatası: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    private void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Shader linkleme hatası: " + glGetProgramInfoLog(programId, 1024));
        }

        // Shader'ları artık silebiliriz
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Shader validasyon uyarısı: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void createUniform(String uniformName) throws Exception {
        int location = glGetUniformLocation(programId, uniformName);
        if (location < 0) {
            System.err.println("Uyarı: Uniform bulunamadı: " + uniformName);
        }
        uniforms.put(uniformName, location);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float x, float y, float z, float w) {
        Integer loc = uniforms.get(uniformName);
        if (loc != null) {
            glUniform4f(loc, x, y, z, w);
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    private String loadResource(String path) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new Exception("Kaynak bulunamadı: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
