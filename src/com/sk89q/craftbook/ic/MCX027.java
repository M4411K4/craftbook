package com.sk89q.craftbook.ic;


import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
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


/**
 *
 * @author Shaun (sturmeh)
 */
public class MCX027 extends BaseIC {
	
	private static final int MAX_TIME = 24000;
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "BETWEEN TIME";
    }
    
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
        if(sign.getLine3().isEmpty())
        {
        	return "Line 3 must have a number from 0 to "+MAX_TIME;
        }
        
        try
        {
        	int time = Integer.parseInt(sign.getLine3());
        	if(time < 0 || time > MAX_TIME)
        	{
        		return "Line 3 must have a number from 0 to "+MAX_TIME;
        	}
        }
        catch(NumberFormatException e)
        {
        	return "Line 3 must have a number from 0 to "+MAX_TIME;
        }
        
        if(!sign.getLine4().isEmpty())
        {
        	try
            {
            	int time = Integer.parseInt(sign.getLine4());
            	if(time < 0 || time > MAX_TIME)
            	{
            		return "Line 4 must have a number from 0 to "+MAX_TIME;
            	}
            }
            catch(NumberFormatException e)
            {
            	return "Line 4 must have a number from 0 to "+MAX_TIME;
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
        if(chip.inputAmount() == 0 || chip.getIn(1).is())
        {
        	int time = Integer.parseInt(chip.getText().getLine3());
        	int timeEnd = MAX_TIME;
        	if(!chip.getText().getLine4().isEmpty())
        		timeEnd = Integer.parseInt(chip.getText().getLine4());
        	
        	long worldTime = chip.getTime() % 24000;
        	if(time <= timeEnd)
        	{
        		chip.getOut(1).set(worldTime >= time && worldTime <= timeEnd);
        	}
        	else
        	{
        		chip.getOut(1).set(worldTime >= time || worldTime <= timeEnd);
        	}
        }
    }
}
