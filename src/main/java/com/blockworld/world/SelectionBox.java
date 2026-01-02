package com.blockworld.world;

import org.joml.Vector3i;

/**
 * Yapı seçimi için kullanılan kutu aracı.
 */
public class SelectionBox {

    private Vector3i corner1;
    private Vector3i corner2;
    private boolean isSelecting;
    private boolean hasSelection;

    public SelectionBox() {
        this.corner1 = null;
        this.corner2 = null;
        this.isSelecting = false;
        this.hasSelection = false;
    }

    /**
     * Seçim modunu başlat/bitir.
     */
    public void toggleSelectionMode() {
        if (isSelecting) {
            // Seçimi iptal et
            cancelSelection();
        } else {
            // Seçim modunu başlat
            isSelecting = true;
            corner1 = null;
            corner2 = null;
            hasSelection = false;
            System.out.println("Seçim modu aktif! İlk köşeyi belirlemek için tıklayın.");
        }
    }

    /**
     * Bir köşe noktası ekle.
     */
    public void addCorner(Vector3i pos) {
        if (!isSelecting)
            return;

        if (corner1 == null) {
            corner1 = new Vector3i(pos);
            System.out.println("İlk köşe belirlendi: " + corner1);
            System.out.println("İkinci köşeyi belirlemek için tıklayın.");
        } else if (corner2 == null) {
            corner2 = new Vector3i(pos);
            hasSelection = true;
            isSelecting = false;
            System.out.println("İkinci köşe belirlendi: " + corner2);
            System.out.println("Seçim tamamlandı! 'C' tuşuna basarak yapıyı kopyalayabilirsiniz.");
        }
    }

    /**
     * Seçimi iptal et.
     */
    public void cancelSelection() {
        corner1 = null;
        corner2 = null;
        isSelecting = false;
        hasSelection = false;
        System.out.println("Seçim iptal edildi.");
    }

    /**
     * Seçili alanın minimum köşesi.
     */
    public Vector3i getMinCorner() {
        if (!hasSelection)
            return null;
        return new Vector3i(
                Math.min(corner1.x, corner2.x),
                Math.min(corner1.y, corner2.y),
                Math.min(corner1.z, corner2.z));
    }

    /**
     * Seçili alanın maksimum köşesi.
     */
    public Vector3i getMaxCorner() {
        if (!hasSelection)
            return null;
        return new Vector3i(
                Math.max(corner1.x, corner2.x),
                Math.max(corner1.y, corner2.y),
                Math.max(corner1.z, corner2.z));
    }

    public Vector3i getMin() {
        if (corner1 != null && corner2 != null) {
            return new Vector3i(
                    Math.min(corner1.x, corner2.x),
                    Math.min(corner1.y, corner2.y),
                    Math.min(corner1.z, corner2.z));
        } else if (corner1 != null) {
            return new Vector3i(corner1);
        }
        return null;
    }

    public Vector3i getMax() {
        if (corner1 != null && corner2 != null) {
            return new Vector3i(
                    Math.max(corner1.x, corner2.x),
                    Math.max(corner1.y, corner2.y),
                    Math.max(corner1.z, corner2.z));
        } else if (corner1 != null) {
            return new Vector3i(corner1);
        }
        return null;
    }

    public Vector3i getCorner1() {
        return corner1;
    }

    public Vector3i getCorner2() {
        return corner2;
    }

    public boolean isSelecting() {
        return isSelecting;
    }

    public boolean hasSelection() {
        return hasSelection;
    }
}
