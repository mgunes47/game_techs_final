package com.blockworld.engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Klavye ve mouse input yönetimi.
 */
public class Input {

    private long windowHandle;

    // Klavye durumları
    private boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];

    // Mouse durumları
    private boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private boolean[] mouseButtonsPressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

    // Mouse pozisyonu ve delta
    private double mouseX, mouseY;
    private double lastMouseX, lastMouseY;
    private double deltaX, deltaY;
    private boolean firstMouse = true;

    // Mouse yakalama durumu
    private boolean mouseCaptured = false;

    // Scroll durumu
    private double scrollX, scrollY;
    private double scrollDeltaX, scrollDeltaY;

    public void init(Window window) {
        this.windowHandle = window.getWindowHandle();

        // Klavye callback
        glfwSetKeyCallback(windowHandle, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                if (action == GLFW_PRESS) {
                    keys[key] = true;
                    keysPressed[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
        });

        // Mouse button callback
        glfwSetMouseButtonCallback(windowHandle, (win, button, action, mods) -> {
            if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                if (action == GLFW_PRESS) {
                    mouseButtons[button] = true;
                    mouseButtonsPressed[button] = true;

                    // İlk tıklamada mouse'u yakala
                    if (!mouseCaptured) {
                        captureMouse(true);
                    }
                } else if (action == GLFW_RELEASE) {
                    mouseButtons[button] = false;
                }
            }
        });

        // Mouse pozisyon callback
        glfwSetCursorPosCallback(windowHandle, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        // Scroll callback
        glfwSetScrollCallback(windowHandle, (win, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
        });

        // Başlangıçta mouse pozisyonunu al
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(windowHandle, xpos, ypos);
        mouseX = lastMouseX = xpos[0];
        mouseY = lastMouseY = ypos[0];
    }

    public void update() {
        // Delta hesapla
        if (firstMouse) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstMouse = false;
        }

        deltaX = mouseX - lastMouseX;
        deltaY = mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        // Scroll delta'yı güncelle ve birikimi sıfırla
        scrollDeltaX = scrollX;
        scrollDeltaY = scrollY;
        scrollX = 0;
        scrollY = 0;

        // Pressed durumlarını sıfırla (tek frame'lik)
        for (int i = 0; i < keysPressed.length; i++) {
            keysPressed[i] = false;
        }
        for (int i = 0; i < mouseButtonsPressed.length; i++) {
            mouseButtonsPressed[i] = false;
        }
    }

    public void captureMouse(boolean capture) {
        mouseCaptured = capture;
        if (capture) {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
        firstMouse = true;
    }

    // Klavye sorgulama
    public boolean isKeyDown(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keys[key];
    }

    public boolean isKeyPressed(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keysPressed[key];
    }

    // Mouse sorgulama
    public boolean isMouseButtonDown(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST && mouseButtons[button];
    }

    public boolean isMouseButtonPressed(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST && mouseButtonsPressed[button];
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }

    public double getScrollDeltaX() {
        return scrollDeltaX;
    }

    public double getScrollDeltaY() {
        return scrollDeltaY;
    }

    public int getWindowWidth() {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(windowHandle, w, h);
        return w[0];
    }
}
