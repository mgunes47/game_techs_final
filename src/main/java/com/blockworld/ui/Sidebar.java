package com.blockworld.ui;

import com.blockworld.engine.Input;
import com.blockworld.world.Structure;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Sidebar {

    private boolean visible = false;
    private float width = 250;
    private float scrollY = 0;

    // Layout
    private static final float PADDING = 10;
    private static final float ITEM_SIZE = 50;
    private static final float ITEM_GAP = 10;
    private static final int COLS = 3;

    public void update(Input input, List<Structure> structures, Hotbar hotbar) {
        if (!visible)
            return;

        // Mouse tıklamarı
        if (input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            double mx = input.getMouseX();
            double my = input.getMouseY();

            // Eğer sidebar üzerine tıklandıysa
            if (mx > input.getWindowWidth() - width) {

                float startX = input.getWindowWidth() - width + PADDING;
                float startY = PADDING + scrollY;

                int index = 0;
                for (Structure structure : structures) {
                    int col = index % COLS;
                    int row = index / COLS;

                    float x = startX + col * (ITEM_SIZE + ITEM_GAP);
                    float y = startY + row * (ITEM_SIZE + ITEM_GAP);

                    // Tıklama kontrolü
                    if (mx >= x && mx <= x + ITEM_SIZE &&
                            my >= y && my <= y + ITEM_SIZE) {

                        // Yapıyı hotbar'a ekle (seçili slota veya boş slota)
                        hotbar.setSlot(hotbar.getSelectedSlot(), new HotbarItem(structure));
                        System.out.println("Sidebar'dan seçildi: " + structure.getName());
                    }

                    index++;
                }
            }
        }

        // Scroll logic eklenebilir.
    }

    public void render(UIRenderer uiRenderer, List<Structure> structures, int screenWidth, int screenHeight) {
        if (!visible)
            return;

        // Arkaplan
        uiRenderer.drawRect(screenWidth - width, 0, width, screenHeight, 0.1f, 0.1f, 0.1f, 0.9f);

        float startX = screenWidth - width + PADDING;
        float startY = PADDING + scrollY;

        int index = 0;
        for (Structure structure : structures) {
            int col = index % COLS;
            int row = index / COLS;

            float x = startX + col * (ITEM_SIZE + ITEM_GAP);
            float y = startY + row * (ITEM_SIZE + ITEM_GAP);

            // Kutucuk arkaplanı
            uiRenderer.drawRect(x, y, ITEM_SIZE, ITEM_SIZE, 0.3f, 0.3f, 0.3f, 1.0f);

            index++;
        }
    }

    public void render3DContents(com.blockworld.graphics.Renderer renderer, List<Structure> structures, int screenWidth,
            int screenHeight) {
        if (!visible)
            return;

        float startX = screenWidth - width + PADDING;
        float startY = PADDING + scrollY;

        int index = 0;
        for (Structure structure : structures) {
            int col = index % COLS;
            int row = index / COLS;

            float x = startX + col * (ITEM_SIZE + ITEM_GAP);
            float y = startY + row * (ITEM_SIZE + ITEM_GAP);

            float innerPadding = 5;
            float size = ITEM_SIZE - innerPadding * 2;

            // Y ekseni (Üstten) -> Viewport Y (Alttan)
            float glX = x + innerPadding;
            float glY = screenHeight - (y + innerPadding + size);

            renderer.renderStructurePreview(structure, glX, glY, size, screenWidth, screenHeight);

            index++;
        }
    }

    public void toggle() {
        this.visible = !this.visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getWidth() {
        return width;
    }
}
