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
 * 
 * @author M4411K4
*/

public class CBPluginInterface
{
	static PluginInterface cbSignMech = new CBPluginInterface.CBSignMech();
	
	public static class CBSignMech implements PluginInterface
	{
		public static final int GATE = 0;
		public static final int BRIDGE = 1;
		public static final int DOOR = 2;
		public static final int LIFT = 3;
		public static final int AREA = 4;
		
		//public static final int GATE_IS_OPEN = 0;
		//public static final int BRIDGE_IS_OPEN = 1;
		//public static final int DOOR_IS_OPEN = 2;
		
		@Override
		public String checkParameters(Object[] args) {
			if(args == null || args.length != getNumParameters()
			   || args[0] == null || !(args[0] instanceof Integer)
			   || args[1] == null || !(args[1] instanceof Sign)
			   || args[2] == null || !(args[2] instanceof Player))
				return "CBSignMech - Invalid Parameters";
			return null;
		}

		@Override
		public String getName() {
			return "CBSignMech";
		}

		@Override
		public int getNumParameters() {
			return 3;
		}

		@Override
		public Object run(Object[] args) {
			Sign sign = (Sign)args[1];
			Player player = (Player)args[2];
			switch((Integer)args[0])
			{
				case GATE:
					return allowGateToggle(sign, player);
				case BRIDGE:
					return allowBridgeToggle(sign, player);
				case DOOR:
					return allowDoorToggle(sign, player);
				case LIFT:
					return allowLift(sign, player);
				case AREA:
					return allowAreaToggle(sign, player);
			}
			return true;
		}
		
		/********************
		 * 
		 * Listener functions
		 * 
		 * EDIT the functions to allow/deny CraftBook features
		 * Remove any functions that are not needed
		 * 
		 ********************/
		
		private boolean allowGateToggle(Sign sign, Player player)
		{
			/*
			Object result = etc.getLoader().callCustomHook("CBRequest", new Object[]{GATE_IS_OPEN, sign});
			if(result == null)
				return true;
			
			boolean isOpen = ((Boolean)result).booleanValue();
			System.out.println("result: "+isOpen);
			*/
			return true;
		}
		
		private boolean allowBridgeToggle(Sign sign, Player player)
		{
			/*
			Object result = etc.getLoader().callCustomHook("CBRequest", new Object[]{BRIDGE_IS_OPEN, sign});
			if(result == null)
				return true;
			
			boolean isOpen = ((Boolean)result).booleanValue();
			System.out.println("result: "+isOpen);
			*/
			return true;
		}
		
		private boolean allowDoorToggle(Sign sign, Player player)
		{
			/*
			Object result = etc.getLoader().callCustomHook("CBRequest", new Object[]{DOOR_IS_OPEN, sign});
			if(result == null)
				return true;
			
			boolean isOpen = ((Boolean)result).booleanValue();
			System.out.println("result: "+isOpen);
			*/
			return true;
		}
		
		private boolean allowLift(Sign sign, Player player)
		{
			return true;
		}
		
		private boolean allowAreaToggle(Sign sign, Player player)
		{
			return true;
		}
	}
}
