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
import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX212 extends MCX211 {
	
	private final String TITLE = "DD";

	/**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "^+"+TITLE+"0:0";
    }
	
    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
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
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign)
    {
    	String line4 = sign.getLine4();
    	if(!line4.isEmpty())
    	{
	    	switch(line4.charAt(0))
	    	{
	    		case 'd':
	    		case 'D':
	    		case 'u':
	    		case 'U':
	    			line4 = line4.substring(1);
	    			break;
	    	}
    	}
    	
    	return validate(sign.getLine3(), line4);
    }
    
    @Override
    protected boolean canUseBlock(int id)
    {
    	if(Door.allowedICBlocks == null)
    		return true;
    	
    	if(Door.allowedICBlocks.size() == 0)
    		return false;
    	
    	for(Integer bid : Door.allowedICBlocks)
    	{
    		if(bid == id)
    			return true;
    	}
    	
    	return false;
    }
    
    @Override
    protected int getMaxLength()
    {
    	return Door.maxLength;
    }
    
    @Override
    protected String[] getLine4Values(ChipState chip)
    {
    	String line4 = chip.getText().getLine4();
    	switch(line4.charAt(0))
    	{
    		case 'd':
    		case 'D':
    		case 'u':
    		case 'U':
    			line4 = line4.substring(1);
    			break;
    	}
    	return line4.split("#", 2);
    }
    
    @Override
    protected int[] getStartEnd(ChipState chip, int data, int[] size, int loc)
    {
    	int wStart = size[0] / 2;
        
        int startX = 0;
        int endX = 0;
        int startZ = 0;
        int endZ = 0;
        
        if (data == 0x2) //east
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + size[0];
        	
        	startZ = (int)chip.getBlockPosition().getZ() + 1;
        	endZ = startZ + 1;
        }
        else if (data == 0x3) //west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + size[0];
        	
        	startZ = (int)chip.getBlockPosition().getZ() - 1;
        	endZ = startZ + 1;
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + size[0];
        	
        	startX = (int)chip.getBlockPosition().getX() + 1;
        	endX = startX + 1;
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + size[0];
        	
        	startX = (int)chip.getBlockPosition().getX() - 1;
        	endX = startX + 1;
        }
        
        boolean up = true;
        switch(chip.getText().getLine4().charAt(0))
        {
        	case 'd':
        	case 'D':
        		up = false;
        		break;
        }
        
        int y = (int)chip.getPosition().getY() + size[2];
        int endY;
        
        if(up)
        {
        	y += loc - 1;
        	endY = y + 1;
        }
        else
        {
        	endY = y - loc + 2;
        	y = endY - 1;
        }
        
        return new int[]{startX, y, startZ, endX, endY, endZ};
    }
}
