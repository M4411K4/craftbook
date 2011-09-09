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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCU131 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "HIT PLAYER ABV";
    public String getTitle() {
        return "^"+TITLE;
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
    	String id = sign.getLine3().toLowerCase();

        if (id.length() != 0)
        {
            if(id.length() < 3 || id.charAt(1) != ':' || (id.charAt(0) != 'g' && id.charAt(0) != 'p') )
            {
            	return "Invalid player or group name on Third line.";
            }
        }
        
        if (sign.getLine4().isEmpty()) {
            sign.setLine4("1:5:0");
        }
        else
        {
        	String[] param = sign.getLine4().split(":", 3);
        	try
        	{
        		int damage = Integer.parseInt(param[0]);
        		if(damage > 20 || damage < 1)
        			return "4th line damage value must be a number from 1 to 20";
        		
        		if(param.length > 1)
        		{
        			int delay = Integer.parseInt(param[1]);
        			if(delay < 1 || delay > 600)
        			{
        				return "4th line delay value must be a number from 1 to 600";
        			}
        			
        			sign.setLine4(damage+":"+delay+":0");
        		}
        		else
        		{
        			sign.setLine4(damage+":5:0");
        		}
        	}
        	catch(NumberFormatException e)
        	{
        		return "4th line values must be numbers.";
        	}
        }
        
        return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip)
    {
    	if(chip.inputAmount() == 0)
    	{
    		String[] param = chip.getText().getLine4().split(":", 3);
    		int delay = Integer.parseInt(param[1]);
    		int count = Integer.parseInt(param[2]);
    		
    		count++;
    		if(count < delay)
    		{
    			chip.getText().setLine4(param[0]+":"+param[1]+":"+count);
    			chip.getText().supressUpdate();
    			chip.getOut(1).set(false);
    			return;
    		}
    		
    		chip.getText().setLine4(param[0]+":"+param[1]+":0");
    		chip.getText().supressUpdate();
    		
    		boolean damaged = MCX131.damagePlayers(CraftBook.getWorld(chip.getWorldType()),
													chip.getBlockPosition().getBlockX(),
													chip.getBlockPosition().getBlockY(),
													chip.getBlockPosition().getBlockZ(),
													Integer.parseInt(param[0]),
													chip.getText().getLine3().toLowerCase()
													);
			
			chip.getOut(1).set(damaged);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			RedstoneListener listener = (RedstoneListener) chip.getExtra();
        		
        		String[] param = chip.getText().getLine4().split(":", 3);
    	        chip.getText().setLine4(param[0]+":"+param[1]+":0");
        		chip.getText().setLine1("%"+TITLE);
        		chip.getText().supressUpdate();
        		
        		listener.onSignAdded(CraftBook.getWorld(chip.getWorldType()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			chip.getText().setLine1("^"+TITLE);
    			chip.getText().supressUpdate();
    		}
    	}
    }
}
