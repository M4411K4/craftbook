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
public class MCX211 extends BaseIC {
	
	private final String TITLE = "DB";
	private static RedstoneListener redListener = null;
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "^+"+TITLE+"0:0";
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
    public String validateEnvironment(int worldType, Vector pos, SignText sign)
    {
    	return validate(sign.getLine3(), sign.getLine4());
    }
    
    protected String validate(String line3, String line4)
    {
        int[][] types = getTypes(line3);
        
        //id check
        if(types == null)
        {
        	return "Specify a block type on the third line.";
        }
        else if(types[0][0] == -1)
        {
        	return "3rd line values must be positive numbers.";
        }
        else if(types[0][0] == -2)
        {
        	return "Block type not allowed: "+types[0][2];
        }
        
        String[] args = line4.split("#", 2);
        int[] dimensions = getDimensions(args[0]);
        
        if(dimensions == null)
        	return "Not a valid dimension WxLxH: "+line4;
        
        if(args.length > 1)
    	{
    		try
    		{
    			String[] delays = args[1].split(":",2);
    			int delay = Integer.parseInt(delays[0]);
    			if(delay < 1 || delay > 600)
    			{
    				return "Delay value must be a number from 1 to 600.";
    			}
    			
    			if(delays.length > 1)
    			{
    				delay = Integer.parseInt(delays[1]);
    				if(delay < 1 || delay > 600)
        			{
        				return "Delay value must be a number from 1 to 600.";
        			}
    			}
    		}
    		catch(NumberFormatException e)
    		{
    			return "Delay value must be a number from 1 to 600.";
    		}
    	}
        
        return null;
    }
    
    protected int[][] getTypes(String line)
    {
    	String[] types = line.split(",", 8);
    	
    	if(types.length == 0)
    		return null;
    	
    	int[][] blocks = new int[types.length][3];
    	
    	for(int i = 0; i < types.length; i++)
    	{
    		String[] args = types[i].split("#", 2);
    		String[] type = args[0].split(":", 2);
    		type = new String[]{type[0],
    							type.length > 1 ? type[1] : "0",
    							args.length > 1 ? args[1] : "1"};
    		
    		try
    		{
    			for(int j = 0; j < type.length; j++)
    			{
    				blocks[i][j] = Integer.parseInt(type[j]);
    				
    				if(blocks[i][j] < 0)
    				{
    					blocks[0][0] = -1;
            			return blocks;
    				}
    			}
    			
    			if(!canUseBlock(blocks[i][0]))
    			{
    				blocks[0][0] = -2;
    				blocks[0][1] = i;
    				blocks[0][2] = blocks[i][0];
    				return blocks;
    			}
    			
    			if(blocks[i][1] > 15)
    			{
    				blocks[i][1] = 0;
    			}
    			
    			if(blocks[i][2] < 1)
    				blocks[i][2] = 1;
    		}
    		catch(NumberFormatException e)
    		{
    			blocks[0][0] = -1;
    			return blocks;
    		}
    	}
    	
    	return blocks;
    }
    
    protected int[] getDimensions(String line)
    {
    	String[] values = line.split(":", 3);
    	
    	int[] out = new int[3];
    	
    	if(values.length == 1)
    		return null;
    	
    	try
    	{
    		for(int i = 0; i < values.length; i++)
    			out[i] = Integer.parseInt(values[i]);
    	}
    	catch(NumberFormatException e)
    	{
    		return null;
    	}
    	
    	if(out[0] <= 0 || out[1] <= 0 || out[0] > 11 || out[2] > 10 || out[2] < -10)
    		return null;
    	
    	if(out[1] > getMaxLength())
    		out[1] = getMaxLength();
    	
    	return out;
    }
    
    protected boolean canUseBlock(int id)
    {
    	if(Bridge.allowedICBlocks == null)
    		return true;
    	
    	if(Bridge.allowedICBlocks.size() == 0)
    		return false;
    	
    	for(Integer bid : Bridge.allowedICBlocks)
    	{
    		if(bid == id)
    			return true;
    	}
    	
    	return false;
    }
    
    protected int getMaxLength()
    {
    	return Bridge.maxLength;
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
    		String times = chip.getText().getLine1().substring(TITLE.length()+2);
    		String[] count = times.split(":",2);
    		
    		int tick = Integer.parseInt(count[0]);
    		tick++;
    		
    		String[] values = getLine4Values(chip);
    		
    		int delay;
    		
    		if(values.length > 1)
    		{
    			String[] delays = values[1].split(":", 2);
    			if(delays.length > 1 && chip.getText().getLine1().charAt(1) == '-')
    				delay = Integer.parseInt(delays[1]);
    			else
    				delay = Integer.parseInt(delays[0]);
    		}
    		else
    			delay = 2;
    		
    		if(tick < delay)
    		{
    			chip.getText().setLine1(chip.getText().getLine1().charAt(0)
    									+""+chip.getText().getLine1().charAt(1)
    									+TITLE+tick+":"+count[1]);
    			chip.getText().supressUpdate();
    			return;
    		}
    		
    		tick = 0;
    		char onState = '%';
    		char state = chip.getText().getLine1().charAt(1);
    		
    		int loc = Integer.parseInt(count[1]);
    		int[] size = getDimensions(values[0]);
    		
    		if(state == '+')
    		{
    			loc++;
    		}
    		else
    		{
    			loc--;
    		}
    		
    		if(loc <= 0 || loc > size[1])
    		{
    			if(loc < 0)
    				loc = 0;
    			else if(loc > size[1] + 1)
    				loc = size[1] + 1;
    			
    			chip.getText().setLine1("^"+state+TITLE+""+tick+":"+loc);
    			chip.getText().supressUpdate();
    			return;
    		}
    		
    		BlockBag bag = redListener.getBlockBag(chip.getWorldType(), chip.getPosition());
    		bag.addSourcePosition(chip.getWorldType(), chip.getPosition());
    		
    		World world = CraftBook.getWorld(chip.getWorldType());
            int data = CraftBook.getBlockData(world, chip.getPosition());
            
            int[] coord = getStartEnd(chip, data, size, loc);
    		
            int[][] types = getTypes(chip.getText().getLine3());
            if(types[0][0] < 0)
            	return; //some kind of error. Most likely a block has been banned.
            
            if(state == '+' && !checkBlocks(world, coord[0], coord[3], coord[1], coord[4], coord[2], coord[5]))
            {
            	onState = '^';
            	if(state == '-')
        		{
            		loc++; //on clearing after block
        		}
            }
            else
            {
                int[] block = getRowBlock(loc, types);
            	boolean result = bagBlocks(state == '+', bag, block[0], block[1], world, coord, false);
            	
            	if(!result)
            	{
            		onState = '^';
            	}
            }
            
    		chip.getText().setLine1(onState+""+state+TITLE+""+tick+":"+loc);
			chip.getText().supressUpdate();
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		char state = chip.getIn(2).is() ? '+' : '-';
    		String times = chip.getText().getLine1().substring(TITLE.length()+2);
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			turnOn(chip, state, times);
    		}
    		else
    		{
    			chip.getText().setLine1("^"+state+TITLE+times);
    			chip.getText().supressUpdate();
    		}
    	}
    	else if(chip.getIn(2).isTriggered())
    	{
    		char state = chip.getIn(2).is() ? '+' : '-';
    		String times = chip.getText().getLine1().substring(TITLE.length()+2);
    		
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) == '^')
    		{
    			turnOn(chip, state, times);
    		}
    		
    		chip.getText().setLine1(chip.getText().getLine1().charAt(0)+""+state+""+TITLE+times);
			chip.getText().supressUpdate();
    	}
    }
    
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
        	
        	startZ = (int)chip.getBlockPosition().getZ() + loc;
        	endZ = startZ + 1;
        }
        else if (data == 0x3) //west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + size[0];
        	
        	startZ = (int)chip.getBlockPosition().getZ() - loc;
        	endZ = startZ + 1;
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + size[0];
        	
        	startX = (int)chip.getBlockPosition().getX() + loc;
        	endX = startX + 1;
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + size[0];
        	
        	startX = (int)chip.getBlockPosition().getX() - loc;
        	endX = startX + 1;
        }
        
        int y = (int)chip.getPosition().getY() + size[2];
        
        return new int[]{startX, y, startZ, endX, y+1, endZ};
    }
    
    protected void turnOn(ChipState chip, char state, String times)
    {
    	String[] args = times.split(":",2);
    	
    	int loc = Integer.parseInt(args[1]);
    	if(loc > 0 && state == '+')
    	{
    		loc--;
    	}
    	
		chip.getText().setLine1("%"+state+TITLE+"0:"+loc);
		chip.getText().supressUpdate();
		
		redListener = (RedstoneListener) chip.getExtra();
		redListener.onSignAdded(CraftBook.getWorld(chip.getWorldType()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    }
    
    protected String[] getLine4Values(ChipState chip)
    {
    	return chip.getText().getLine4().split("#", 2);
    }
    
    protected static boolean checkBlocks(World world, int startX, int endX, int startY, int endY, int startZ, int endZ)
    {
    	startY = setYLimit(startY);
    	endY = setYLimit(endY);
    	
    	for(int x = startX; x < endX; x++)
        {
    		for(int y = startY; y < endY; y++)
    		{
	        	for(int z = startZ; z < endZ; z++)
	        	{
	        		int bType = CraftBook.getBlockID(world, x, y, z);
	        		if(!MCX209.canPassThrough(bType))
	        		{
	        			return false;
	        		}
	        	}
    		}
        }
    	
    	return true;
    }
    
    protected static boolean bagBlocks(boolean set, BlockBag bag, int id, int data, World world, int[] coords, boolean force)
    {
    	coords[1] = setYLimit(coords[1]);
    	coords[4] = setYLimit(coords[4]);
    	
    	for(int x = coords[0]; x < coords[3]; x++)
        {
    		for(int y = coords[1]; y < coords[4]; y++)
    		{
	        	for(int z = coords[2]; z < coords[5]; z++)
	        	{
	        		int curID = CraftBook.getBlockID(world, x, y, z);
	        		try
	        		{
		        		if(set)
		        		{
		        			if(force || MCX209.canPassThrough(curID))
		        			{
		        				if(!bag.setBlockID(world, x, y, z, id, data))
		        					return false;
		        			}
		        			else if(!force && curID != id)
		        			{
		        				return false;
		        			}
		        		}
		        		else
		        		{
		        			int curData = CraftBook.getBlockData(world, x, y , z);
		        			//store
	    					if(curID == id && (!BlockType.isColorTypeBlock(curID) || curData == data))
	    					{
	    						if(!bag.setBlockID(world, x, y, z, 0))
	    						{
	    							return false;
	    						}
	    					}
	    					else if(!force && curID != 0)
	    					{
	    						return false;
	    					}
		        		}
	        		}
	        		catch(OutOfBlocksException e)
	        		{
	        			if(bag.hasRealFetch())
	        				return false;
	        		}
	        		catch(OutOfSpaceException e)
	        		{
	        			if(bag.hasRealStore())
	        				return false;
	        		}
	        		catch(BlockSourceException e)
	        		{
	        			
	        		}
	        	}
    		}
        }
    	return true;
    }
    
    protected static int[] getRowBlock(int row, int[][] blocks)
    {
    	int block = blocks.length - 1;
    	int blockSet = 0;
		for(int i = 0; i < blocks.length; i++)
		{
			blockSet += blocks[i][2];
			if(row <= blockSet)
			{
				block = i;
				break;
			}
		}
		
		return new int[]{blocks[block][0], blocks[block][1]};
    }
    
    protected static int setYLimit(int y)
    {
    	if(y < 0)
    		return 0;
    	if(y > 127)
    		return 127;
    	return y;
    }
}
