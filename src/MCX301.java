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


public class MCX301 extends MCX300 {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "SOFT LAND";
	@Override
    public String getTitle() {
        return "^"+TITLE;
    }
    @Override
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
    	if(!sign.getLine4().isEmpty())
    		return "4th line must be blank.";
    	
    	return super.validateEnvironment(cbworld, pos, sign);
    }
    
    @Override
    protected boolean hasArea(WorldBlockVector key)
    {
    	return Bounce.icSoftAreas.containsKey(key);
    }
    
    @Override
    protected void addArea(WorldBlockVector key, BlockArea area)
    {
    	Bounce.icSoftAreas.put(key, area);
    }
    
    @Override
    protected void removeArea(WorldBlockVector key)
    {
    	Bounce.icSoftAreas.remove(key);
    }
}
