package com.sk89q.craftbook;

public class CraftBookEnchantment
{
	private final EnchantmentType ENCHANTMENT;
	private final int LEVEL;
	
	public CraftBookEnchantment(EnchantmentType enchantment, int level)
	{
		if(enchantment == null)
		{
			throw new IllegalArgumentException("EnchantmentType must not be null.");
		}
		
		ENCHANTMENT = enchantment;
		LEVEL = level;
	}
	
	public CraftBookEnchantment setEnchantments(EnchantmentType enchantment)
	{
		return new CraftBookEnchantment(enchantment, LEVEL);
	}
	
	public CraftBookEnchantment setLevel(int level)
	{
		return new CraftBookEnchantment(ENCHANTMENT, level);
	}
	
	public EnchantmentType enchantment()
	{
		return ENCHANTMENT;
	}
	
	public int level()
	{
		return LEVEL;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ENCHANTMENT == null) ? 0 : ENCHANTMENT.hashCode());
		result = prime * result + LEVEL;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CraftBookEnchantment))
			return false;
		CraftBookEnchantment other = (CraftBookEnchantment) obj;
		if (ENCHANTMENT != other.ENCHANTMENT)
			return false;
		if (LEVEL != other.LEVEL)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "["+ENCHANTMENT.name()+"#"+LEVEL+"]";
	}
}
