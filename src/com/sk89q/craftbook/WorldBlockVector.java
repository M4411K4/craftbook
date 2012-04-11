package com.sk89q.craftbook;

public class WorldBlockVector extends BlockVector
{
	protected final CraftBookWorld cbworld;
	
	/**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(CraftBookWorld cbworld, Vector pt) {
        super(pt);
        this.cbworld = cbworld;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(CraftBookWorld cbworld, int x, int y, int z) {
        super(x, y, z);
        this.cbworld = cbworld;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(CraftBookWorld cbworld, float x, float y, float z) {
        super(x, y, z);
        this.cbworld = cbworld;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldBlockVector(CraftBookWorld cbworld, double x, double y, double z) {
        super(x, y, z);
        this.cbworld = cbworld;
    }

    public CraftBookWorld getCBWorld()
    {
    	return cbworld;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cbworld == null) ? 0 : cbworld.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof WorldBlockVector))
			return false;
		WorldBlockVector other = (WorldBlockVector) obj;
		if (cbworld == null) {
			if (other.cbworld != null)
				return false;
		} else if (!cbworld.equals(other.cbworld))
			return false;
		return true;
	}
}
