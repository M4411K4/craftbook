package com.sk89q.craftbook;

public class BlockArea
{
	private final int X;
	private final int Y;
	private final int Z;
	private final int X2;
	private final int Y2;
	private final int Z2;
	
	private final int WORLD_TYPE;
	
	public BlockArea(int worldType, int x, int y, int z, int x2, int y2, int z2)
	{
		this.WORLD_TYPE = worldType;
		
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.X2 = x2;
		this.Y2 = y2;
		this.Z2 = z2;
	}
	
	public BlockArea add(int x, int y, int z)
	{
		return new BlockArea(WORLD_TYPE, X + x, Y + y, Z + z, X2 + x, Y2 + y, Z2 + z);
	}
	
	public boolean containsPoint(int worldType, int x, int y, int z)
	{
		if(worldType != WORLD_TYPE
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
	
	public int getWorldType()
	{
		return WORLD_TYPE;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof BlockArea))
			return false;
		
		BlockArea area = (BlockArea)obj;
		
		if(area.getWorldType() != this.WORLD_TYPE
			|| area.getX() != this.X
			|| area.getX2() != this.X2
			|| area.getY() != this.Y
			|| area.getY2() != this.Y2
			|| area.getZ() != this.Z
			|| area.getZ2() != this.Z2
			)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return "(" + X + ", " + Y + ", " + Z + ") - (" + X2 + ", " + Y2 + ", " + Z2 + ")";
	}
}
