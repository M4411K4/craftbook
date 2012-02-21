package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.Map;

public enum PotionType
{
	SPEED(1),
	SLOWNESS(2),
	HASTE(3),
	MINING_FATIGUE(4),
	STRENGTH(5),
	INSTANT_HEALTH(6),
	INSTANT_DAMAGE(7),
	JUMP_BOOST(8),
	NAUSEA(9),
	REGENERATION(10),
	RESISTANCE(11),
	FIRE_RESISTANCE(12),
	WATER_BREATHING(13),
	INVISIBILITY(14),
	BLINDNESS(15),
	NIGHT_VISION(16),
	HUNGER(17),
	WEAKNESS(18),
	POISON(19),
	;
	
	private static final Map<Integer, PotionType> ref = new HashMap<Integer, PotionType>(PotionType.values().length);
	static
	{
		for(PotionType type : PotionType.values())
		{
			ref.put(type.getId(), type);
		}
	}
	
	private final int ID;
	public boolean allowed = true;
	
	private PotionType(int id)
	{
		ID = id;
	}
	
	public int getId()
	{
		return ID;
	}
	
	public static PotionType getEffect(String value)
	{
		if(value == null || value.isEmpty())
			return null;
		
		try
		{
			int id = Integer.parseInt(value);
			return getEffectFromId(id);
		}
		catch(NumberFormatException e)
		{
			return getEffectFromName(value);
		}
	}
	
	public static PotionType getEffectFromName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		name = name.toUpperCase();
		name = name.replace(' ', '_');
		try
		{
			return PotionType.valueOf(name);
		}
		catch(IllegalArgumentException e)
		{
			
		}
		return null;
	}
	
	public static PotionType getEffectFromId(int id)
	{
		return ref.get(id);
	}
}
