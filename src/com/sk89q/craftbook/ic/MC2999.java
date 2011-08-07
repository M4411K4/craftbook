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

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;

public class MC2999 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MARQUEE";
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
        if (!sign.getLine3().isEmpty()) {
            return "3rd line must be blank.";
        }
        if (!sign.getLine4().isEmpty()) {
            return "4th line must be blank.";
        }

        return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (!chip.getIn(1).is())
            return;

        int pin = 1;
        if(chip.getMode() == 'r')
        	pin = 3;

        try {
            pin = Integer.parseInt(chip.getText().getLine3());
        } catch (Exception e) {
        }

        chip.getOut(1).set(false);
        chip.getOut(2).set(false);
        chip.getOut(3).set(false);
        
        //since chip output order is different, translate
        switch(pin)
        {
        	case 1:
        		chip.getOut(2).set(true);
        		break;
        	case 2:
        		chip.getOut(1).set(true);
        		break;
        	case 3:
        		chip.getOut(3).set(true);
        		break;
        }

        if(chip.getMode() == 'r')
        	pin--;
        else
        	pin++;
        
        if (pin == 4)
            pin = 1;
        else if(pin == 0)
        	pin = 3;

        chip.getText().setLine3(Integer.toString(pin));
    }
}
