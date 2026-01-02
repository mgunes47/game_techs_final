package com.blockworld;

import com.blockworld.engine.GameLoop;
import com.blockworld.engine.Window;

/**
 * 3D Blok Dünyası - Ana Giriş Noktası
 * 
 * Minecraft benzeri bir blok dünyası oyunu.
 * LWJGL (OpenGL) kullanarak 3D grafikler render edilir.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("3D Blok Dünyası Başlatılıyor...");

        try {
            Window window = new Window("3D Blok Dünyası", 1280, 720);
            GameLoop gameLoop = new GameLoop(window);
            gameLoop.run();
        } catch (Exception e) {
            System.err.println("Oyun başlatılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
