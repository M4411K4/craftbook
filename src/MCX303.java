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
import com.sk89q.craftbook.ic.ChipState;


public class MCX303 extends MCX302 {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "REPEL WALL";
	@Override
    public String getTitle() {
        return "^"+TITLE;
    }
    protected String thisTitle()
    {
    	return TITLE;
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
    
    @Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	if(sign.getLine4().isEmpty())
    	{
    		switch(CraftBook.getBlockData(cbworld, pos))
    		{
	    		case 0x2:
	    			sign.setLine4("0:0:5");
	    			break;
	    		case 0x3:
	    			sign.setLine4("0:0:-5");
	    			break;
	    		case 0x4:
	    			sign.setLine4("5:0:0");
	    			break;
	    		case 0x5:
	    			sign.setLine4("-5:0:0");
	    			break;
    		}
    	}
    	
    	return super.validateEnvironment(cbworld, pos, sign);
    }

    protected BlockArea getBlockArea(ChipState chip, int data, int width, int length, int offx, int offy, int offz)
    {
    	int wStart = width / 2;
        
        int startX = 0;
        int endX = 0;
        int startZ = 0;
        int endZ = 0;
        
        if (data == 0x2) //east
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + width;
        	
        	startZ = (int)chip.getBlockPosition().getZ();
        	endZ = startZ;
        }
        else if (data == 0x3) //west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + width;
        	
        	endZ = (int)chip.getBlockPosition().getZ();
        	startZ = endZ;
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + width;
        	
        	startX = (int)chip.getBlockPosition().getX();
        	endX = startX;
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + width;
        	
        	endX = (int)chip.getBlockPosition().getX();
        	startX = endX;
        }
        
        int y = (int)chip.getPosition().getY() + offy;
        
        return new BlockArea(chip.getCBWorld(), startX + offx, y, startZ + offz, endX + offx, y + length, endZ + offz);
    }
}
