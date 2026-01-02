package com.blockworld.ui;

import com.blockworld.world.Block;
import com.blockworld.world.Structure;

/**
 * Hotbar slotunda tutulabilecek öğe.
 * Blok tipi veya bir yapı olabilir.
 */
public class HotbarItem {

    private Block.Type blockType;
    private Structure structure;
    private boolean isStructure;

    public HotbarItem(Block.Type type) {
        this.blockType = type;
        this.isStructure = false;
    }

    public HotbarItem(Structure structure) {
        this.structure = structure;
        this.isStructure = true;
    }

    public boolean isStructure() {
        return isStructure;
    }

    public Block.Type getBlockType() {
        return blockType;
    }

    public Structure getStructure() {
        return structure;
    }
}
