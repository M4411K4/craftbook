// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Mob spawner.
 *
 * @author sk89q
 */
public class MCX200 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MOB SPAWNER CLR";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {
        return true;
    }
    
    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign) {
        String id = sign.getLine3();
        String rider = sign.getLine4();

        if (id.length() == 0) {
            return "Specify a mob type on the third line.";
        }
        
        String[] args = id.split(":", 2);
        int color = getColor(args);
        
        if(color >= 0)
        	id = args[0];
        else if(color == -2)
        	return "Not a valid color value: " + args[1] + ".";
        
        String[] args2 = rider.split(":", 2);
        int colorRider = getColor(args2);
        if(colorRider >= 0)
        	rider = args2[0];
        else if(colorRider == -2)
        	return "Not a valid color value: " + args2[1] + ".";
        
        if (!isValidMob(id)) {
            return "Not a valid mob type: " + id + ".";
        } else if (rider.length() != 0 && !isValidMob(rider)) {
            return "Not a valid rider type: " + rider + ".";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (chip.getIn(1).is()) {
            String id = chip.getText().getLine3();
            String rider = chip.getText().getLine4();
            
            String[] args = id.split(":", 2);
            int color = getColor(args);
            
            if(color >= 0)
            	id = args[0];
            
            String[] args2 = rider.split(":", 2);
            int colorRider = getColor(args2);
            if(colorRider >= 0)
            	rider = args2[0];
            
            if (isValidMob(id)) {
                Vector pos = chip.getBlockPosition();
                int maxY = Math.min(128, pos.getBlockY() + 10);
                int x = pos.getBlockX();
                int z = pos.getBlockZ();

                for (int y = pos.getBlockY() + 1; y <= maxY; y++)
                {
                	int blockId = CraftBook.getBlockID(x, y, z);
                    if (BlockType.canPassThrough(blockId) || BlockType.isWater(blockId))
                    {
                        Location loc = new Location(x, y, z);
                        Mob mob = new Mob(id, loc);
                        if (rider.length() != 0 && isValidMob(rider)) {
                        	Mob mobRider = new Mob(rider);
                            mob.spawn(mobRider);
                            
                            if(colorRider >= 0)
                            	setSheep(mobRider.getEntity(), colorRider);
                            
                        } else {
                            mob.spawn();
                        }
                        
                        if(color >= 0)
                        	setSheep(mob.getEntity(), color);
                        
                        return;
                    }
                }
            }
        }
    }
    
    private int getColor(String[] args)
    {
    	int color;
    	
    	if(args.length < 2 || !args[0].equals("Sheep"))
    		return -1;
    	
    	try
    	{
    		color = Integer.parseInt(args[1]);
    	}
    	catch(NumberFormatException e)
    	{
    		return -2;
    	}
    	
    	if(color < 0 || color > 15)
    		return -2;
    	
    	return color;
    }
    
    private void setSheep(OEntityLiving entity, int color)
	{
		OEntitySheep sheep = (OEntitySheep)entity;
		sheep.a(color);
	}
    
    private boolean isValidMob(String mob)
    {
    	if (mob == null)
    		return false;
    	
    	OEntity entity = OEntityList.a(mob, etc.getMCServer().e);
    	
    	return (entity instanceof OEntityCreature) || (entity instanceof OIMobs);
    }
}
