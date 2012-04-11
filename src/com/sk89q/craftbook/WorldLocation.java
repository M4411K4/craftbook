// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

/**
 * Extension of Vector that supports being compared as ints (for accuracy).
 *
 * @author sk89q
 */
public class WorldLocation extends Vector {
	
	protected final CraftBookWorld cbworld;
	
	protected final float rotation;
	protected final float pitch;
	
    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldLocation(CraftBookWorld cbworld, Vector pt, float rotation, float pitch) {
        super(pt);
        if(cbworld == null)
        {
        	throw new IllegalArgumentException("CraftBookWorld cannot be null.");
        }
        this.cbworld = cbworld;
        
        this.rotation = rotation;
        this.pitch = pitch;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldLocation(CraftBookWorld cbworld, int x, int y, int z, float rotation, float pitch) {
        super(x, y, z);
        if(cbworld == null)
        {
        	throw new IllegalArgumentException("CraftBookWorld cannot be null.");
        }
        this.cbworld = cbworld;

        this.rotation = rotation;
        this.pitch = pitch;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldLocation(CraftBookWorld cbworld, float x, float y, float z, float rotation, float pitch) {
        super(x, y, z);
        if(cbworld == null)
        {
        	throw new IllegalArgumentException("CraftBookWorld cannot be null.");
        }
        this.cbworld = cbworld;

        this.rotation = rotation;
        this.pitch = pitch;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public WorldLocation(CraftBookWorld cbworld, double x, double y, double z, float rotation, float pitch) {
        super(x, y, z);
        if(cbworld == null)
        {
        	throw new IllegalArgumentException("CraftBookWorld cannot be null.");
        }
        this.cbworld = cbworld;

        this.rotation = rotation;
        this.pitch = pitch;
    }
    
    public Vector getCoordinate()
    {
    	return super.add(0, 0, 0);
    }
    
    
    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    @Override
    public WorldLocation setX(double x) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    @Override
    public WorldLocation setX(int x) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    @Override
    public WorldLocation setY(double y) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }

    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    @Override
    public WorldLocation setY(int y) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    @Override
    public WorldLocation setZ(double z) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    @Override
    public WorldLocation setZ(int z) {
        return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    public WorldLocation setRotation(float rotation)
    {
    	return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    public WorldLocation setPitch(float pitch)
    {
    	return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    public WorldLocation setCBWorld(CraftBookWorld cbworld)
    {
    	return new WorldLocation(cbworld, x, y, z, rotation, pitch);
    }
    
    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    @Override
    public WorldLocation add(Vector other) {
        return new WorldLocation(cbworld,
        						super.add(other),
        						rotation,
        						pitch
        						);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    @Override
    public WorldLocation add(double x, double y, double z) {
    	return new WorldLocation(cbworld,
				    			this.x + x,
								this.y + y,
								this.z + z,
								rotation,
								pitch
								);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    @Override
    public WorldLocation add(int x, int y, int z) {
    	return new WorldLocation(cbworld,
								this.x + x,
								this.y + y,
								this.z + z,
								rotation,
								pitch
								);
    }
    
    public WorldLocation add(double x, double y, double z, float rotation, float pitch)
    {
    	return new WorldLocation(cbworld,
				    			this.x + x,
								this.y + y,
								this.z + z,
    							this.rotation + rotation,
    							this.pitch + pitch
    							);
    }
    
    public WorldLocation add(int x, int y, int z, float rotation, float pitch)
    {
    	return new WorldLocation(cbworld,
				    			this.x + x,
								this.y + y,
								this.z + z,
    							this.rotation + rotation,
    							this.pitch + pitch
    							);
    }
    
    
    public CraftBookWorld getCBWorld()
    {
    	return cbworld;
    }
    
    public float rotation()
    {
    	return rotation;
    }
    
    public float pitch()
    {
    	return pitch;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cbworld == null) ? 0 : cbworld.hashCode());
		result = prime * result + Float.floatToIntBits(pitch);
		result = prime * result + Float.floatToIntBits(rotation);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof WorldLocation))
			return false;
		WorldLocation other = (WorldLocation) obj;
		if (!cbworld.equals(other.cbworld))
			return false;
		if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch))
			return false;
		if (Float.floatToIntBits(rotation) != Float
				.floatToIntBits(other.rotation))
			return false;
		return true;
	}
}
