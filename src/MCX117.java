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



import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX117 extends MCX116 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PLAYER MINE";
    }
    
    public boolean requiresPermission() {
        return true;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
    	
    	if(chip.inputAmount() == 0 || (chip.getIn(1).is() && chip.getIn(1).isTriggered()) )
    	{
    		World world = CraftBook.getWorld(chip.getWorldType());
    		Player player = playerAbove(world, chip.getBlockPosition(), chip.getText().getLine3());
    		
    		if(player == null || player.getEntity().bB)
    		{
    			chip.getOut(1).set(false);
    		}
    		else
    		{
    			explodeTNT(player.getWorld().getWorld(), player.getX(), player.getY(), player.getZ());
    			
    			chip.getOut(1).set(true);
    		}
    	}
    }
    
    /**
     * Makes TNT go boom.
     * 
     * @param x
     * @param y
     * @param z
     */
    protected void explodeTNT(OWorld oworld, double x, double y, double z) {
        // Make TNT explode
    	OEntityTNTPrimed tnt = new OEntityTNTPrimed(oworld);
        tnt.c(x, y, z);
        tnt.y_();
    }
}
