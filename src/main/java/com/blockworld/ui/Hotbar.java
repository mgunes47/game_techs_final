package com.blockworld.ui;

import com.blockworld.world.Block;

/**
 * Ekranın altındaki varlık seçim menüsü.
 */
public class Hotbar {

    private static final int SLOT_COUNT = 9;
    private static final float SLOT_SIZE = 50;
    private static final float SLOT_PADDING = 5;
    private static final float HOTBAR_PADDING = 10;

    private int selectedSlot;
    private HotbarItem[] slots;

    public Hotbar() {
        this.selectedSlot = 0;
        this.slots = new HotbarItem[SLOT_COUNT];

        // Varsayılan slotları doldur (Bloklar)
        slots[0] = new HotbarItem(Block.Type.GRASS);
        slots[1] = new HotbarItem(Block.Type.DIRT);
        slots[2] = new HotbarItem(Block.Type.STONE);
        slots[3] = new HotbarItem(Block.Type.WOOD);
        slots[4] = new HotbarItem(Block.Type.SAND);
        slots[5] = new HotbarItem(Block.Type.WATER);
        // Geri kalanlar null (boş)
    }

    public void render(UIRenderer uiRenderer) {
        int screenWidth = uiRenderer.getScreenWidth();
        int screenHeight = uiRenderer.getScreenHeight();

        // Hotbar boyutları
        float totalWidth = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_PADDING;
        float startX = (screenWidth - totalWidth) / 2;
        float startY = screenHeight - SLOT_SIZE - HOTBAR_PADDING;

        // Arka plan
        uiRenderer.drawRect(startX - HOTBAR_PADDING, startY - HOTBAR_PADDING,
                totalWidth + HOTBAR_PADDING * 2, SLOT_SIZE + HOTBAR_PADDING * 2,
                0.2f, 0.2f, 0.2f, 0.7f);

        // Her slot için
        for (int i = 0; i < SLOT_COUNT; i++) {
            float slotX = startX + i * (SLOT_SIZE + SLOT_PADDING);
            float slotY = startY;

            // Slot arka planı
            if (i == selectedSlot) {
                // Seçili slot
                uiRenderer.drawRect(slotX - 2, slotY - 2, SLOT_SIZE + 4, SLOT_SIZE + 4,
                        1.0f, 1.0f, 1.0f, 0.8f);
            }

            // Slot çerçevesi
            uiRenderer.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 0.3f, 0.3f, 0.3f, 0.8f);

            // Eski 2D renkli çizim kaldırıldı, artık 3D çizilecek
        }

        // Crosshair (nişangah) - ekran ortasında
        float crosshairSize = 20;
        float crosshairThickness = 2;
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;

        // Yatay çizgi
        uiRenderer.drawRect(centerX - crosshairSize / 2, centerY - crosshairThickness / 2,
                crosshairSize, crosshairThickness, 1.0f, 1.0f, 1.0f, 0.8f);
        // Dikey çizgi
        uiRenderer.drawRect(centerX - crosshairThickness / 2, centerY - crosshairSize / 2,
                crosshairThickness, crosshairSize, 1.0f, 1.0f, 1.0f, 0.8f);
    }

    public void render3DContents(com.blockworld.graphics.Renderer renderer, int screenWidth, int screenHeight) {
        float totalWidth = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_PADDING;
        float startX = (screenWidth - totalWidth) / 2;
        float startY = screenHeight - SLOT_SIZE - HOTBAR_PADDING;

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] != null) {
                float slotX = startX + i * (SLOT_SIZE + SLOT_PADDING);
                float slotY = startY;

                float innerPadding = 5;
                float size = SLOT_SIZE - innerPadding * 2;
                float x = slotX + innerPadding;

                // UIRenderer Y (üstten) -> Viewport Y (alttan) dönüşümü
                float glY = screenHeight - (slotY + innerPadding + size);

                if (slots[i].isStructure()) {
                    renderer.renderStructurePreview(slots[i].getStructure(), x, glY, size, screenWidth, screenHeight);
                } else {
                    renderer.renderBlockPreview(slots[i].getBlockType(), x, glY, size, screenWidth, screenHeight);
                }
            }
        }
    }

    public void selectSlot(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            this.selectedSlot = slot;
        }
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public HotbarItem getSelectedItem() {
        return slots[selectedSlot];
    }

    public void addItem(HotbarItem item) {
        // İlk boş slotu bul
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                // Eğer boş slot bulunduysa, o slotu seç (kullanıcı hemen görsün)
                this.selectedSlot = i;
                return;
            }
        }
        // Boş slot yoksa son slota koy
        slots[SLOT_COUNT - 1] = item;
        this.selectedSlot = SLOT_COUNT - 1;
    }

    public void setSlot(int index, HotbarItem item) {
        if (index >= 0 && index < SLOT_COUNT) {
            slots[index] = item;
        }
    }

    public HotbarItem[] getSlots() {
        return slots;
    }
}
