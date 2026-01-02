package com.blockworld.world;

import com.blockworld.graphics.Camera;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Kameranın baktığı yönde ışın gönderip blok tespiti yapar.
 */
public class Raycaster {

    // Raycast adım boyutu
    private static final float STEP_SIZE = 0.1f;

    /**
     * Raycast sonucu
     */
    public static class RaycastResult {
        public Vector3i blockPos; // Vurulan bloğun pozisyonu
        public Vector3i faceNormal; // Vurulan yüzeyin normali
        public float distance; // Kameradan uzaklık

        public RaycastResult(Vector3i blockPos, Vector3i faceNormal, float distance) {
            this.blockPos = blockPos;
            this.faceNormal = faceNormal;
            this.distance = distance;
        }
    }

    /**
     * Kamera yönünde ışın gönderir ve ilk vurulan bloğu bulur.
     */
    public RaycastResult cast(Camera camera, World world, float maxDistance) {
        Vector3f rayOrigin = new Vector3f(camera.getPosition());
        Vector3f rayDir = new Vector3f(camera.getFront()).normalize();

        Vector3f currentPos = new Vector3f(rayOrigin);
        Vector3i lastEmptyBlock = null;

        float distance = 0;

        while (distance < maxDistance) {
            // Mevcut pozisyonu blok koordinatlarına çevir
            int blockX = (int) Math.floor(currentPos.x);
            int blockY = (int) Math.floor(currentPos.y);
            int blockZ = (int) Math.floor(currentPos.z);

            Vector3i blockPos = new Vector3i(blockX, blockY, blockZ);

            // Bu pozisyonda blok var mı?
            if (world.hasBlock(blockPos)) {
                // Yüzey normalini hesapla
                Vector3i faceNormal = calculateFaceNormal(lastEmptyBlock, blockPos);
                return new RaycastResult(blockPos, faceNormal, distance);
            }

            // Son boş bloğu kaydet (yüzey normali için)
            lastEmptyBlock = new Vector3i(blockX, blockY, blockZ);

            // Işını ilerlet
            currentPos.add(new Vector3f(rayDir).mul(STEP_SIZE));
            distance += STEP_SIZE;
        }

        return null; // Hiçbir bloğa çarpmadı
    }

    /**
     * Hangi yüzeyden bloğa girildiğini hesaplar.
     */
    private Vector3i calculateFaceNormal(Vector3i from, Vector3i to) {
        if (from == null) {
            return new Vector3i(0, 1, 0); // Varsayılan: yukarı
        }

        int dx = from.x - to.x;
        int dy = from.y - to.y;
        int dz = from.z - to.z;

        // En büyük farkı bul
        if (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) >= Math.abs(dz)) {
            return new Vector3i(dx > 0 ? 1 : -1, 0, 0);
        } else if (Math.abs(dy) >= Math.abs(dx) && Math.abs(dy) >= Math.abs(dz)) {
            return new Vector3i(0, dy > 0 ? 1 : -1, 0);
        } else {
            return new Vector3i(0, 0, dz > 0 ? 1 : -1);
        }
    }
}
