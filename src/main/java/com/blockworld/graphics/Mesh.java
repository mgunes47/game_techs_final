package com.blockworld.graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

/**
 * 3D mesh verilerini yöneten sınıf (VAO, VBO, EBO).
 */
public class Mesh {

    private int vaoId;
    private int posVboId;
    private int colorVboId;
    private int normalVboId;
    private int eboId;

    private int vertexCount;

    public Mesh(float[] positions, float[] colors, float[] normals, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer colorBuffer = null;
        FloatBuffer normalBuffer = null;
        IntBuffer indicesBuffer = null;

        try {
            vertexCount = indices.length;

            // VAO oluştur
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Pozisyon VBO
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Renk VBO
            colorVboId = glGenBuffers();
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            // Normal VBO
            normalVboId = glGenBuffers();
            normalBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // EBO (Element Buffer Object)
            eboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            // VAO'yu unbind et
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            if (posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            if (colorBuffer != null)
                MemoryUtil.memFree(colorBuffer);
            if (normalBuffer != null)
                MemoryUtil.memFree(normalBuffer);
            if (indicesBuffer != null)
                MemoryUtil.memFree(indicesBuffer);
        }
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        // VBO'ları sil
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colorVboId);
        glDeleteBuffers(normalVboId);
        glDeleteBuffers(eboId);

        // VAO'yu sil
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /**
     * Birim küp oluşturur (1x1x1 boyutunda, merkez orijinde).
     */
    public static Mesh createCube(float r, float g, float b) {
        float[] positions = {
                // Ön yüz
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                // Arka yüz
                -0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                // Üst yüz
                -0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                // Alt yüz
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                // Sağ yüz
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                // Sol yüz
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
        };

        // Her vertex için aynı renk
        float[] colors = new float[24 * 3];
        for (int i = 0; i < 24; i++) {
            colors[i * 3] = r;
            colors[i * 3 + 1] = g;
            colors[i * 3 + 2] = b;
        }

        float[] normals = {
                // Ön yüz
                0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
                // Arka yüz
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
                // Üst yüz
                0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
                // Alt yüz
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
                // Sağ yüz
                1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
                // Sol yüz
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
        };

        int[] indices = {
                // Ön yüz
                0, 1, 2, 2, 3, 0,
                // Arka yüz
                4, 5, 6, 6, 7, 4,
                // Üst yüz
                8, 9, 10, 10, 11, 8,
                // Alt yüz
                12, 13, 14, 14, 15, 12,
                // Sağ yüz
                16, 17, 18, 18, 19, 16,
                // Sol yüz
                20, 21, 22, 22, 23, 20
        };

        return new Mesh(positions, colors, normals, indices);
    }
}
