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
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX112 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "TRANSPORTER";
    }
    
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
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a band name on the third line.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        String id = chip.getText().getLine3();

        if (!id.isEmpty() && chip.getIn(1).is()) {
            Location dest = MCX113.airwaves.get(id);
            
            if (dest == null) {
                chip.getOut(1).set(false);
            } else
            {
            	dest = new Location(dest.x+0.5, dest.y, dest.z+0.5, dest.rotX, dest.rotY);
            	Vector pos;
            	
            	if(chip.getMode() == 'p' || chip.getMode() == 'P')
            	{
            		pos = Util.getWallSignBack(chip.getPosition(), -2);
            		
            		double newY = pos.getY() + 2;
            		
            		if(newY > 128)
            			newY = 128;
            		
            		pos.setY(newY);
            	}
            	else
            		pos = chip.getBlockPosition();
            	
                int x = pos.getBlockX();
                int z = pos.getBlockZ();
                
                int y = getSafeY(pos);
                
                for(Player player: etc.getServer().getPlayerList())
                {
                	Location pLoc = player.getLocation();
                	Vector pVec = new Vector(pLoc.x, pLoc.y, pLoc.z);
                	
                	if( (pVec.getBlockX() == x || pVec.getBlockX() == x + 1 || pVec.getBlockX() == x - 1) &&
                			pVec.getBlockY() == y &&
                			(pVec.getBlockZ() == z || pVec.getBlockZ() == z + 1 || pVec.getBlockZ() == z - 1)
                		)
                	{
                        dest.y = getSafeY(new Vector(dest.x, dest.y, dest.z)) + 0.2;
                        
                        String msg = chip.getText().getLine4();
                		if(msg.length() == 0)
                			msg = "Woosh!";
                		player.sendMessage(Colors.Gold+msg);
                        
                		player.teleportTo(dest);
                		
                		if(chip.getMode() == 'P')
                		{
                			//force plate off
	                		int bdata = CraftBook.getBlockID(pVec);
	                		if(bdata == BlockType.STONE_PRESSURE_PLATE || bdata == BlockType.WOODEN_PRESSURE_PLATE)
	                		{
	                			OWorld world = player.getEntity().aH;
	                			
	                			int bx = pVec.getBlockX();
	                			int by = pVec.getBlockY();
	                			int bz = pVec.getBlockZ();
	                			
	                			world.c(bx, by, bz, 0);
	                            world.h(bx, by, bz, bdata);
	                            world.h(bx, by - 1, bz, bdata);
	                            world.b(bx, by, bz, bx, by, bz);
	                		}
                		}
                		
                		chip.getOut(1).set(true);
                		break;
                	}
                }
            	
                chip.getOut(1).set(false);
            }
        } else {
            chip.getOut(1).set(false);
        }
    }
    
    private int getSafeY(Vector pos)
    {
    	int maxY = Math.min(128, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
    	
    	for (int y = pos.getBlockY() + 1; y <= maxY; y++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(x, y, z)) &&
            	y < 128 && BlockType.canPassThrough(CraftBook.getBlockID(x, y+1, z))	
            	)
            {
            	return y;
            }
		}
    	
    	return maxY;
    }
}
