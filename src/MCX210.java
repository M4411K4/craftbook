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

import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX210 extends MCX209 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "DOOR+";
    }

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
    
    protected int getMaxLength()
    {
    	return Door.maxLength;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
        if (!chip.getIn(1).isTriggered())
            return;
        
        int[] type = getType(chip.getText().getLine3());
        int[] values = getDimensions(chip.getText().getLine4());
        
        if(type[0] < 0 || type[1] < 0 || !canUseBlock(type[1]) || type[2] < -1 || values == null || !(chip.getExtra() instanceof BlockBag))
        	return;
        
        if(type[2] < 0)
			type[2] = 0;
        
        BlockBag bag = (BlockBag) chip.getExtra();
        bag.addSourcePosition(chip.getCBWorld(), chip.getPosition());
        
        World world = CraftBook.getWorld(chip.getCBWorld());
        
        int data = CraftBook.getBlockData(world, chip.getPosition());
        
        int wStart = values[0] / 2;
        
        int startX = 0;
        int endX = 0;
        int startY = (int)chip.getPosition().getY() + values[2];;
        int endY = startY + values[1];
        int startZ = 0;
        int endZ = 0;
        
        if(data == 0x2 || data == 0x3) //east or west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
            endX = startX + values[0];
        	
        	startZ = (int)chip.getBlockPosition().getZ();
        	if(data == 0x2)
        		startZ++;
        	else
        		startZ--;
        	
        	endZ = startZ + 1;
        }
        else if(data == 0x4 || data == 0x5) //north or south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
            endZ = startZ + values[0];
            
        	startX = (int)chip.getBlockPosition().getX();
        	if(data == 0x4)
        		startX++;
        	else
        		startX--;
        	
        	endX = startX + 1;
        }
        
        setBlocks(world, startX, endX, startY, endY, startZ, endZ, chip.getIn(1).is(), type, bag);
    }
}
