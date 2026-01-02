package com.blockworld.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * GLFW pencere yönetimi ve OpenGL context oluşturma.
 */
public class Window {

    private long windowHandle;
    private int width;
    private int height;
    private String title;
    private boolean resized;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.resized = false;
    }

    public void init() {
        // GLFW hata callback'i
        GLFWErrorCallback.createPrint(System.err).set();

        // GLFW'yi başlat
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW başlatılamadı!");
        }

        // Pencere ayarları
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Pencereyi oluştur
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("GLFW penceresi oluşturulamadı!");
        }

        // Resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> {
            this.width = w;
            this.height = h;
            this.resized = true;
        });

        // ESC tuşu ile çıkış
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        // Pencereyi ekranın ortasına konumlandır
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }

        // OpenGL context'i aktif et
        glfwMakeContextCurrent(windowHandle);

        // VSync aktif
        glfwSwapInterval(1);

        // Pencereyi göster
        glfwShowWindow(windowHandle);

        // OpenGL capabilities
        GL.createCapabilities();

        // Depth testing aktif
        glEnable(GL_DEPTH_TEST);

        // Face culling (performans için)
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Arka plan rengi (gökyüzü mavisi)
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        System.out.println("OpenGL Version: " + glGetString(GL_VERSION));
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public float getAspectRatio() {
        return (float) width / (float) height;
    }
}
