package com.sk89q.craftbook;

public class WorldBlockVector extends BlockVector
{
	protected final int worldType;
	
	/**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(int worldType, Vector pt) {
        super(pt);
        this.worldType = worldType;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(int worldType, int x, int y, int z) {
        super(x, y, z);
        this.worldType = worldType;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(int worldType, float x, float y, float z) {
        super(x, y, z);
        this.worldType = worldType;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(int worldType, double x, double y, double z) {
        super(x, y, z);
        this.worldType = worldType;
    }

    public int getWorldType()
    {
    	return worldType;
    }
    
    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldBlockVector)) {
            return false;
        }
        WorldBlockVector other = (WorldBlockVector)obj;
        return (int)Math.floor(other.x) == (int)Math.floor(this.x)
                && (int)Math.floor(other.y) == (int)Math.floor(this.y)
                && (int)Math.floor(other.z) == (int)Math.floor(this.z)
                && other.worldType == this.worldType;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return (Integer.valueOf(worldType).hashCode() >> 13) ^
               (Integer.valueOf((int)x).hashCode() >> 12) ^
               (Integer.valueOf((int)y).hashCode() >> 6) ^
                Integer.valueOf((int)z).hashCode();
    }
}
