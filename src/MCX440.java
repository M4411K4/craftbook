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
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

public class MCX440 extends BaseIC {
	
	private final String TITLE = "S-DOWN COUNT";
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
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
    public String validateEnvironment(Vector pos, SignText sign) {
        if (sign.getLine3().length() == 0)
        {
            return "Specify counter configuration on line 3.";
        }
        
        String[] param = sign.getLine3().split(":", 2);
        
        int clockTime;
        int downCount;
        if(param.length == 1)
        {
        	clockTime = 5;
        }
        else
        {
        	try
        	{
        		clockTime = Integer.parseInt(param[1]);
        	}
        	catch(NumberFormatException e)
        	{
        		return "Clock rate is not a number.";
        	}
        }
        
        try
        {
        	downCount = Integer.parseInt(param[0]);
        }
        catch(NumberFormatException e)
        {
        	return "Count value is not a number.";
        }
        
        if(downCount < 1 || downCount > 99999)
        {
        	return "Count value must be a number from 1 to 99999.";
        }
        
        if(clockTime < 5 || clockTime > 15)
        {
        	return "Clock rate must be a number from 5 to 15.";
        }

        if (sign.getLine4().length() != 0)
        {
            return "The fourth line must be empty.";
        }
        
        sign.setLine3(downCount+":"+clockTime+":0");

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
	    	String[] param = chip.getText().getLine3().split(":", 3);
	        
	        int downCount = Integer.parseInt(param[0]);
	        int clockTime = Integer.parseInt(param[1]);
	        int curCount = downCount;
	        
	        if(param.length > 2)
	        	curCount = Integer.parseInt(param[2]);
	        
	        int count = chip.getText().getLine4().length();
	        if(count % clockTime == clockTime-1)
	        {
	        	curCount--;
	        	
	        	if(curCount <= 0)
	        	{
	        		//since self-updates don't get mode, we need to get it
	        		char mode = ' ';
	        		if(chip.getText().getLine2().length() > 8)
	        			mode = chip.getText().getLine2().charAt(8);
	        		
	        		if(curCount < 0 && mode == '1')
	        		{
	        			chip.getOut(1).set(false);
	        		}
	        		else
	        		{
	        			chip.getOut(1).set(true);
	        		}
	        		
	        		if(curCount != 0 || mode != '1')
	        		{
	        			chip.getText().setLine1("^"+TITLE);
	        		}
	        		
	        		curCount = 0;
	        	}
	        	else
	        	{
	        		chip.getOut(1).set(false);
	        	}
	        	
	        	chip.getText().setLine3(downCount+":"+clockTime+":"+curCount);
	        	
	            chip.getText().setLine4("");
	        }
	        else
	        {
	        	chip.getText().setLine4(chip.getText().getLine4()+" ");
	        }
	        
	        chip.getText().supressUpdate();
    	}
    	else
    	{
    		if(!chip.getIn(1).is() || !chip.getIn(1).isTriggered() || chip.getText().getLine1().charAt(0) == '%')
    			return;
    		
    		RedstoneListener listener = (RedstoneListener) chip.getExtra();
    		
    		String[] param = chip.getText().getLine3().split(":", 3);
	        
	        int downCount = Integer.parseInt(param[0]);
	        int clockTime = Integer.parseInt(param[1]);
	        int curCount = downCount;
    		
    		chip.getText().setLine3(downCount+":"+clockTime+":"+curCount);
    		
    		chip.getText().setLine1("%"+TITLE);
    		chip.getText().setLine4("");
    		
    		chip.getText().supressUpdate();
    		
    		listener.onSignAdded(chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    	}
    }
}