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
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX119 extends MCX118 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MOB NEAR?";
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
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() != 0)
        {
            if(!id.equalsIgnoreCase("mob") && !id.equalsIgnoreCase("mobs")
            	&& !id.equalsIgnoreCase("animal") && !id.equalsIgnoreCase("animals")
            	&& !Mob.isValid(id))
            {
            	return "Invalid mob name or type on 3rd line.";
            }
        }
        
        if (sign.getLine4().length() != 0) {
            try
            {
            	double dist = Double.parseDouble(sign.getLine4());
            	if(dist < 1.0D || dist > 64.0D)
            		return "Range must be a number from 1 to 64";
            }
            catch(NumberFormatException e)
            {
            	return "4th line must be a number.";
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
    	
    	if(chip.inputAmount() == 0 || (chip.getIn(1).is() && chip.getIn(1).isTriggered()) )
    	{
    		String id = chip.getText().getLine3();
    		World world = CraftBook.getWorld(chip.getWorldType());
    		
    		if(id.equalsIgnoreCase("mob") || id.equalsIgnoreCase("mobs"))
    			chip.getOut(1).set( findEntitiesInRange(chip, world.getMobList(), (byte)0) );
    		else if(id.equalsIgnoreCase("animal") || id.equalsIgnoreCase("animals"))
    			chip.getOut(1).set( findEntitiesInRange(chip, world.getAnimalList(), (byte)0) );
    		else
    			chip.getOut(1).set( findEntitiesInRange(chip, world.getLivingEntityList(), (byte)1) );
    	}
    }
    
    @Override
    protected Boolean entityInRange(ChipState chip, BaseEntity entity, byte type)
    {
    	if(type == 0)
    		return true;
    	else if(type == 1 && (entity.isMob() || entity.isAnimal()) && entity instanceof Mob)
    	{
			Mob mob = (Mob) entity;
			if(chip.getText().getLine3().isEmpty() || mob.getName().equalsIgnoreCase(chip.getText().getLine3()))
				return true;
    	}
    	
    	return false;
    }
}
