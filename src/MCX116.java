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
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX116 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PLAYER ABOVE?";
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
            	return "Invalid player or group name on Third line.";
            }
        }
        
        if (sign.getLine4().length() != 0) {
            return "Fourth line must be blank.";
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
    		World world = CraftBook.getWorld(chip.getWorldType());
    		chip.getOut(1).set(playerAbove(world, chip.getBlockPosition(), chip.getText().getLine3()) != null);
    	}
    }
    
    protected Player playerAbove(World world, Vector pos, String id)
    {
    	id = id.toLowerCase();
    	
    	int x = pos.getBlockX();
        int z = pos.getBlockZ();
        
        int y = getSafeY(world, pos);
    	
    	for(Player player: etc.getServer().getPlayerList())
        {
        	Location pLoc = player.getLocation();
        	Vector pVec = new Vector(pLoc.x, pLoc.y, pLoc.z);
        	
        	if( (pVec.getBlockX() == x || pVec.getBlockX() == x + 1 || pVec.getBlockX() == x - 1) &&
        			pVec.getBlockY() == y &&
        			(pVec.getBlockZ() == z || pVec.getBlockZ() == z + 1 || pVec.getBlockZ() == z - 1)
        		)
        	{
        		if(!id.isEmpty())
        		{
        			if( (id.charAt(0) == 'g' && player.isInGroup(id.substring(2))) ||
        					(id.charAt(0) == 'p' && player.getName().equalsIgnoreCase(id.substring(2))) )
        			{
        				return player;
        			}
        			else
        			{
        				//continue to check if another player happens to be in the same area instead of stopping
        				continue;
        			}
        		}
        		else
        		{
        			return player;
        		}
        	}
        }
    	
    	return null;
    }
    
    private int getSafeY(World world, Vector pos)
    {
    	int maxY = Math.min(128, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
    	
    	for (int y = pos.getBlockY() + 1; y <= maxY; y++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)) &&
            	y < 128 && BlockType.canPassThrough(CraftBook.getBlockID(world, x, y+1, z))	
            	)
            {
            	return y;
            }
		}
    	
    	return maxY;
    }
}
