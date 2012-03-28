package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.Map;

public enum SFXType
{
	//Keep length to 15 chars or less, to fit on signs
	CLICK(1000),
	CLICK2(1001),
	BOW(1002),
	DOOR(1003),
	FIZZ(1004),
	MUSIC(1005),
	GHAST_CHARGE(1007),
	GHAST_FIREBALL(1008),
	ZOMBIE_WOOD(1010),
	ZOMBIE_METAL(1011),
	ZOMBIE_BREAK(1012),
	;
	
	private static final Map<Integer, SFXType> ref = new HashMap<Integer, SFXType>(SFXType.values().length);
	static
	{
		for(SFXType type : SFXType.values())
		{
			ref.put(type.getId(), type);
		}
	}
	
	private final int ID;
	public boolean allowed = true;
	
	private SFXType(int id)
	{
		ID = id;
	}
	
	public int getId()
	{
		return ID;
	}
	
	public static SFXType getEffect(String value)
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
	
	public static SFXType getEffectFromName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		name = name.toUpperCase();
		name = name.replace(' ', '_');
		try
		{
			return SFXType.valueOf(name);
		}
		catch(IllegalArgumentException e)
		{
			
		}
		return null;
	}
	
	public static SFXType getEffectFromId(int id)
	{
		return ref.get(id);
	}
}
