package com.blockworld.world;

import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Kaydedilmiş bir yapı (birden fazla blok).
 */
public class Structure {

    private String name;
    private List<StructureBlock> blocks;

    /**
     * Yapı içindeki tek bir blok.
     */
    public static class StructureBlock {
        public int offsetX, offsetY, offsetZ;
        public Block.Type type;

        public StructureBlock(int offsetX, int offsetY, int offsetZ, Block.Type type) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.type = type;
        }
    }

    public Structure(String name) {
        this.name = name;
        this.blocks = new ArrayList<>();
    }

    /**
     * Dünyadan belirli bir alandaki blokları yapı olarak kaydet.
     */
    public static Structure createFromWorld(World world, Vector3i corner1, Vector3i corner2, String name) {
        Structure structure = new Structure(name);

        int minX = Math.min(corner1.x, corner2.x);
        int minY = Math.min(corner1.y, corner2.y);
        int minZ = Math.min(corner1.z, corner2.z);
        int maxX = Math.max(corner1.x, corner2.x);
        int maxY = Math.max(corner1.y, corner2.y);
        int maxZ = Math.max(corner1.z, corner2.z);

        // Alandaki tüm blokları tara
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null) {
                        // Offset olarak kaydet (minX, minY, minZ'ye göre)
                        structure.addBlock(x - minX, y - minY, z - minZ, block.getType());
                    }
                }
            }
        }

        return structure;
    }

    public void addBlock(int offsetX, int offsetY, int offsetZ, Block.Type type) {
        blocks.add(new StructureBlock(offsetX, offsetY, offsetZ, type));
    }

    /**
     * Yapıyı dünyaya yerleştir.
     */
    public void placeInWorld(World world, int baseX, int baseY, int baseZ) {
        for (StructureBlock block : blocks) {
            world.addBlock(
                    baseX + block.offsetX,
                    baseY + block.offsetY,
                    baseZ + block.offsetZ,
                    block.type);
        }
    }

    public String getName() {
        return name;
    }

    public List<StructureBlock> getBlocks() {
        return blocks;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    /**
     * Yapının boyutlarını hesapla.
     */
    public Vector3i getSize() {
        if (blocks.isEmpty())
            return new Vector3i(0, 0, 0);

        int maxX = 0, maxY = 0, maxZ = 0;
        for (StructureBlock block : blocks) {
            maxX = Math.max(maxX, block.offsetX);
            maxY = Math.max(maxY, block.offsetY);
            maxZ = Math.max(maxZ, block.offsetZ);
        }
        return new Vector3i(maxX + 1, maxY + 1, maxZ + 1);
    }
}
