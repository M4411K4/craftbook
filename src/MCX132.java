// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.util.List;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.*;


public class MCX132 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "HIT MOB ABOVE";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
	@Override
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
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	String id = sign.getLine3();

        if (id.length() != 0)
        {
        	if(!id.equalsIgnoreCase("mob") && !id.equalsIgnoreCase("mobs")
            	&& !id.equalsIgnoreCase("animal") && !id.equalsIgnoreCase("animals")
            	&& !Mob.isValid(id))
            {
            	return "Invalid mob name or type on 3rd line.";
            }
        }
        
        if (sign.getLine4().isEmpty()) {
            sign.setLine4("1");
        }
        else
        {
        	try
        	{
        		int damage = Integer.parseInt(sign.getLine4());
        		if(damage > 20 || damage < 1)
        			return "4th line damage value must be a number from 1 to 20";
        	}
        	catch(NumberFormatException e)
        	{
        		return "4th line must be a number.";
        	}
        }
        
        return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
	@Override
    public void think(ChipState chip)
    {
    	if(!chip.getIn(1).is() || !chip.getIn(1).isTriggered())
    		return;
    	
    	String id = chip.getText().getLine3();
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	@SuppressWarnings("rawtypes")
		List list;
    	if(id.equalsIgnoreCase("mob") || id.equalsIgnoreCase("mobs"))
    		list = world.getMobList();
		else if(id.equalsIgnoreCase("animal") || id.equalsIgnoreCase("animals"))
			list = world.getAnimalList();
		else
			list = world.getLivingEntityList();
    	
    	boolean damaged = MCX131.damageEntities(list,
    											world,
												chip.getBlockPosition().getBlockX(),
												chip.getBlockPosition().getBlockY(),
												chip.getBlockPosition().getBlockZ(),
												Integer.parseInt(chip.getText().getLine4()),
												chip.getText().getLine3()
												);
    	
    	chip.getOut(1).set(damaged);
    }
}
