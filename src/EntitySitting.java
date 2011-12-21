
/*
 * Minecraft Sitting Mod
 * Copyright (C) 2011  M4411K4
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class EntitySitting extends OEntityFishHook
{
	private final double OFFSET_Y;
	private final int BLOCK_ID;
	private final SitType[] TYPES;
	private int ticks = 0;
	
	public EntitySitting(SitType[] types, OWorld oworld, double x, double y, double z, double offsety)
	{
		super(oworld);
		c(x, y, z);
		
		int nullcount = 0;
		for(SitType type : types)
		{
			if(type == null)
				nullcount++;
		}
		if(nullcount > 0)
		{
			SitType[] newtypes = new SitType[types.length - nullcount];
			int j = 0;
			for(int i = 0; i < types.length; i++)
			{
				if(types[i] == null)
					continue;
				newtypes[j] = types[i];
				j++;
			}
			TYPES = newtypes;
		}
		else
		{
			TYPES = types;
		}
		
		OFFSET_Y = offsety;
		
		int blockX = OMathHelper.b(this.bj);
		int blockY = OMathHelper.b(this.bk);
		int blockZ = OMathHelper.b(this.bl);
		
		BLOCK_ID = oworld.a(blockX, blockY, blockZ);
		
		if(!canSitOnBlock(oworld, blockX, blockY, blockZ))
			this.S();
	}
	
	public static boolean isChairBlock(int id)
	{
		switch(id)
		{
			case 53:
			case 67:
			case 108:
			case 109:
			case 114:
				return true;
		}
		return false;
	}
	
	public static boolean canSitOnBlock(OWorld oworld, int x, int y, int z)
	{
		int id = oworld.a(x, y, z);
		
		if(id == 0)
		{
			id = oworld.a(x, y-1, z);
			if(id == 0)
				return false;
		}
		
		if(id == 34 //piston ext
			|| id == 36 //block moved
			|| id == 52 //mob spawner
			|| id == 65 //ladder
			|| id == 85 //fence
			|| id == 101 //iron bars
			|| id == 102 //glass pane
			|| id == 107 //fence gate
			|| id == 113 //nether brick fence
			|| OBlock.m[id].e(oworld, x, y, z) == null)
			return false;
		return true;
	}
	
	@Override
	//onUpdate
	public void w_()
	{
		if(this.bd != null && this.bd.bB)
		{
			//sitting player is dead
			this.bd = null;
		}
		
		int x = OMathHelper.b(this.bj);
		int y = OMathHelper.b(this.bk);
		int z = OMathHelper.b(this.bl);
		
		if(this.bd == null || this.bf.a(x, y, z) != BLOCK_ID || !canSitOnBlock(this.bf, x, y, z))
		{
			//dismounted
			this.S();
			return;
		}
		
		ticks++;
		if(ticks >= 1200)
		{
			EntitySitting esitting = new EntitySitting(this.TYPES, this.bf, this.bj, this.bk, this.bl, this.OFFSET_Y);
			this.bf.b(esitting);
			this.bd.b(esitting);
			this.S();
			return;
		}
		
		if(this.bd instanceof OEntityPlayerMP)
		{
			OEntityPlayerMP eplayer = (OEntityPlayerMP)this.bd;
			for(SitType type : TYPES)
			{
				type.update(this.bf, this, eplayer);
			}
		}
		
		this.bm = 0.0D;
		this.bn = 0.0D;
		this.bo = 0.0D;
		this.bd.bm = 0.0D;
		this.bd.bn = 0.0D;
		this.bd.bo = 0.0D;
		a(0.0D, 0.0D, 0.0D);
	}
	
	@Override
	//getMountedYOffset
	public double q()
	{
		return OFFSET_Y;
	}
	
	@Override
	protected boolean g_()
	{
		return false;
	}
	
	@Override
	//attackEntityFrom
	public boolean a(ODamageSource paramODamageSource, int paramInt)
	{
		return false;
	}
	
	public void b(ONBTTagCompound paramONBTTagCompound)
	{
		
	}
	
	public void a(ONBTTagCompound paramONBTTagCompound)
	{
		
	}
}