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




import java.util.Map;

import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

public class MCX700 extends BaseIC {
	
	private final String TITLE = "MELODY";
	
	/**
     * Data store.
     */
    public static Map<String,MusicPlayer> music =
            new HistoryHashMap<String,MusicPlayer>(100);
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "^"+TITLE;
    }
    
    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
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
        if (sign.getLine3().length() == 0)
        {
            return "Specify song file name on line 3.";
        }
        
        String[] params = sign.getLine3().split(":", 3);
        
        if(params[0].length() == 0)
        {
        	return "Specify song file name on line 3.";
        }
        
        String[] file = params[0].split("\\.", 2);
        if(file[0].length() == 0)
        {
        	return "Specify song file name on line 3.";
        }
        
        if(!CopyManager.isValidName(file[0]) || (file.length > 1 && !file[1].equalsIgnoreCase("m")) )
        {
        	return "Not a valid song file name.";
        }
        
        int tickRate;
        if(params.length < 2 || params[1].length() == 0)
        {
        	tickRate = 4;
        }
        else
        {
        	try
        	{
        		tickRate = Integer.parseInt(params[1]);
        	}
        	catch(NumberFormatException e)
        	{
        		return "Tick rate is not a number.";
        	}
        }
        
        if(tickRate < 1)
        {
        	return "Tick rate must be 1 or up.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip)
    {
    	if(chip.inputAmount() == 0)
    	{
        	MusicPlayer player = music.get(chip.getText().getLine4());
        	if(player != null)
        	{
        		player.tick();
        		
        		chip.getOut(1).set(player.isPlaying());
        	}
        	else
        	{
        		chip.getOut(1).set(false);
        	}
    	}
    	else
    	{
    		if(!chip.getIn(1).is() && chip.getText().getLine4().length() != 0)
    		{
    			//turn off
    			turnOff(chip);
    			return;
    		}
    		
    		if(!chip.getIn(1).is() || !chip.getIn(1).isTriggered() || chip.getText().getLine1().charAt(0) == '%')
    			return;
    		
    		long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
    		
    		if(chip.getText().getLine4().length() > 0)
    		{
    			try
    			{
    				long lastTime = Integer.parseInt(chip.getText().getLine4());
    				if(curtime - lastTime < 5)
    				{
    					//quick on/off protection
    					return;
    				}
    			}
    			catch(NumberFormatException e)
    			{
    				return;
    			}
    		}
    		
    		RedstoneListener listener = (RedstoneListener) chip.getExtra();
    		
    		chip.getText().setLine1("%"+TITLE);
    		chip.getText().setLine4(""+curtime);
    		
    		chip.getText().supressUpdate();
    		
    		String[] params = chip.getText().getLine3().split(":", 2);
    		int rate = -1;
    		
    		if(params.length > 1)
    		{
	    		try
	    		{
	    			rate = Integer.parseInt(params[1]);
	    			
	    			if(rate < 1)
	    				rate = -1;
	    		}
	    		catch(NumberFormatException e)
	    		{
	    			return;
	    		}
    		}
    		
    		Vector noteblockPos = Util.getWallSignBack(chip.getPosition(), 2);
    		
    		MusicPlayer player = new MusicPlayer(params[0],
												noteblockPos.getBlockX(),
												noteblockPos.getBlockY(),
												noteblockPos.getBlockZ(),
												listener.properties,
												rate,
												chip.getMode() == 'r');
    		
    		music.put(""+curtime, player);
    		
    		player.loadSong();
    		
    		listener.onSignAdded(chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    	}
    }
    
    private void turnOff(ChipState chip)
    {
    	music.remove(chip.getText().getLine4());
    	chip.getText().setLine1("^"+TITLE);
		chip.getText().supressUpdate();
    }
}