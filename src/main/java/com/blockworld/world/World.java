package com.blockworld.world;

import com.blockworld.engine.Input;
import com.blockworld.graphics.Camera;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Blok dünyasını yöneten sınıf.
 */
public class World {

    // Blokları pozisyonlarına göre tutan harita
    private Map<Vector3i, Block> blocks;

    // Raycaster
    private Raycaster raycaster;

    // Seçili blok tipi
    private Block.Type selectedBlockType;

    // Yapı sistemi
    private SelectionBox selectionBox;
    private List<Structure> savedStructures;
    private Structure selectedStructure;

    // Dünya boyutları
    private static final int WORLD_SIZE = 16;

    // Tıklama bekleme süresi (spam engelleme)
    private float clickCooldown = 0;
    private static final float CLICK_DELAY = 0.2f;

    public World() {
        this.blocks = new HashMap<>();
        this.raycaster = new Raycaster();
        this.selectedBlockType = Block.Type.GRASS;
        this.selectionBox = new SelectionBox();
        this.savedStructures = new ArrayList<>();
        this.selectedStructure = null;
    }

    public void init() {
        generateFloor();
        System.out.println("Dünya oluşturuldu! Toplam blok: " + blocks.size());
    }

    /**
     * 16x16 düz zemin oluşturur.
     */
    private void generateFloor() {
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                // Zemin katmanı (y = 0)
                addBlock(x, 0, z, Block.Type.GRASS);
            }
        }
    }

    // Son kaydedilen yapıyı al ve listeyi temizle (GameLoop için)
    private Structure justSavedStructure = null;

    public void update(Input input, Camera camera) {
        // Cooldown güncelle
        if (clickCooldown > 0) {
            clickCooldown -= 1.0f / 60.0f;
        }

        // B tuşu - Seçim modunu aç/kapa
        if (input.isKeyPressed(GLFW_KEY_B)) {
            selectionBox.toggleSelectionMode();
        }

        // C tuşu - Seçili alanı yapı olarak kaydet (Sadece seçim varsa)
        if (input.isKeyPressed(GLFW_KEY_C) && selectionBox.hasSelection()) {
            saveSelection();
        }

        // Mouse yakalanmışsa blok etkileşimi
        if (input.isMouseCaptured() && clickCooldown <= 0) {
            Raycaster.RaycastResult hit = raycaster.cast(camera, this, 10.0f);

            if (hit != null) {
                // 1. Seçim Modu
                if (selectionBox.isSelecting()) {
                    if (input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                        selectionBox.addCorner(hit.blockPos);
                        clickCooldown = CLICK_DELAY;
                    }
                }
                // 2. Yapı Yerleştirme Modu (Hotbar'dan seçili yapı varsa)
                else if (selectedStructure != null) {
                    // Sağ Tık - Yapıyı yerleştir
                    if (input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
                        Vector3i placePos = new Vector3i(hit.blockPos).add(hit.faceNormal);
                        selectedStructure.placeInWorld(this, placePos.x, placePos.y, placePos.z);
                        System.out.println("Yapı yerleştirildi: " + placePos);
                        clickCooldown = CLICK_DELAY;
                    }
                    // Sol Tık - Blok kır (Normal mod gibi davran, böylece yanlış yerleşimi
                    // düzeltebilirsin)
                    if (input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                        removeBlock(hit.blockPos);
                        clickCooldown = CLICK_DELAY;
                    }
                }
                // 3. Blok Kırma/Koyma Modu (Hotbar'dan seçili blok varsa)
                else if (selectedBlockType != null) {
                    // Sol tık - blok kır
                    if (input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                        removeBlock(hit.blockPos);
                        clickCooldown = CLICK_DELAY;
                    }

                    // Sağ tık - blok yerleştir
                    if (input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
                        Vector3i newPos = new Vector3i(hit.blockPos).add(hit.faceNormal);
                        addBlock(newPos.x, newPos.y, newPos.z, selectedBlockType);
                        clickCooldown = CLICK_DELAY;
                    }
                }
            }
        }

        // NOT: 1-6 tuşları ve V tuşu artık GameLoop tarafından yönetiliyor.
    }

    private void saveSelection() {
        String name = "Structure_" + (savedStructures.size() + 1);
        Structure structure = Structure.createFromWorld(
                this,
                selectionBox.getCorner1(),
                selectionBox.getCorner2(),
                name);

        if (structure.getBlockCount() > 0) {
            savedStructures.add(structure);
            justSavedStructure = structure; // GameLoop'a bildir
            System.out.println("Yapı kaydedildi: " + name + " (" + structure.getBlockCount() + " blok)");
            System.out.println("Yapı Hotbar'a eklendi!");
        } else {
            System.out.println("Seçili alanda blok bulunamadı!");
        }

        selectionBox.cancelSelection();
    }

    public Structure consumeJustSavedStructure() {
        Structure s = justSavedStructure;
        justSavedStructure = null;
        return s;
    }

    public void addBlock(int x, int y, int z, Block.Type type) {
        Vector3i pos = new Vector3i(x, y, z);
        blocks.put(pos, new Block(type));
    }

    public void removeBlock(Vector3i pos) {
        blocks.remove(pos);
    }

    public Block getBlock(int x, int y, int z) {
        return blocks.get(new Vector3i(x, y, z));
    }

    public Block getBlock(Vector3i pos) {
        return blocks.get(pos);
    }

    public boolean hasBlock(int x, int y, int z) {
        return blocks.containsKey(new Vector3i(x, y, z));
    }

    public boolean hasBlock(Vector3i pos) {
        return blocks.containsKey(pos);
    }

    public Map<Vector3i, Block> getBlocks() {
        return blocks;
    }

    public Block.Type getSelectedBlockType() {
        return selectedBlockType;
    }

    public void setSelectedBlockType(Block.Type type) {
        this.selectedBlockType = type;
    }

    public List<Structure> getSavedStructures() {
        return savedStructures;
    }

    public void setSelectedStructure(Structure structure) {
        this.selectedStructure = structure;
    }

    public SelectionBox getSelectionBox() {
        return selectionBox;
    }

    public Structure getSelectedStructure() {
        return selectedStructure;
    }

    public void cleanup() {
        blocks.clear();
    }
}
