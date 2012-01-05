package com.sk89q.craftbook;

public class CraftBookItem
{
	private final int ID;
	private final int COLOR;
	
	public CraftBookItem(int id, int color)
	{
		ID = id;
		COLOR = color;
	}
	
	public int id()
	{
		return ID;
	}
	
	public int color()
	{
		return COLOR;
	}
	
	public int safeColor()
	{
		if(COLOR < 0)
			return 0;
		if(COLOR > 15)
			return 15;
		return COLOR;
	}
	
	@Override
	public int hashCode()
	{
		return (ID << 4) | (COLOR & 0xF);
	}
	
	@Override
    public boolean equals(Object obj)
    {
    	if(obj == null || obj.getClass() != getClass())
    		return false;
    	if(obj == this)
    		return true;
    	
    	CraftBookItem item = (CraftBookItem)obj;
    	return (   item.id() == id()
    			&& item.color() == color()
    			);
    }
	
	@Override
	public String toString()
	{
		return "["+ID+"@"+COLOR+"]";
	}
}
