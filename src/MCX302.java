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


public class MCX302 extends MCX300 {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "REPEL FLOOR";
	@Override
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
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
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
    			String[] params = sign.getLine4().split(":", 3);
    			double forcex = Double.parseDouble(params[0]);
    			if(forcex < -10 || forcex > 10)
    				return "4th line force values must be a number from -10 to 10";
    			
    			if(params.length > 1)
    			{
    				double forcey = Double.parseDouble(params[1]);
    				if(forcey < -10 || forcey > 10)
    					return "4th line force values must be a number from -10 to 10";
    				
    				if(params.length > 2)
    				{
    					double forcez = Double.parseDouble(params[2]);
        				if(forcez < -10 || forcez > 10)
        					return "4th line force values must be a number from -10 to 10";
    				}
    				else
    				{
    					sign.setLine4(forcex+":"+forcey+":0");
    				}
    			}
    			else
				{
					sign.setLine4(forcex+":0:0");
				}
    		}
    		catch(NumberFormatException e)
    		{
    			return "4th line must be numbers";
    		}
    	}

        return null;
    }

    protected static double[] getForces(SignText text)
    {
    	if(!text.getLine4().isEmpty())
    	{
    		double[] forces = new double[3];
    		String[] params = text.getLine4().split(":",3);
    		forces[0] = Double.parseDouble(params[0]);
    		forces[1] = Double.parseDouble(params[1]);
    		forces[2] = Double.parseDouble(params[2]);
    		return forces;
    	}
    	return new double[]{0.0D, 5.0D, 0.0D};
    }
    
    protected boolean hasArea(WorldBlockVector key)
    {
    	return Bounce.icRepelAreas.containsKey(key);
    }
    
    protected void addArea(WorldBlockVector key, BlockArea area)
    {
    	Bounce.icRepelAreas.put(key, area);
    }
    
    protected void removeArea(WorldBlockVector key)
    {
    	Bounce.icRepelAreas.remove(key);
    }
}
