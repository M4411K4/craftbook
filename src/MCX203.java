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

import java.util.Iterator;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX203 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "CHEST COLLECTOR";
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
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() > 0) {
        	String[] args = id.split(":", 2);
            int color = getColor(args);
            
            if(color >= 0)
            	id = args[0];
            else if(color == -2)
            	return "Not a valid color/damage value: " + args[1] + ".";
            
            if (getItem(id) < 1) {
                return "Not a valid item type: " + sign.getLine3() + ".";
            }
        }
        
        if (sign.getLine4().length() > 0)
        {
        	try
        	{
        		double dist = Double.parseDouble(sign.getLine4());
        		if(dist < 1.0D || dist > 64.0D)
        			return "4th line must be a number from 1 to 64.";
        	}
        	catch(NumberFormatException e)
        	{
        		return "4th line must be a number from 1 to 64.";
        	}
        	
        }

        return null;
    }
    
    /**
     * Get an item from its name or ID.
     * 
     * @param id
     * @return
     */
    protected int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }
    
    protected int getColor(String[] args)
    {
    	int color;
    	
    	if(args.length < 2)
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
    

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (chip.inputAmount() != 0 && !chip.getIn(1).is()) {
            return;
        }
        
        NearbyChestBlockBag source = new NearbyChestBlockBag(chip.getWorldType(), chip.getPosition());
        source.addSourcePosition(chip.getWorldType(), chip.getPosition());
        
        String id = chip.getText().getLine3();
        
        int item = -1;
        int color = -1;
        if(id.length() > 0)
        {
	        String[] args = id.split(":", 2);
	        color = getColor(args);
	        
	        if(color >= 0)
	        	id = args[0];
	        else if(color < -1)
	        	color = -1;
	        
	        item = getItem(id);
        }
        
        double dist = 16.0D;
        if(chip.getText().getLine4().length() > 0)
        {
        	dist = Double.parseDouble(chip.getText().getLine4());
        }
        dist = dist * dist;
        
        OWorldServer oworld = CraftBook.getOWorldServer(chip.getWorldType());
        
        double x = chip.getPosition().getX();
        double y = chip.getPosition().getY();
        double z = chip.getPosition().getZ();
        
        boolean found = false;
		for(@SuppressWarnings("rawtypes")
		Iterator it = oworld.b.iterator(); it.hasNext();)
		{
			Object obj = it.next();
			if(obj instanceof OEntityItem)
			{
				OEntityItem eitem = (OEntityItem) obj;
				
				if(!eitem.bh && (item == -1 || (eitem.a.c == item && (color < 0 || eitem.a.h() == color) )))
				{
    				double diffX = x - eitem.aP;
    				double diffY = y - eitem.aQ;
    				double diffZ = z - eitem.aR;
    				
    				if(((diffX * diffX + diffY * diffY + diffZ * diffZ) < dist)
    					&& source.hasAvailableSlotSpace(eitem.a.c, (byte)eitem.a.h()))
    				{
    					found = true;
    					
    					//kill
    					eitem.J();
    					
    					//store
    					try {
                            source.storeBlock(eitem.a.c, (byte)eitem.a.h());
                        } catch (BlockSourceException e) {
                            break;
                        }
    				}
				}
			}
		}
    	
    	chip.getOut(1).set(found);
    }
}
