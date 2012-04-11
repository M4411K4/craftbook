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

import java.util.ArrayList;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX217 extends BaseIC {
	
	private final String TITLE = "DRAWBRIDGE";
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "^+"+TITLE;
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
    	if(!sign.getLine3().isEmpty())
    	{
    		String[] dim = sign.getLine3().split(":", 3);
    		if(dim.length < 2)
    			return "3rd line format: width:length:y-offset";
    		try
    		{
    			int width = Integer.parseInt(dim[0]);
    			int length = Integer.parseInt(dim[1]);
    			if(width < 1 || width > 11)
    				return "width must be a number from 1 to 11";
    			if(length < 2 || length > getMaxLength())
    				return "length must be a number from 2 to "+getMaxLength();
    			
    			if(dim.length > 2)
    			{
    				int offy = Integer.parseInt(dim[2]);
    				
    				if(offy < -10 || offy > 10)
    				{
    					return "y-offset value must be a number from -10 to 10";
    				}
    			}
    		}
    		catch(NumberFormatException e)
    		{
    			return "3rd line format: width:length:y-offset";
    		}
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		try
    		{
    			int delay = Integer.parseInt(sign.getLine4());
    			if(delay < 1 || delay > 600)
    				return "4th line must be a number from 1 to 600";
    			
    			sign.setLine4(delay+":0:0");
    		}
    		catch(NumberFormatException e)
    		{
    			return "4th line must be a number from 1 to 600";
    		}
    	}
    	else
    	{
    		sign.setLine4("2:0:0");
    	}

        return null;
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
    @Override
    public void think(ChipState chip)
    {
    	if(chip.inputAmount() == 0)
    	{
    		String[] times = chip.getText().getLine4().split(":",3);
    		
    		int tick = Integer.parseInt(times[1]);
    		tick++;
    		int delay = Integer.parseInt(times[0]);
    		
    		if(tick < delay)
    		{
    			chip.getText().setLine1(chip.getText().getLine1().charAt(0)
    									+""+chip.getText().getLine1().charAt(1)
    									+TITLE);
    			chip.getText().setLine4(times[0]+":"+tick+":"+times[2]);
    			chip.getText().supressUpdate();
    			return;
    		}
    		
    		tick = 0;
    		char state = chip.getText().getLine1().charAt(1);
    		
    		int rot = Integer.parseInt(times[2]);
    		
    		int width = 3;
	    	int length = 5;
	    	int offy = 0;
	    	
	    	if(!chip.getText().getLine3().isEmpty())
	    	{
	    		String[] dim = chip.getText().getLine3().split(":", 3);
	    		
	    		width = Integer.parseInt(dim[0]);
	    		length = Integer.parseInt(dim[1]);
	    		
	    		if(dim.length > 2)
	    		{
	    			offy = Integer.parseInt(dim[2]);
	    		}
	    	}
    		
	    	int nextRot;
    		if(state == '+')
    		{
    			nextRot = rot + 10;
    		}
    		else
    		{
    			nextRot = rot - 10;
    		}
    		
    		if(nextRot < 0 || nextRot > 90)
    		{
    			if(nextRot < 0)
    				nextRot = 0;
    			else if(nextRot > 90)
    				nextRot = 90;
    			
    			chip.getText().setLine4(times[0]+":"+tick+":"+nextRot);
    			turnOff(chip, state);
    			return;
    		}
    		
    		World world = CraftBook.getWorld(chip.getCBWorld());
            int data = CraftBook.getBlockData(world, chip.getPosition());
            
            ArrayList<BlockArea> prevRows = getRows(chip, data, width, length, offy, rot);
            
            if(!checkRows(chip, world, prevRows))
            {
            	turnOff(chip, state);
            	return;
            }
            
            ArrayList<BlockArea> rows = getRows(chip, data, width, length, offy, nextRot);
            
            if(!canMove(chip, world, prevRows, rows))
            {
            	turnOff(chip, state);
            	return;
            }
            
            moveRows(chip, world, prevRows, rows);
            
            chip.getText().setLine4(times[0]+":"+tick+":"+nextRot);
			chip.getText().supressUpdate();
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		char state = chip.getIn(2).is() ? '+' : '-';
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			turnOn(chip, state);
    		}
    		else
    		{
    			turnOff(chip, state);
    		}
    	}
    	else if(chip.getIn(2).isTriggered())
    	{
    		char state = chip.getIn(2).is() ? '+' : '-';
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) == '^')
    		{
    			turnOn(chip, state);
    		}
    		
    		chip.getText().setLine1(chip.getText().getLine1().charAt(0)+""+state+""+TITLE);
			chip.getText().supressUpdate();
    	}
    }
    
    private void turnOn(ChipState chip, char state)
    {
		chip.getText().setLine1("%"+state+TITLE);
		chip.getText().supressUpdate();
		
		RedstoneListener redListener = (RedstoneListener) chip.getExtra();
		redListener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    }
    
    private void turnOff(ChipState chip, char state)
    {
    	chip.getText().setLine1("^"+state+TITLE);
		chip.getText().supressUpdate();
    }
    
    private static ArrayList<BlockArea> getRows(ChipState chip, int data, int width, int length, int offy, int rot)
    {
    	length--;
    	
    	int px;
    	int py;
    	
    	double rad = Math.toRadians(rot);
    	if(rot == 90)
    	{
    		px = 0;
    	}
    	else if(rot <= 45)
    	{
    		px = length;
    	}
    	else
    	{
    		px = (int)Math.ceil(Math.cos(rad) * length);
    	}
    	
    	if(rot == 0)
    	{
    		py = 0;
    	}
    	else if(rot >= 45)
    	{
    		py = length;
    	}
    	else
    	{
    		py = (int)Math.ceil(Math.sin(rad) * length);
    	}
    	
    	int dx = Math.abs(px);
		int dy = Math.abs(py);
		
    	int wStart = width / 2;
    	
    	int sx;
    	int sy;
    	int sz;
    	int ed;
    	
    	ArrayList<BlockArea> rows = new ArrayList<BlockArea>();
        
    	if(data == 0x2) //east
    	{
    		sx = (int)chip.getPosition().getX() - wStart;
    		ed = sx + width;
        	sz = (int)chip.getBlockPosition().getZ() + 1;
    	}
    	else if(data == 0x3) //west
    	{
    		sx = (int)chip.getPosition().getX() - wStart;
    		ed = sx + width;
    		sz = (int)chip.getBlockPosition().getZ() - 1;
    	}
    	else if(data == 0x4) //north
    	{
    		sz = (int)chip.getPosition().getZ() - wStart;
    		ed = sz + width;
    		sx = (int)chip.getBlockPosition().getX() + 1;
    	}
    	else if(data == 0x5) //south
    	{
    		sz = (int)chip.getPosition().getZ() - wStart;
    		ed = sz + width;
    		sx = (int)chip.getBlockPosition().getX() - 1;
    	}
    	else
    		return rows;
    	
    	sy = (int)chip.getBlockPosition().getY() + offy;
		
		boolean steep = dy > dx;
		
		if(steep)
		{
			px = px ^ py;
			py = px ^ py;
			px = px ^ py;
			
			dx = dx ^ dy;
			dy = dx ^ dy;
			dx = dx ^ dy;
		}
		
		int err = dx >> 1;
		
		int j = 0;
		
		int qx;
		int qy;
		
		int nqx = 1;
		int nqy = 0;
		
		for(int i = 0; i <= px; i++)
		{
			qx = i;
			qy = j;
			
			if (nqx != qx || nqy != qy)
			{
				nqx = qx;
				nqy = qy;
				
				if(steep)
				{
					switch(data)
					{
						case 0x2:
							rows.add(new BlockArea(chip.getCBWorld(), sx, sy+qx, sz+qy, ed, sy+qx+1, sz+qy+1));
							break;
						case 0x3:
							rows.add(new BlockArea(chip.getCBWorld(), sx, sy+qx, sz-qy, ed, sy+qx+1, sz-qy+1));
							break;
						case 0x4:
							rows.add(new BlockArea(chip.getCBWorld(), sx+qy, sy+qx, sz, sx+qy+1, sy+qx+1, ed));
							break;
						case 0x5:
							rows.add(new BlockArea(chip.getCBWorld(), sx-qy, sy+qx, sz, sx-qy+1, sy+qx+1, ed));
							break;
					}
				}
				else
				{
					switch(data)
					{
						case 0x2:
							rows.add(new BlockArea(chip.getCBWorld(), sx, sy+qy, sz+qx, ed, sy+qy+1, sz+qx+1));
							break;
						case 0x3:
							rows.add(new BlockArea(chip.getCBWorld(), sx, sy+qy, sz-qx, ed, sy+qy+1, sz-qx+1));
							break;
						case 0x4:
							rows.add(new BlockArea(chip.getCBWorld(), sx+qx, sy+qy, sz, sx+qx+1, sy+qy+1, ed));
							break;
						case 0x5:
							rows.add(new BlockArea(chip.getCBWorld(), sx-qx, sy+qy, sz, sx-qx+1, sy+qy+1, ed));
							break;
					}
				}
			}
			
			err -= dy;
			if (err < 0)
			{
				j++;
				err += dx;
			}
    	}
		
    	return rows;
    }
    
    protected boolean checkRows(ChipState chip, World world, ArrayList<BlockArea> rows)
    {
    	if(rows == null || rows.size() == 0)
    		return false;
    	
    	for(int i = 0; i < rows.size(); i++)
    	{
    		BlockArea row = rows.get(i);
    		if(!checkRow(world, row))
    			return false;
    	}
    	
    	return true;
    }
    
    private boolean checkRow(World world, BlockArea row)
    {
    	for(int x = row.getX(); x < row.getX2(); x++)
    	{
    		for(int y = row.getY(); y < row.getY2(); y++)
    		{
    			for(int z = row.getZ(); z < row.getZ2(); z++)
    			{
    				int id = CraftBook.getBlockID(world, x, y, z);
    				if(id == 0 || !canUseBlock(id))
    				{
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
    
    private static boolean canMove(ChipState chip, World world, ArrayList<BlockArea> prevRows, ArrayList<BlockArea> rows)
    {
    	if(rows == null || prevRows == null || rows.size() == 0 || rows.size() != prevRows.size())
    		return false;
    	
    	for(int i = 0; i < rows.size(); i++)
    	{
    		BlockArea row = rows.get(i);
    		BlockArea prevRow = prevRows.get(i);
    		if(!row.equals(prevRow) && !rowCanPassThrough(world, row))
    		{
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    private static boolean rowCanPassThrough(World world, BlockArea row)
    {
    	for(int x = row.getX(); x < row.getX2(); x++)
    	{
    		for(int y = row.getY(); y < row.getY2(); y++)
    		{
    			for(int z = row.getZ(); z < row.getZ2(); z++)
    			{
    				int id = CraftBook.getBlockID(world, x, y, z);
    				if(!BlockType.canPassThrough(id))
    				{
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
    
    private static void moveRows(ChipState chip, World world, ArrayList<BlockArea> prevRows, ArrayList<BlockArea> rows)
    {
    	for(int i = 0; i < rows.size(); i++)
    	{
    		BlockArea row = rows.get(i);
    		BlockArea prevRow = prevRows.get(i);
    		if(!row.equals(prevRow))
    		{
    			ArrayList<Block> blocks = getRowBlocks(world, prevRow);
    			clearRow(world, prevRow);
    			setRow(world, row, blocks);
    		}
    	}
    }
    
    private static ArrayList<Block> getRowBlocks(World world, BlockArea row)
    {
    	ArrayList<Block> blocks = new ArrayList<Block>();
    	for(int x = row.getX(); x < row.getX2(); x++)
    	{
    		for(int y = row.getY(); y < row.getY2(); y++)
    		{
    			for(int z = row.getZ(); z < row.getZ2(); z++)
    			{
    				blocks.add(world.getBlockAt(x, y, z));
    			}
    		}
    	}
    	return blocks;
    }
    
    private static void setRow(World world, BlockArea row, ArrayList<Block> blocks)
    {
    	int i = 0;
    	for(int x = row.getX(); x < row.getX2(); x++)
    	{
    		for(int y = row.getY(); y < row.getY2(); y++)
    		{
    			for(int z = row.getZ(); z < row.getZ2(); z++)
    			{
    				Block block = blocks.get(i);
    				i++;
    				if(block.getData() != 0)
    					CraftBook.setBlockIdAndData(world, x, y, z, block.getType(), block.getData());
    				else
    					CraftBook.setBlockID(world, x, y, z, block.getType());
    			}
    		}
    	}
    }
    
    private static void clearRow(World world, BlockArea row)
    {
    	for(int x = row.getX(); x < row.getX2(); x++)
    	{
    		for(int y = row.getY(); y < row.getY2(); y++)
    		{
    			for(int z = row.getZ(); z < row.getZ2(); z++)
    			{
    				CraftBook.setBlockID(world, x, y, z, 0);
    			}
    		}
    	}
    }
}
