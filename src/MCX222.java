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


public class MCX222 extends MCX220 {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "DETECT EDIT";
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
    protected boolean hasArea(WorldBlockVector key)
    {
    	return MCX220.icAreas.containsKey(key) && MCX221.icAreas.containsKey(key);
    }
    
    @Override
    protected void addArea(WorldBlockVector key, BlockArea area)
    {
    	MCX220.icAreas.put(key, area);
    	MCX221.icAreas.put(key, area);
    }
    
    @Override
    protected void removeArea(WorldBlockVector key)
    {
    	MCX220.icAreas.remove(key);
    	MCX221.icAreas.remove(key);
    }
}
