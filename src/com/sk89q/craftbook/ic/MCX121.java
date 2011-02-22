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

package com.sk89q.craftbook.ic;

import java.util.Map;

import com.sk89q.craftbook.*;

/**
 * Positive edge-triggered wireless receiver.
 *
 * @author sk89q
 */
public class MCX121 extends BaseIC {
	
	private final String TITLE = "PASS-COMMAND";
	
	/**
     * Data store.
     */
    public static Map<String,Boolean> airwaves =
            new HistoryHashMap<String,Boolean>(100);
    
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return TITLE;
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

        if (id.length() == 0) {
            return "Specify a password reference on the third line.";
        }
        
        //check if id is valid name? I'll leave it for now, so admins can
        // have more control.

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        String id = chip.getText().getLine3();
        if (!id.isEmpty())
        {
            Boolean out = MCX121.airwaves.get(id);
            if (out == null)
            {
            	out = chip.getText().getLine1().charAt(0) == 'P';
            	
            	airwaves.put(id, out);
            }
            else
            {
            	if(out && chip.getText().getLine1().charAt(0) == '-')
        		{
        			chip.getText().setLine1(TITLE);
        			chip.getText().supressUpdate();
        		}
        		else if(!out && chip.getText().getLine1().charAt(0) == 'P')
        		{
        			chip.getText().setLine1("-"+TITLE);
        			chip.getText().supressUpdate();
        		}
            }
            
            chip.getOut(1).set(out);
        }
        else
        {
            chip.getOut(1).set(false);
        }
    }
}
