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



import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX133 extends MCX119 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "HUMANS ONLY";
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
    	if(!sign.getLine3().isEmpty())
    	{
    		sign.setLine3("");
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
    		double dist = 5;
    		if(!chip.getText().getLine4().isEmpty())
    			dist = Double.parseDouble(chip.getText().getLine4());
    		dist *= dist;
    		Vector lever = Util.getWallSignBack(chip.getWorldType(), chip.getPosition(), 2);
    		World world = CraftBook.getWorld(chip.getWorldType());
    		
    		//since self-updates don't get mode, we need to get it
    		char mode = ' ';
    		if(chip.getText().getLine2().length() > 8)
    			mode = chip.getText().getLine2().charAt(8);
    		
    		int type = 4;
    		if(mode == '-')
    			type = 5;
    		
        	NearbyEntityFinder nearbyFinder = new NearbyEntityFinder(world, chip.getBlockPosition(), lever, dist, "", type, true);
        	etc.getServer().addToServerQueue(nearbyFinder);
    	}
    }
}
