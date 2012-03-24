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
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * NamedNearby
 * 
 * Variant of MCX512 this sets %p to the name of the player closest to the IC as
 * opposed to the receiving player.
 * 
 * @author drathus
 */
public class MCX513 extends BaseIC {

	private final String TITLE = "NamedNearby";
	protected String distance = "64";

	/**
	 * Get the title of the IC.
	 * 
	 * @return
	 */
	public String getTitle() {
		return TITLE + distance;
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
	 * Validates the IC's environment. The position of the sign is given. Return
	 * a string in order to state an error message and deny creation, otherwise
	 * return null to allow.
	 * 
	 * @param sign
	 * @return
	 */
	public String validateEnvironment(int worldType, Vector pos, SignText sign) {
		if (sign.getLine1().isEmpty() || sign.getLine1().length() > 2) {
			return "A distance is required on the 1st line (1 to 64)";
		}

		try {
			int dist = Integer.parseInt(sign.getLine1());
			if (dist < 1 || dist > 64)
				return "The distance must be a number from 1 to 64";
		} catch (NumberFormatException e) {
			return "The distance must be a number from 1 to 64";
		}

		if (sign.getLine3().isEmpty() && sign.getLine4().isEmpty()) {
			return "A message on the 3rd or 4th line is required.";
		}

		distance = sign.getLine1();

		return null;
	}

	/**
	 * Think.
	 * 
	 * @param chip
	 */
	public void think(ChipState chip) {
		if (!chip.getIn(1).is()) {
			chip.getOut(1).set(false);
			return;
		}

		processMessage(chip.getText().getLine3() + "" + chip.getText().getLine4(), chip.getBlockPosition(), chip.getWorldType(), Integer.parseInt(chip.getText().getLine1().substring(TITLE.length())));

		chip.getOut(1).set(true);
	}

	protected static void processMessage(String message, Vector position, int worldType, int distance) {
		Player nearest = null;
		int min = 64;
		
		if (distance > RedstoneListener.icMessageMaxRange)
			distance = RedstoneListener.icMessageMaxRange;

    	for(Player player: etc.getServer().getPlayerList())
    	{
    		Vector loc = new Vector(player.getX(), player.getY(), player.getZ());
    		int playerDist = (int)Math.floor(loc.distance(position));
    		if(player.getWorld().getType().getId() == worldType && playerDist <= distance)
    		{
    			if(nearest == null || playerDist < min)
    			{
    				min = playerDist;
    				nearest = player;
    			}
    		}
    	}
		
		for (Player player : etc.getServer().getPlayerList()) {
			Vector loc = new Vector(player.getX(), player.getY(), player.getZ());
			int playerDist = (int) Math.floor(loc.distance(position));
			if (player.getWorld().getType().getId() == worldType && playerDist <= distance) {
				String msg = message.replaceAll("%p", nearest.getName());
				msg = msg.replaceAll("&", "§"); // Allow & coloring in message
				msg = msg + "§7"; // Reset color at end of message
				String[] lines = msg.split("/n", 5);
				for (String line : lines) {
					player.sendMessage(line);
				}
			}
		}

		return;
	}
}
