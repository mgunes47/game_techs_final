package com.blockworld.ui;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 2D UI render sistemi (overlay) - Basitleştirilmiş versiyon.
 */
public class UIRenderer {

    private int shaderProgram;
    private int quadVaoId;
    private int quadVboId;
    private int quadEboId;

    private int projectionLoc;
    private int modelLoc;
    private int colorLoc;

    private Matrix4f projectionMatrix;
    private Matrix4f modelMatrix;

    private int screenWidth;
    private int screenHeight;

    private static final String VERTEX_SHADER = "#version 330 core\n" +
            "layout (location = 0) in vec2 aPos;\n" +
            "uniform mat4 projection;\n" +
            "uniform mat4 model;\n" +
            "void main() {\n" +
            "    gl_Position = projection * model * vec4(aPos, 0.0, 1.0);\n" +
            "}\n";

    private static final String FRAGMENT_SHADER = "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "uniform vec4 color;\n" +
            "void main() {\n" +
            "    FragColor = color;\n" +
            "}\n";

    public UIRenderer() {
        this.projectionMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
    }

    public void init(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        // Shader oluştur
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, VERTEX_SHADER);
        glCompileShader(vertexShader);

        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == 0) {
            System.err.println("UI Vertex Shader hatası: " + glGetShaderInfoLog(vertexShader, 1024));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, FRAGMENT_SHADER);
        glCompileShader(fragmentShader);

        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == 0) {
            System.err.println("UI Fragment Shader hatası: " + glGetShaderInfoLog(fragmentShader, 1024));
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == 0) {
            System.err.println("UI Shader link hatası: " + glGetProgramInfoLog(shaderProgram, 1024));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // Uniform locations
        projectionLoc = glGetUniformLocation(shaderProgram, "projection");
        modelLoc = glGetUniformLocation(shaderProgram, "model");
        colorLoc = glGetUniformLocation(shaderProgram, "color");

        // Quad mesh
        float[] vertices = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        };

        int[] indices = { 0, 1, 2, 2, 3, 0 };

        quadVaoId = glGenVertexArrays();
        glBindVertexArray(quadVaoId);

        quadVboId = glGenBuffers();
        FloatBuffer vertBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertBuffer.put(vertices).flip();
        glBindBuffer(GL_ARRAY_BUFFER, quadVboId);
        glBufferData(GL_ARRAY_BUFFER, vertBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(vertBuffer);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        quadEboId = glGenBuffers();
        IntBuffer indBuffer = MemoryUtil.memAllocInt(indices.length);
        indBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadEboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(indBuffer);

        glBindVertexArray(0);

        // Projeksiyon
        updateProjection(width, height);

        System.out.println("UI Renderer başlatıldı!");
    }

    public void updateProjection(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        projectionMatrix.identity();
        projectionMatrix.ortho(0, width, height, 0, -1, 1);
    }

    public void beginRender() {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE); // UI için culling'i kapat
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            projectionMatrix.get(fb);
            glUniformMatrix4fv(projectionLoc, false, fb);
        }
    }

    public void endRender() {
        glUseProgram(0);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE); // 3D dünya için tekrar aç
    }

    public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        modelMatrix.identity();
        modelMatrix.translate(x, y, 0);
        modelMatrix.scale(width, height, 1);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            modelMatrix.get(fb);
            glUniformMatrix4fv(modelLoc, false, fb);
        }

        glUniform4f(colorLoc, r, g, b, a);

        glBindVertexArray(quadVaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void cleanup() {
        glDeleteProgram(shaderProgram);
        glDeleteBuffers(quadVboId);
        glDeleteBuffers(quadEboId);
        glDeleteVertexArrays(quadVaoId);
    }
}
