package com.sk89q.craftbook;

import java.util.Arrays;

public class CraftBookItem
{
	private final int ID;
	private final int COLOR;
	private final CraftBookEnchantment[] ENCHANTMENTS;
	
	public CraftBookItem(int id, int color)
	{
		this(id, color, null);
	}
	
	public CraftBookItem(int id, int color, CraftBookEnchantment[] enchantments)
	{
		ID = id;
		COLOR = color;
		ENCHANTMENTS = enchantments;
	}
	
	public CraftBookItem setID(int id)
	{
		return new CraftBookItem(id, COLOR, ENCHANTMENTS);
	}
	
	public CraftBookItem setColor(int color)
	{
		return new CraftBookItem(ID, color, ENCHANTMENTS);
	}
	
	public CraftBookItem setEnchantments(CraftBookEnchantment[] enchantments)
	{
		return new CraftBookItem(ID, COLOR, enchantments);
	}
	
	public int id()
	{
		return ID;
	}
	
	public int color()
	{
		return COLOR;
	}
	
	public CraftBookEnchantment[] enchantments()
	{
		return ENCHANTMENTS;
	}
	
	public CraftBookEnchantment enchantment(int index)
	{
		if(ENCHANTMENTS == null || index < 0 || index >= ENCHANTMENTS.length)
			return null;
		
		return ENCHANTMENTS[index];
	}
	
	public int safeColor()
	{
		if(COLOR < 0)
			return 0;
		if(COLOR > 15)
			return 15;
		return COLOR;
	}
	
	public boolean hasEnchantments()
	{
		return ENCHANTMENTS != null && ENCHANTMENTS.length > 0;
	}
	
	public boolean allEnchantsAllowed()
	{
		if(ENCHANTMENTS == null)
			return true;
		
		for(CraftBookEnchantment cbenchant : ENCHANTMENTS)
		{
			if(!cbenchant.enchantment().allowed)
				return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + COLOR;
		result = prime * result + Arrays.hashCode(ENCHANTMENTS);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CraftBookItem))
			return false;
		CraftBookItem other = (CraftBookItem) obj;
		if (ID != other.ID)
			return false;
		if (COLOR != other.COLOR)
			return false;
		if (!Arrays.equals(ENCHANTMENTS, other.ENCHANTMENTS))
			return false;
		return true;
	}
	
	@Override
	public CraftBookItem clone()
	{
		return new CraftBookItem(ID, COLOR, ENCHANTMENTS);
	}

	@Override
	public String toString()
	{
		if(ENCHANTMENTS == null)
			return "["+ID+"@"+COLOR+"]";
		String out = "["+ID+"@"+COLOR;
		for(CraftBookEnchantment enchantment : ENCHANTMENTS)
		{
			out += "&"+enchantment.toString();
		}
		return out+"]";
	}
}
