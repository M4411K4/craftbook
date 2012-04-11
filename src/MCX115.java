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
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX115 extends MCX112 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "CBWARP PASS";
    }
    
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
    	
    	if(sign.getLine1().isEmpty())
    		return "Type password on the 1st line.";
    	
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a CBWarp name on the third line.";
        }
        
        if(!sign.getLine4().isEmpty())
        {
        	sign.setLine4("");
        }
        
        if(!CBWarp.isCorrectPassword(id, sign.getLine1()))
        {
        	return "incorrect CBWarp name or password";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
	@Override
    public void think(ChipState chip) {
        String id = chip.getText().getLine3();

        if (!id.isEmpty() && chip.getIn(1).is())
        {
        	CBWarpObject warp = CBWarp.getWarp(id, true);
        	
        	if(warp == null)
        		return;
        	
        	chip.getOut(1).set(transport(chip, warp.LOCATION, false, warp.getMessage()));
        	
        } else {
            chip.getOut(1).set(false);
        }
    }
}
