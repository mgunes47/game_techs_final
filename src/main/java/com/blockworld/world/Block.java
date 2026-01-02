package com.blockworld.world;

/**
 * Tek bir blok temsil eder.
 */
public class Block {

    /**
     * Blok tipleri
     */
    public enum Type {
        GRASS("Çimen", 0.2f, 0.8f, 0.2f),
        DIRT("Toprak", 0.55f, 0.35f, 0.15f),
        STONE("Taş", 0.5f, 0.5f, 0.5f),
        WOOD("Ahşap", 0.6f, 0.4f, 0.2f),
        SAND("Kum", 0.9f, 0.85f, 0.6f),
        WATER("Su", 0.2f, 0.4f, 0.9f);

        private final String displayName;
        private final float r, g, b;

        Type(String displayName, float r, float g, float b) {
            this.displayName = displayName;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }
    }

    private Type type;

    public Block(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
