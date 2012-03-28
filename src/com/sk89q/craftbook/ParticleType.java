package com.sk89q.craftbook;

import java.util.HashMap;
import java.util.Map;

public enum ParticleType
{
	//Keep length to 15 chars or less, to fit on signs
	SMOKE(2000),
	BLOCK(2001),
	SPLASH(2002),
	FLAMES(2004),
	;
	
	private static final Map<Integer, ParticleType> ref = new HashMap<Integer, ParticleType>(ParticleType.values().length);
	static
	{
		for(ParticleType type : ParticleType.values())
		{
			ref.put(type.getId(), type);
		}
	}
	
	private final int ID;
	public boolean allowed = true;
	
	private ParticleType(int id)
	{
		ID = id;
	}
	
	public int getId()
	{
		return ID;
	}
	
	public static ParticleType getParticle(String value)
	{
		if(value == null || value.isEmpty())
			return null;
		
		try
		{
			int id = Integer.parseInt(value);
			return getParticleFromId(id);
		}
		catch(NumberFormatException e)
		{
			return getParticleFromName(value);
		}
	}
	
	public static ParticleType getParticleFromName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		name = name.toUpperCase();
		name = name.replace(' ', '_');
		try
		{
			return ParticleType.valueOf(name);
		}
		catch(IllegalArgumentException e)
		{
			
		}
		return null;
	}
	
	public static ParticleType getParticleFromId(int id)
	{
		return ref.get(id);
	}
}
