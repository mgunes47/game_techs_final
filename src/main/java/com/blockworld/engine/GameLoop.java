package com.blockworld.engine;

import com.blockworld.graphics.Camera;
import com.blockworld.graphics.Renderer;
import com.blockworld.ui.Hotbar;
import com.blockworld.ui.HotbarItem;
import com.blockworld.ui.Sidebar;
import com.blockworld.ui.UIRenderer;
import com.blockworld.world.Structure;
import com.blockworld.world.World;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Ana oyun döngüsü - Update ve Render işlemlerini yönetir.
 */
public class GameLoop {

    private static final float TARGET_FPS = 60.0f;
    private static final float TARGET_UPS = 60.0f; // Updates per second

    private final Window window;
    private final Input input;
    private final Camera camera;
    private final Renderer renderer;
    private final World world;
    private final UIRenderer uiRenderer;
    private final Hotbar hotbar;
    private final Sidebar sidebar;

    private boolean running;

    public GameLoop(Window window) {
        this.window = window;
        this.input = new Input();
        this.camera = new Camera();
        this.renderer = new Renderer();
        this.world = new World();
        this.uiRenderer = new UIRenderer();
        this.hotbar = new Hotbar();
        this.sidebar = new Sidebar();
        this.running = false;
    }

    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    private void init() {
        // Pencereyi başlat
        window.init();

        // Input sistemini başlat
        input.init(window);

        // Kamerayı başlat
        camera.init(window);

        // Renderer'ı başlat
        renderer.init();

        // UI Renderer'ı başlat
        uiRenderer.init(window.getWidth(), window.getHeight());

        // Dünyayı başlat (zemin oluştur)
        world.init();

        running = true;
        System.out.println("Oyun başlatıldı!");
        System.out.println("Kontroller:");
        System.out.println("  WASD - Hareket");
        System.out.println("  Mouse - Bakış yönü");
        System.out.println("  Sol Tık - Blok kır");
        System.out.println("  Sağ Tık - Blok yerleştir");
        System.out.println("  1-9 / Scroll - Slot seç");
        System.out.println("  B - Seçim modunu aç/kapa");
        System.out.println("  C - Seçili alanı kaydet (Hotbar'a eklenir)");
        System.out.println("  E - Yapı Menüsünü (Sidebar) Aç/Kapa");
        System.out.println("  Space/Shift - Yukarı/Aşağı");
        System.out.println("  ESC - Çıkış");
    }

    private void loop() {
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 / TARGET_UPS;
        double delta = 0;

        long lastFpsTime = System.currentTimeMillis();
        int frames = 0;

        while (running && !window.shouldClose()) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            // Sabit aralıklarla update
            while (delta >= 1) {
                update((float) (1.0 / TARGET_UPS));
                delta--;
            }

            // Render
            render();

            // Input durumunu sıfırla (Bir sonraki poll için)
            input.update();

            // FPS sayacı
            frames++;
            if (System.currentTimeMillis() - lastFpsTime >= 1000) {
                // System.out.println("FPS: " + frames);
                frames = 0;
                lastFpsTime = System.currentTimeMillis();
            }

            // Pencereyi güncelle (Poll Events burada yapılır)
            window.update();
        }
    }

    private void update(float deltaTime) {
        // E Tuşu - Envanter/Sidebar Aç/Kapa
        if (input.isKeyPressed(GLFW_KEY_E)) {
            sidebar.toggle();
            // Mouse capture durumunu değiştir
            input.captureMouse(!sidebar.isVisible());
        }

        // Eğer sidebar açıksa oyuna müdahale etme, sadece sidebar'ı güncelle
        if (sidebar.isVisible()) {
            sidebar.update(input, world.getSavedStructures(), hotbar);
            return;
        }

        // Camera ve Dünya update (Sadece sidebar kapalıyken)
        camera.update(input, deltaTime);

        // Hotbar kontrolü (slot değişimi)
        handleHotbarInput();

        // Seçili slotu dünyaya bildir
        updateWorldSelection();

        // Dünya güncellemesi (blok ekleme/silme, yapı kaydetme)
        world.update(input, camera);

        // Eğer yeni yapı kaydedildiyse hotbar'a ekle
        Structure justSaved = world.consumeJustSavedStructure();
        if (justSaved != null) {
            hotbar.addItem(new HotbarItem(justSaved));
            // Seçimi güncelle
            updateWorldSelection();
        }
    }

    private void handleHotbarInput() {
        boolean slotChanged = false;

        // 1-9 tuşları
        for (int i = 0; i < 9; i++) {
            if (input.isKeyPressed(GLFW_KEY_1 + i)) {
                hotbar.selectSlot(i);
                slotChanged = true;
            }
        }

        // Mouse scroll ile slot değişimi
        double scrollY = input.getScrollDeltaY();
        if (scrollY != 0) {
            int currentSlot = hotbar.getSelectedSlot();
            if (scrollY > 0) {
                // Scroll up - önceki slot
                currentSlot--;
                if (currentSlot < 0)
                    currentSlot = 8;
            } else {
                // Scroll down - sonraki slot
                currentSlot++;
                if (currentSlot > 8)
                    currentSlot = 0;
            }
            hotbar.selectSlot(currentSlot);
            slotChanged = true;
        }

        if (slotChanged) {
            HotbarItem item = hotbar.getSelectedItem();
            if (item != null) {
                if (item.isStructure()) {
                    System.out.println("Seçildi: Yapı - " + item.getStructure().getName());
                } else {
                    System.out.println("Seçildi: Blok - " + item.getBlockType());
                }
            } else {
                System.out.println("Boş slot seçildi");
            }
        }
    }

    private void updateWorldSelection() {
        HotbarItem item = hotbar.getSelectedItem();
        if (item != null) {
            if (item.isStructure()) {
                world.setSelectedStructure(item.getStructure());
                world.setSelectedBlockType(null); // Blok seçimini kaldır
            } else {
                world.setSelectedBlockType(item.getBlockType());
                world.setSelectedStructure(null); // Yapı seçimini kaldır
            }
        } else {
            world.setSelectedBlockType(null);
            world.setSelectedStructure(null);
        }
    }

    private void render() {
        // Viewport'u güncelle
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            camera.updateProjection(window);
            uiRenderer.updateProjection(window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Ekranı temizle
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // 3D dünyayı render et
        renderer.render(camera, world);

        // UI render et
        uiRenderer.beginRender();
        hotbar.render(uiRenderer);

        // Sidebar'ı en son çiz (üstte kalsın)
        if (sidebar.isVisible()) {
            sidebar.render(uiRenderer, world.getSavedStructures(), window.getWidth(), window.getHeight());
        }

        uiRenderer.endRender();

        // UI üzerine 3D Önizlemeleri çiz
        hotbar.render3DContents(renderer, window.getWidth(), window.getHeight());
        if (sidebar.isVisible()) {
            sidebar.render3DContents(renderer, world.getSavedStructures(), window.getWidth(), window.getHeight());
        }
    }

    private void cleanup() {
        System.out.println("Oyun kapatılıyor...");
        uiRenderer.cleanup();
        renderer.cleanup();
        world.cleanup();
        window.cleanup();
    }
}
