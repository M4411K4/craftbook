package com.sk89q.craftbook;

public class BlockArea
{
	private final int X;
	private final int Y;
	private final int Z;
	private final int X2;
	private final int Y2;
	private final int Z2;
	
	private final CraftBookWorld CB_WORLD;
	
	public BlockArea(CraftBookWorld cbworld, int x, int y, int z, int x2, int y2, int z2)
	{
		if(cbworld == null)
		{
			throw new IllegalArgumentException("CraftBookWorld must not be null.");
		}
		
		this.CB_WORLD = cbworld;
		
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.X2 = x2;
		this.Y2 = y2;
		this.Z2 = z2;
	}
	
	public BlockArea add(int x, int y, int z)
	{
		return new BlockArea(CB_WORLD, X + x, Y + y, Z + z, X2 + x, Y2 + y, Z2 + z);
	}
	
	public boolean containsPoint(CraftBookWorld cbworld, int x, int y, int z)
	{
		if(!cbworld.equals(CB_WORLD)
			|| x < this.X
			|| x > this.X2
			|| y < this.Y
			|| y > this.Y2
			|| z < this.Z
			|| z > this.Z2
			)
		{
			return false;
		}
		
		return true;
	}
	
	public int getX()
	{
		return X;
	}
	
	public int getY()
	{
		return Y;
	}
	
	public int getZ()
	{
		return Z;
	}
	
	public int getX2()
	{
		return X2;
	}
	
	public int getY2()
	{
		return Y2;
	}
	
	public int getZ2()
	{
		return Z2;
	}
	
	public CraftBookWorld getCBWorld()
	{
		return CB_WORLD;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((CB_WORLD == null) ? 0 : CB_WORLD.hashCode());
		result = prime * result + X;
		result = prime * result + X2;
		result = prime * result + Y;
		result = prime * result + Y2;
		result = prime * result + Z;
		result = prime * result + Z2;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BlockArea))
			return false;
		BlockArea other = (BlockArea) obj;
		if (!CB_WORLD.equals(other.CB_WORLD))
			return false;
		if (X != other.X)
			return false;
		if (X2 != other.X2)
			return false;
		if (Y != other.Y)
			return false;
		if (Y2 != other.Y2)
			return false;
		if (Z != other.Z)
			return false;
		if (Z2 != other.Z2)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "(" + X + ", " + Y + ", " + Z + ") - (" + X2 + ", " + Y2 + ", " + Z2 + ")";
	}
}
