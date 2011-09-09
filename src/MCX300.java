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


public class MCX300 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "BOUNCE UP";
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
    		String[] args = sign.getLine3().split("/", 2);
    		String[] dim = args[0].split(":", 2);
    		if(dim.length != 2)
    			return "3rd line format: width:length/x-offset:y-offset:z-offset";
    		try
    		{
    			int width = Integer.parseInt(dim[0]);
    			int length = Integer.parseInt(dim[1]);
    			if(width < 1 || width > 11 || length < 1 || length > 11)
    				return "width and length must be a number from 1 to 11";
    			
    			if(args.length > 1)
    			{
    				String[] offsets = args[1].split(":", 3);
    				if(offsets.length != 3)
    					return "3rd line format: width:length/x-offset:y-offset:z-offset";
    				
    				int offx = Integer.parseInt(offsets[0]);
    				int offy = Integer.parseInt(offsets[1]);
    				int offz = Integer.parseInt(offsets[2]);
    				
    				if(offx < -10 || offx > 10
    					|| offy < -10 || offy > 10
    					|| offz < -10 || offz > 10)
    				{
    					return "offset values must be a number from -10 to 10";
    				}
    			}
    		}
    		catch(NumberFormatException e)
    		{
    			return "3rd line format: width:height/x-offset:y-offset:z-offset";
    		}
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		try
    		{
    			double force = Double.parseDouble(sign.getLine4());
    			if(force < 0.1 || force > 10)
    				return "4th line must be a number from 0.1 to 10";
    		}
    		catch(NumberFormatException e)
    		{
    			return "4th line must be a number from 0.1 to 10";
    		}
    	}

        return null;
    }

    protected static double getForce(SignText text)
    {
    	if(!text.getLine4().isEmpty())
    	{
    		return Double.parseDouble(text.getLine4());
    	}
    	return 5.0D;
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
    		WorldBlockVector wblockVec = new WorldBlockVector(chip.getWorldType(), chip.getPosition());
    		if(hasArea(wblockVec))
    		{
    			return;
    		}
    		
	    	int width = 3;
	    	int length = 3;
	    	int offx = 0;
	    	int offy = 0;
	    	int offz = 0;
	    	
	    	if(!chip.getText().getLine3().isEmpty())
	    	{
	    		String[] args = chip.getText().getLine3().split("/", 2);
	    		String[] dim = args[0].split(":", 2);
	    		
	    		width = Integer.parseInt(dim[0]);
	    		length = Integer.parseInt(dim[1]);
	    		
	    		if(args.length > 1)
	    		{
	    			String[] offsets = args[1].split(":", 3);
	    			offx = Integer.parseInt(offsets[0]);
	    			offy = Integer.parseInt(offsets[1]);
	    			offz = Integer.parseInt(offsets[2]);
	    		}
	    	}
	    	
	    	World world = CraftBook.getWorld(chip.getWorldType());
	        int data = CraftBook.getBlockData(world, chip.getPosition());
	        BlockArea area = getBlockArea(chip, data, width, length, offx, offy, offz);
	        addArea(wblockVec, area);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '!')
    		{
    			chip.getText().setLine1("!"+thisTitle());
    			chip.getText().supressUpdate();
    			
    			RedstoneListener redListener = (RedstoneListener) chip.getExtra();
    			redListener.onSignAdded(CraftBook.getWorld(chip.getWorldType()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			WorldBlockVector wblockVec = new WorldBlockVector(chip.getWorldType(), chip.getPosition());
    			removeArea(wblockVec);
    			chip.getText().setLine1("^"+thisTitle());
    			chip.getText().supressUpdate();
    		}
    	}
    }
    
    protected boolean hasArea(WorldBlockVector key)
    {
    	return Bounce.icAreas.containsKey(key);
    }
    
    protected void addArea(WorldBlockVector key, BlockArea area)
    {
    	Bounce.icAreas.put(key, area);
    }
    
    protected void removeArea(WorldBlockVector key)
    {
    	Bounce.icAreas.remove(key);
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
        	endZ = startZ + length;
        }
        else if (data == 0x3) //west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + width;
        	
        	endZ = (int)chip.getBlockPosition().getZ();
        	startZ = endZ - length;
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + width;
        	
        	startX = (int)chip.getBlockPosition().getX();
        	endX = startX + length;
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + width;
        	
        	endX = (int)chip.getBlockPosition().getX();
        	startX = endX - length;
        }
        
        int y = (int)chip.getPosition().getY() + offy;
        
        return new BlockArea(chip.getWorldType(), startX + offx, y, startZ + offz, endX + offx, y, endZ + offz);
    }
}
