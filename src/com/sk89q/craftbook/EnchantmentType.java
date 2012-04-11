package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.Map;

public enum EnchantmentType
{
	//Keep length to 15 chars or less, to fit on signs
	PROTECTION(0),
	FIRE_P(1),
	FEATHER_FALL(2),
	BLAST_P(3),
	PROJECTILE_P(4),
	RESPIRATION(5),
	AQUA_AFFINITY(6),
	
	SHARPNESS(16),
	SMITE(17),
	BANE_ARTHROPOD(18),
	BANE_OF_ARTHRO(18),
	KNOCKBACK(19),
	FIRE_ASPECT(20),
	LOOTING(21),
	
	EFFICIENCY(32),
	SILK_TOUCH(33),
	UNBREAKING(34),
	FORTUNE(35),
	
	POWER(48),
	PUNCH(49),
	FLAME(50),
	INFINITY(51),
	;
	
	private static final Map<Integer, EnchantmentType> ref = new HashMap<Integer, EnchantmentType>(EnchantmentType.values().length);
	static
	{
		for(EnchantmentType type : EnchantmentType.values())
		{
			ref.put(type.getId(), type);
		}
	}
	
	private final int ID;
	public boolean allowed = true;
	
	private EnchantmentType(int id)
	{
		ID = id;
	}
	
	public int getId()
	{
		return ID;
	}
	
	public static EnchantmentType getEnchantment(String value)
	{
		if(value == null || value.isEmpty())
			return null;
		
		try
		{
			int id = Integer.parseInt(value);
			return getEnchantmentFromId(id);
		}
		catch(NumberFormatException e)
		{
			return getEnchantmentFromName(value);
		}
	}
	
	public static EnchantmentType getEnchantmentFromName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		name = name.toUpperCase();
		name = name.replace(' ', '_');
		try
		{
			return EnchantmentType.valueOf(name);
		}
		catch(IllegalArgumentException e)
		{
			
		}
		return null;
	}
	
	public static EnchantmentType getEnchantmentFromId(int id)
	{
		return ref.get(id);
	}
}
