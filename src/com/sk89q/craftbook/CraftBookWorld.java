package com.sk89q.craftbook;

public class CraftBookWorld
{
	private final String NAME;
	private final int DIMENSION;
	
	public CraftBookWorld(String name, int dimension)
	{
		if(name == null)
		{
			throw new IllegalArgumentException("world name cannot be null.");
		}
		
		this.NAME = name;
		this.DIMENSION = dimension;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + DIMENSION;
		result = prime * result + ((NAME == null) ? 0 : NAME.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CraftBookWorld other = (CraftBookWorld) obj;
		if (DIMENSION != other.DIMENSION)
			return false;
		if (!NAME.equals(other.NAME))
			return false;
		return true;
	}
	
	public String name()
	{
		return NAME;
	}
	
	//The dimension ID and not the index #
	public int dimension()
	{
		return DIMENSION;
	}
}
