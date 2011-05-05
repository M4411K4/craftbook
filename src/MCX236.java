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
import java.util.Map;

import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sets the server time to day or night, repeats the signal.
 *
 * @author Shaun (sturmeh)
 */
public class MCX236 extends BaseIC {
	
	/**
     * Data store.
     */
    private static Map<Player,Boolean> players =
            new HistoryHashMap<Player,Boolean>(100);
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "DIST FAKE RAIN";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {
        return true;
    }

    public String validateEnvironment(Vector pos, SignText sign) {
    	if (sign.getLine3().length() == 0) {
    		return "Please put a distance on the third line.";
        }
    	
    	try
    	{
    		int dist = Integer.parseInt(sign.getLine3());
    		if(dist < 1 || dist > 127)
    			return "Distance must be a number from 1 to 127";
    	}
    	catch(NumberFormatException e)
    	{
    		return "Third line must be a number";
    	}
        
        return null;
    }
    
    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
    	
    	if(chip.inputAmount() == 0 || chip.getIn(1).is())
    	{
	    	Vector pos = chip.getBlockPosition();
	    	int dist = Integer.parseInt(chip.getText().getLine3());
	    	dist = dist * dist;
	    	
	    	for(Player player: etc.getServer().getPlayerList())
	        {
	    		Location pLoc = player.getLocation();
	    		Vector diff = pos.subtract(pLoc.x, pLoc.y, pLoc.z);
	    		
	    		Boolean exists = players.get(player);
	    		
	    		if(diff.getX() * diff.getX() + diff.getY() * diff.getY() + diff.getZ() * diff.getZ() < dist)
	    		{
	    			if(exists == null)
	    			{
	    				players.put(player, true);
	    				player.getEntity().a.b(new OPacket70Bed(1));
	    				if(chip.getText().getLine4().length() > 0)
	    					player.sendMessage(chip.getText().getLine4());
	    			}
	    		}
	    		else if(exists != null)
	    		{
	    			players.remove(player);
	    			if(!etc.getMCServer().e.q().l())
	    				player.getEntity().a.b(new OPacket70Bed(2));
	    		}
	        }
    	}
    	else if(!chip.getIn(1).is())
    	{
    		Iterator<Map.Entry<Player, Boolean>> it = players.entrySet().iterator();
    	    while (it.hasNext())
    	    {
				Map.Entry<Player, Boolean> item = (Map.Entry<Player, Boolean>)it.next();
				if(!etc.getMCServer().e.q().l())
					item.getKey().getEntity().a.b(new OPacket70Bed(2));
				it.remove();
    	    }
    	}
    }
}
