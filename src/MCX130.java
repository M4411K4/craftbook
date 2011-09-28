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
public class MCX130 extends MCX119 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MOB ZAPPER";
    }
    
    public boolean requiresPermission() {
        return true;
    }
    
    
    @Override
    protected Boolean entityInRange(ChipState chip, BaseEntity entity, byte type)
    {
    	if(type == 0)
    	{
    		//entity.destroy();
    		entity.getEntity().N();
    		return null;
    	}
    	else if(type == 1 && (entity.isMob() || entity.isAnimal()) && entity instanceof Mob)
    	{
			Mob mob = (Mob) entity;
			if(chip.getText().getLine3().isEmpty() || mob.getName().equalsIgnoreCase(chip.getText().getLine3()))
			{
				//entity.destroy();
				entity.getEntity().N();
				return null;
			}
    	}
    	
    	return false;
    }
}
