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

package com.sk89q.craftbook;

/**
 * String utilities.
 * 
 * @author sk89q
 */
public class StringUtil {
    /**
     * Trim a string if it is longer than a certain length.
     *  
     * @param str
     * @param len
     * @return
     */
    public static String trimLength(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len);
        }
        
        return str;
    }
    
    /**
     * Get item id and color from property value.
     * 
     * @return
     */
    
    public static int[] getPropColorInt(String prop, int itemDefault, int colorDefault)
    {
    	String[] props = prop.split(":", 2);
    	int[] out = new int[2];
    	
    	if(props.length <= 1 && props[0].length() == 0)
    	{
    		out[0] = itemDefault;
    		out[1] = colorDefault;
    		return out;
    	}
    	
    	try
    	{
    		out[0] = Integer.parseInt(props[0]);
    	}
    	catch(NumberFormatException e)
    	{
    		out[0] = itemDefault;
    	}
    	
    	if(!BlockType.isColorTypeBlock(out[0]))
    	{
    		//not a color block so return color value 0.
    		out[1] = 0;
    		return out;
    	}
    	
    	if(props.length > 1)
    	{
    		try
    		{
    			out[1] = Integer.parseInt(props[1]);
    			
    			//ensure value isn't greater than max color value
    			//15 is current max color value.
    			if(out[1] > 15)
    				out[1] = colorDefault;
    		}
    		catch(NumberFormatException e)
    		{
    			out[1] = colorDefault;
    		}
    	}
    	else
    	{
    		out[1] = colorDefault;
    	}
    	
    	return out;
    }
}
