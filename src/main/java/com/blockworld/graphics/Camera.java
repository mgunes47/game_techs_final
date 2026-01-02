package com.blockworld.graphics;

import com.blockworld.engine.Input;
import com.blockworld.engine.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * 3D kamera - Perspektif projeksiyon ve hareket kontrolü.
 */
public class Camera {

    // Kamera pozisyonu
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;

    // Euler açıları
    private float yaw; // Y ekseni etrafında dönüş (sağ-sol)
    private float pitch; // X ekseni etrafında dönüş (yukarı-aşağı)

    // Matrisler
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    // Hareket parametreleri
    private float moveSpeed = 8.0f;
    private float mouseSensitivity = 0.15f;

    // Projeksiyon parametreleri
    private float fov = 70.0f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000.0f;

    public Camera() {
        // Kamerayı zeminin üstünde ve geri planda başlat
        position = new Vector3f(8.0f, 3.0f, 20.0f);
        front = new Vector3f(0.0f, 0.0f, -1.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(1.0f, 0.0f, 0.0f);

        yaw = -90.0f; // -Z yönüne bak
        pitch = -15.0f; // Hafif aşağı bak

        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public void init(Window window) {
        updateProjection(window);
        updateCameraVectors();
    }

    public void updateProjection(Window window) {
        float aspectRatio = window.getAspectRatio();
        projectionMatrix.identity();
        projectionMatrix.perspective(
                (float) Math.toRadians(fov),
                aspectRatio,
                nearPlane,
                farPlane);
    }

    public void update(Input input, float deltaTime) {
        // Mouse ile bakış
        if (input.isMouseCaptured()) {
            float xOffset = (float) input.getDeltaX() * mouseSensitivity;
            float yOffset = (float) -input.getDeltaY() * mouseSensitivity;

            yaw += xOffset;
            pitch += yOffset;

            // Pitch sınırları (dik yukarı/aşağı bakma engeli)
            if (pitch > 89.0f)
                pitch = 89.0f;
            if (pitch < -89.0f)
                pitch = -89.0f;

            updateCameraVectors();
        }

        // WASD ile hareket - YERDEKİ HAREKET (y ekseni sabit)
        float velocity = moveSpeed * deltaTime;

        // Yatay hareket vektörü (y = 0 ile)
        Vector3f frontFlat = new Vector3f(front.x, 0, front.z).normalize();
        Vector3f rightFlat = new Vector3f(right.x, 0, right.z).normalize();

        if (input.isKeyDown(GLFW_KEY_W)) {
            position.add(new Vector3f(frontFlat).mul(velocity));
        }
        if (input.isKeyDown(GLFW_KEY_S)) {
            position.sub(new Vector3f(frontFlat).mul(velocity));
        }
        if (input.isKeyDown(GLFW_KEY_A)) {
            position.sub(new Vector3f(rightFlat).mul(velocity));
        }
        if (input.isKeyDown(GLFW_KEY_D)) {
            position.add(new Vector3f(rightFlat).mul(velocity));
        }

        // Yukarı/Aşağı hareket (Space/Shift) - dikey hareket
        if (input.isKeyDown(GLFW_KEY_SPACE)) {
            position.y += velocity;
        }
        if (input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            position.y -= velocity;
        }

        // Y pozisyonunun minimum değeri (zeminin altına düşmesin)
        if (position.y < 1.5f) {
            position.y = 1.5f;
        }
    }

    private void updateCameraVectors() {
        // Front vektörünü hesapla
        float x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float y = (float) Math.sin(Math.toRadians(pitch));
        float z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        front = new Vector3f(x, y, z).normalize();

        // Right ve Up vektörlerini yeniden hesapla
        right = new Vector3f(front).cross(new Vector3f(0, 1, 0)).normalize();
        up = new Vector3f(right).cross(front).normalize();
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.identity();
        Vector3f target = new Vector3f(position).add(front);
        viewMatrix.lookAt(position, target, up);
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getRight() {
        return right;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
