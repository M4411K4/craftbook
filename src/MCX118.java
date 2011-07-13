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



import java.util.List;

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX118 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PLAYER NEAR?";
    }
    
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
        String id = sign.getLine3().toLowerCase();

        if (id.length() != 0)
        {
            if(id.length() < 3 || id.charAt(1) != ':' || (id.charAt(0) != 'g' && id.charAt(0) != 'p') )
            {
            	return "Invalid player or group name on 3rd line.";
            }
        }
        
        if (sign.getLine4().length() != 0) {
            try
            {
            	double dist = Double.parseDouble(sign.getLine4());
            	if(dist < 1.0D || dist > 64.0D)
            		return "Range must be a number from 1 to 64";
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
    public void think(ChipState chip) {
    	
    	if(chip.inputAmount() == 0 || (chip.getIn(1).is() && chip.getIn(1).isTriggered()) )
    	{
    		chip.getOut(1).set( findEntitiesInRange(chip, etc.getServer().getPlayerList(), (byte)0) );
    	}
    }
    
    protected Boolean entityInRange(ChipState chip, BaseEntity entity, byte type)
    {
    	Player player = (Player) entity;
    	
    	String args = chip.getText().getLine3();
    	
    	return args.isEmpty()
    			|| (args.charAt(0) == 'g' && player.isInGroup(args.substring(2)))
    			|| (args.charAt(0) == 'p' && player.getName().equalsIgnoreCase(args.substring(2)));
    }
    
    @SuppressWarnings("rawtypes")
	protected boolean findEntitiesInRange(ChipState chip, List entities, byte type)
    {
    	int x = chip.getBlockPosition().getBlockX();
		int y = chip.getBlockPosition().getBlockY();
		int z = chip.getBlockPosition().getBlockZ();
		double dist = 5;
		if(!chip.getText().getLine4().isEmpty())
			dist = Double.parseDouble(chip.getText().getLine4());
		dist *= dist;
		
		boolean found = false;
		
		for(Object obj: entities)
		{
			BaseEntity entity = (BaseEntity)obj;
			if(entity.getWorld().getType().getId() != chip.getWorldType())
				continue;
			
			double diffX = x - entity.getX();
			double diffY = y - entity.getY();
			double diffZ = z - entity.getZ();
			
			if(diffX * diffX + diffY * diffY + diffZ * diffZ < dist)
			{
				Boolean result = entityInRange(chip, entity, type);
				if(result == null)
					found = true;
				else if(result == true)
					return true;
			}
		}
		
		return found;
    }
}
