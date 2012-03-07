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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.craftbook.*;

/**
 * Delegate listener for vehicle-related hooks and features.
 * 
 * @author sk89q
 */
public class VehicleListener extends CraftBookDelegateListener {
    /**
     * Station to stop at.
     */
    private Map<String,String> stopStation =
            new HashMap<String,String>();
    /**
     * Last minecart message sent from a sign to a player.
     */
    private Map<String,String> lastMinecartMsg =
            new HashMap<String,String>();
    /**
     * Last time minecart message sent from a sign to a player.
     */
    private Map<String,Long> lastMinecartMsgTime =
            new HashMap<String,Long>();
    /**
     * Decay watcher. Used to remove empty minecarts.
     */
    private MinecartDecayWatcher decayWatcher;
    
    /**
     * Track direction for sorting.
     */
    private enum SortDir {
        FORWARD, LEFT, RIGHT
    }
    
    protected static CauldronCookbook craftBlockRecipes = null;
    
    // Settings
    private boolean minecartControlBlocks = true;
    private boolean slickPressurePlates = false;
    private boolean unoccupiedCoast = true;
    private boolean inCartControl = true;
    private boolean minecartDispensers = true;
    private boolean minecartTrackMessages = true;
    private boolean minecartDestroyOnExit = false;
    private boolean minecartDropOnExit = false;
    private boolean minecartEnableLoadBlock = true;
    private boolean minecartEnableWarpBlock = true;
    private boolean minecartEnableDestroyBlock = false;
    
    private int[] minecart25xBoostBlock = new int[]{BlockType.GOLD_ORE, 0};
    private int[] minecart100xBoostBlock = new int[]{BlockType.GOLD_BLOCK, 0};
    private int[] minecart50xSlowBlock = new int[]{BlockType.SLOW_SAND, 0};
    private int[] minecart20xSlowBlock = new int[]{BlockType.GRAVEL, 0};
    private int[] minecartStationBlock = new int[]{BlockType.OBSIDIAN, 0};
    private int[] minecartReverseBlock = new int[]{BlockType.CLOTH, 0};
    private int[] minecartDepositBlock = new int[]{BlockType.IRON_ORE, 0};
    private int[] minecartEjectBlock = new int[]{BlockType.IRON_BLOCK, 0};
    private int[] minecartSortBlock = new int[]{BlockType.NETHERSTONE, 0};
    private int[] minecartDirectionBlock = new int[]{BlockType.CLOTH, 14};
    private int[] minecartLiftBlock = new int[]{BlockType.CLOTH, 1};
    private int[] minecartLaunchBlock = new int[]{BlockType.CLOTH, 5};
    private int[] minecartDelayBlock = new int[]{BlockType.CLOTH, 4};
    private int[] minecartLoadBlock = new int[]{BlockType.CLOTH, 9};
    private int[] minecartStationClearBlock = new int[]{BlockType.CLOTH, 12};
    private int[] minecartCraftBlock = new int[]{BlockType.CLOTH, 7};
    private int[] minecartWarpBlock = new int[]{BlockType.CLOTH, 10};
    private int[] minecartDestroyBlock = new int[]{BlockType.CLOTH, 15};
    
    private enum MinecartCollisionType
    {
    	DEFAULT(false),
    	GHOST(false),
    	PLOW(false),
    	NONE(true),
    	PHASE(true),
    	PHASE_PLOW(true),
    	SMASH(true),
    	SMASH_SCALED(true),
    	RAM(true),
    	RAM_SCALED(true),
    	NO_MERCY(true),
    	NO_MERCY_SCALED(true),
    	;
    	
    	public final boolean REQUIRES_OWORLD;
    	
    	MinecartCollisionType(boolean requiresOWorldFile)
    	{
    		REQUIRES_OWORLD = requiresOWorldFile;
    	}
    };
    private MinecartCollisionType minecartCollisionType = MinecartCollisionType.DEFAULT;
    private boolean usingMinecartOWorldFile = false;
    
    private int minecartMaxSpeed = 100; //higher values than 100 may cause minecarts to derail
    private double minecartBoostFull = 2;
    private double minecartBoostSmall = 1.25;
    private double minecartBoostLaunch = 0.3;
    private double minecartBoostFromRider = 0.1;

    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     * @param properties
     */
    public VehicleListener(CraftBook craftBook, CraftBookListener listener) {
        super(craftBook, listener);
    }

    /**
    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        minecartControlBlocks = properties.getBoolean("minecart-control-blocks", true);
        slickPressurePlates = properties.getBoolean("hinder-minecart-pressure-plate-slow", true);
        unoccupiedCoast = properties.getBoolean("minecart-hinder-unoccupied-slowdown", true);
        inCartControl = properties.getBoolean("minecart-in-cart-control", true);
        minecartDispensers = properties.getBoolean("minecart-dispensers", true);
        minecartTrackMessages = properties.getBoolean("minecart-track-messages", true);
        
        if(properties.containsKey("minecart-enable-loadblock"))
        	minecartEnableLoadBlock = properties.getBoolean("minecart-enable-loadblock", true);
        if(properties.containsKey("minecart-enable-warpblock"))
        	minecartEnableWarpBlock = properties.getBoolean("minecart-enable-warpblock", true);
        if(properties.containsKey("minecart-enable-destroyblock"))
        	minecartEnableDestroyBlock = properties.getBoolean("minecart-enable-destroyblock", false);
        if(properties.containsKey("minecart-using-oworld-server-file"))
        	usingMinecartOWorldFile = properties.getBoolean("minecart-using-oworld-server-file", false);
        
        minecart25xBoostBlock = StringUtil.getPropColorInt(properties.getString("minecart-25x-boost-block"), BlockType.GOLD_ORE, 0);
        minecart100xBoostBlock = StringUtil.getPropColorInt(properties.getString("minecart-100x-boost-block"), BlockType.GOLD_BLOCK, 0);
        minecart50xSlowBlock = StringUtil.getPropColorInt(properties.getString("minecart-50x-slow-block"), BlockType.SLOW_SAND, 0);
        minecart20xSlowBlock = StringUtil.getPropColorInt(properties.getString("minecart-20x-slow-block"), BlockType.GRAVEL, 0);
        minecartStationBlock = StringUtil.getPropColorInt(properties.getString("minecart-station-block"), BlockType.OBSIDIAN, 0);
        minecartReverseBlock = StringUtil.getPropColorInt(properties.getString("minecart-reverse-block"), BlockType.CLOTH, 0);
        minecartDepositBlock = StringUtil.getPropColorInt(properties.getString("minecart-deposit-block"), BlockType.IRON_ORE, 0);
        minecartEjectBlock = StringUtil.getPropColorInt(properties.getString("minecart-eject-block"), BlockType.IRON_BLOCK, 0);
        minecartSortBlock = StringUtil.getPropColorInt(properties.getString("minecart-sort-block"), BlockType.NETHERSTONE, 0);
        minecartDirectionBlock = StringUtil.getPropColorInt(properties.getString("minecart-direction-block"), BlockType.CLOTH, 14);
        minecartLiftBlock = StringUtil.getPropColorInt(properties.getString("minecart-lift-block"), BlockType.CLOTH, 1);
        minecartLaunchBlock = StringUtil.getPropColorInt(properties.getString("minecart-launch-block"), BlockType.CLOTH, 5);
        minecartDelayBlock = StringUtil.getPropColorInt(properties.getString("minecart-delay-block"), BlockType.CLOTH, 4);
        minecartLoadBlock = StringUtil.getPropColorInt(properties.getString("minecart-load-block"), BlockType.CLOTH, 9);
        minecartStationClearBlock = StringUtil.getPropColorInt(properties.getString("minecart-station-clear-block"), BlockType.CLOTH, 12);
        minecartCraftBlock = StringUtil.getPropColorInt(properties.getString("minecart-craft-block"), BlockType.CLOTH, 7);
        minecartWarpBlock = StringUtil.getPropColorInt(properties.getString("minecart-warp-block"), BlockType.CLOTH, 10);
        minecartDestroyBlock = StringUtil.getPropColorInt(properties.getString("minecart-destroy-block"), BlockType.CLOTH, 15);
        
        if(properties.containsKey("minecart-max-speed"))
        	minecartMaxSpeed = properties.getInt("minecart-max-speed", 100);
        if(properties.containsKey("minecart-boost-full"))
        	minecartBoostFull = properties.getDouble("minecart-boost-full", 2.0);
        if(properties.containsKey("minecart-boost-small"))
        	minecartBoostSmall = properties.getDouble("minecart-boost-small", 1.25);
        if(properties.containsKey("minecart-boost-launch"))
        	minecartBoostLaunch = properties.getDouble("minecart-boost-launch", 0.3);
        if(properties.containsKey("minecart-boost-from-rider"))
        	minecartBoostFromRider = properties.getDouble("minecart-boost-from-rider", 0.1);
        
        // If the configuration is merely reloaded, then this must be destroyed
        if (decayWatcher != null) {
            decayWatcher.disable();
        }
        
        int decay = properties.getInt("minecart-decay-time", 0);
        if (decay > 0) {
            decayWatcher = new MinecartDecayWatcher(decay);
        }
        
        minecartDestroyOnExit = properties.getBoolean("minecart-destroy-on-exit");
        minecartDropOnExit = properties.getBoolean("minecart-drop-on-exit");
        
        if(properties.containsKey("minecart-collision-type"))
        {
        	String mct = properties.getString("minecart-collision-type", "DEFAULT").toUpperCase();
        	try
        	{
        		minecartCollisionType = MinecartCollisionType.valueOf(mct);
        		if(minecartCollisionType.REQUIRES_OWORLD && !usingMinecartOWorldFile)
        		{
        			minecartCollisionType = MinecartCollisionType.DEFAULT;
        		}
        	}
        	catch(IllegalArgumentException e)
        	{
        		minecartCollisionType = MinecartCollisionType.DEFAULT;
        	}
        }
    }

    /**
     * Called before the command is parsed. Return true if you don't want the
     * command to be parsed.
     * 
     * @param player
     * @param split
     * @return false if you want the command to be parsed.
     */
    public boolean onCommand(Player player, String[] split) {
        // /st station stop command
        if (split[0].equalsIgnoreCase("/st")) {
            if (split.length >= 2) {
                stopStation.put(player.getName(), "#" + split[1].trim());
                player.sendMessage(Colors.Gold + "You will stop at station \""
                        + split[1].trim() + "\".");
            } else {
                player.sendMessage(Colors.Rose
                        + "You need to specify a station name.");
            }
            return true;
        }
        else if(split[0].equalsIgnoreCase("/cbgo") &&
        		player.getEntity() != null &&
        		UtilEntity.ridingEntity(player.getEntity()) instanceof OEntityMinecart
        		)
        {
        	World world = player.getWorld();
        	
        	BaseEntity basecart = new BaseEntity(UtilEntity.ridingEntity(player.getEntity()));
        	
        	Vector blockpt = new Vector((int)Math.floor(basecart.getX()), (int)Math.floor(basecart.getY()), (int)Math.floor(basecart.getZ()));
        	
        	int under = CraftBook.getBlockID(world, blockpt.getBlockX(), blockpt.getBlockY() - 1, blockpt.getBlockZ());
        	int underColor = CraftBook.getBlockData(world, blockpt.getBlockX(), blockpt.getBlockY() - 1, blockpt.getBlockZ());
        	
        	if(under == minecartDirectionBlock[0] && underColor == minecartDirectionBlock[1])
        	{
        		//make sure tracks are straight? probably doesn't matter, so I'll leave it out for now.
        		Vector point = Util.getFrontPoint(player.getRotation(), blockpt.getBlockX(), blockpt.getBlockY(), blockpt.getBlockZ());
    			if(CraftBook.getBlockID(world, point) != BlockType.MINECART_TRACKS)
    			{
    				player.sendMessage(Colors.Rose+"There are no connected tracks to go that way.");
    			}
    			else
    			{
    				basecart.teleportTo(point.getX(), point.getY() + 0.6200000047683716D, point.getZ(), player.getRotation(), 0);
    				basecart.setMotionX((point.getX() - blockpt.getBlockX()) * minecartBoostLaunch);
    				basecart.setMotionY(0.0);
    				basecart.setMotionZ((point.getZ() - blockpt.getBlockZ()) * minecartBoostLaunch);
    			}
        		
        		return true;
        	}
        }
        return false;
    }

    /**
     * Handles the wire input at a block in the case when the wire is
     * directly connected to the block in question only.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onDirectWireInput(World world, Vector pt, boolean isOn, Vector changed) {
        int type = CraftBook.getBlockID(world, pt);
        
        // Minecart dispenser
        if (minecartDispensers && (
            (type == BlockType.CHEST
             && (CraftBook.getBlockID(world, pt.add(0, -2, 0)) == BlockType.SIGN_POST
              || CraftBook.getBlockID(world, pt.add(0, -1, 0)) == BlockType.SIGN_POST)
             )
             || (type == BlockType.SIGN_POST && Util.doesSignSay(world, pt, 1, "[Dispenser]"))
             )
           )
        {
            
            // Rising edge-triggered only
            if (!isOn) {
                return;
            }
            
            if(type == BlockType.SIGN_POST)
            {
            	pt = pt.add(0, 1, 0);
            	if(CraftBook.getBlockID(world, pt) != BlockType.CHEST)
            	{
            		pt = pt.add(0, 1, 0);
            		if(CraftBook.getBlockID(world, pt) != BlockType.CHEST)
            		{
            			//no chest?
            			return;
            		}
            	}
            }
            
            Vector signPos = pt.add(0, -2, 0);

            Sign sign = getControllerSign(world, pt.add(0, -1, 0), "[Dispenser]");
            
            if (sign == null) {
                return;
            }

            String collectType = sign != null ? sign.getText(2) : "";
            boolean push = sign != null ? sign.getText(3).contains("Push") : false;

            Vector dir = Util.getSignPostOrthogonalBack(world, signPos, 1)
                    .subtract(signPos);
            Vector depositPt = pt.add(dir.multiply(2.5));

            /*if (CraftBook.getBlockID(depositPt) != BlockType.MINECART_TRACKS) {
                return;
            }*/

            int worldType = world.getType().getId();
            NearbyChestBlockBag blockBag = new NearbyChestBlockBag(worldType, pt);
            blockBag.addSingleSourcePosition(worldType, pt);
            blockBag.addSingleSourcePosition(worldType, pt.add(1, 0, 0));
            blockBag.addSingleSourcePosition(worldType, pt.add(-1, 0, 0));
            blockBag.addSingleSourcePosition(worldType, pt.add(0, 0, 1));
            blockBag.addSingleSourcePosition(worldType, pt.add(0, 0, -1));

            try {
                Minecart minecart;
                
                if(collectType.equalsIgnoreCase("All"))
                {
                	Minecart.Type minecartType = Minecart.Type.StorageCart;
                	
                	try {
                        blockBag.fetchBlock(ItemType.STORAGE_MINECART);
                    } catch (BlockSourceException e) {
                        // Okay, no storage minecarts... but perhaps we can
                        // craft a minecart + chest!
                        if (blockBag.peekBlock(BlockType.CHEST)) {
                            blockBag.fetchBlock(ItemType.MINECART);
                            blockBag.fetchBlock(BlockType.CHEST);
                        } else {
                        	minecartType = Minecart.Type.PoweredMinecart;
                        	
                        	try {
                                blockBag.fetchBlock(ItemType.POWERED_MINECART);
                            } catch (BlockSourceException e2) {
                                // Okay, no storage minecarts... but perhaps we can
                                // craft a minecart + chest!
                                if (blockBag.peekBlock(BlockType.FURNACE)) {
                                    blockBag.fetchBlock(ItemType.MINECART);
                                    blockBag.fetchBlock(BlockType.FURNACE);
                                } else {
                                	minecartType = Minecart.Type.Minecart;
                                	blockBag.fetchBlock(ItemType.MINECART);
                                }
                            }
                        }
                    }
                    
                    minecart = spawnMinecart(worldType,
                    						depositPt.getX(),
                    						depositPt.getY(),
                    						depositPt.getZ(),
                    						minecartType.getType());
                }
                else if (collectType.equalsIgnoreCase("Storage")) {
                    try {
                        blockBag.fetchBlock(ItemType.STORAGE_MINECART);
                    } catch (BlockSourceException e) {
                        // Okay, no storage minecarts... but perhaps we can
                        // craft a minecart + chest!
                        if (blockBag.peekBlock(BlockType.CHEST)) {
                            blockBag.fetchBlock(ItemType.MINECART);
                            blockBag.fetchBlock(BlockType.CHEST);
                        } else {
                            throw new BlockSourceException();
                        }
                    }
                    
                    minecart = spawnMinecart(worldType,
    						depositPt.getX(),
    						depositPt.getY(),
    						depositPt.getZ(),
    						Minecart.Type.StorageCart.getType());
                } else if (collectType.equalsIgnoreCase("Powered")) {
                    try {
                        blockBag.fetchBlock(ItemType.POWERED_MINECART);
                    } catch (BlockSourceException e) {
                        // Okay, no storage minecarts... but perhaps we can
                        // craft a minecart + chest!
                        if (blockBag.peekBlock(BlockType.FURNACE)) {
                            blockBag.fetchBlock(ItemType.MINECART);
                            blockBag.fetchBlock(BlockType.FURNACE);
                        } else {
                            throw new BlockSourceException();
                        }
                    }
                    
                    minecart = spawnMinecart(worldType,
    						depositPt.getX(),
    						depositPt.getY(),
    						depositPt.getZ(),
    						Minecart.Type.PoweredMinecart.getType());
                } else {
                    blockBag.fetchBlock(ItemType.MINECART);
                    minecart = spawnMinecart(worldType,
    						depositPt.getX(),
    						depositPt.getY(),
    						depositPt.getZ(),
    						Minecart.Type.Minecart.getType());
                }
                
                if (push) {
                    int data = CraftBook.getBlockData(world, signPos);
                    
                    if (data == 0x0) {
                        minecart.setMotion(0, 0, -minecartBoostLaunch);
                    } else if (data == 0x4) {
                        minecart.setMotion(minecartBoostLaunch, 0, 0);
                    } else if (data == 0x8) {
                        minecart.setMotion(0, 0, minecartBoostLaunch);
                    } else if (data == 0xC) {
                        minecart.setMotion(-minecartBoostLaunch, 0, 0);
                    }
                }
            } catch (BlockSourceException e) {
                // No minecarts
            }
        // Minecart station
        } else if (minecartControlBlocks && type == minecartStationBlock[0]
                && CraftBook.getBlockData(world, pt) == minecartStationBlock[1]
                && CraftBook.getBlockID(world, pt.add(0, 1, 0)) == BlockType.MINECART_TRACKS
                && (CraftBook.getBlockID(world, pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(world, pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            ComplexBlock cblock = world.getComplexBlock(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());

            // Maybe it's the sign directly below
            if (cblock == null || !(cblock instanceof Sign)) {
                cblock = world.getComplexBlock(
                        pt.getBlockX(), pt.getBlockY() - 1, pt.getBlockZ());
            }

            if (cblock == null || !(cblock instanceof Sign)) {
                return;
            }

            Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);

            if (!line2.equalsIgnoreCase("[Station]")) {
                return;
            }

            Vector motion;
            int data = CraftBook.getBlockData(world, 
                    pt.getBlockX(), cblock.getY(), pt.getBlockZ());
            
            if (data == 0x0) {
                motion = new Vector(0, 0, -minecartBoostLaunch);
            } else if (data == 0x4) {
                motion = new Vector(minecartBoostLaunch, 0, 0);
            } else if (data == 0x8) {
                motion = new Vector(0, 0, minecartBoostLaunch);
            } else if (data == 0xC) {
                motion = new Vector(-minecartBoostLaunch, 0, 0);
            } else {
                return;
            }

            for (Minecart minecart : world.getMinecartList()) {
                    int cartX = (int)Math.floor(minecart.getX());
                    int cartY = (int)Math.floor(minecart.getY());
                    int cartZ = (int)Math.floor(minecart.getZ());

                    if (cartX == pt.getBlockX()
                            && cartY == pt.getBlockY() + 1
                            && cartZ == pt.getBlockZ()) {
                        minecart.setMotion(motion.getX(), motion.getY(), motion.getZ());
                    }
            }
        }
    }
    
    /**
     * Called when a vehicle changes block
     *
     * @param vehicle the vehicle
     * @param blockX coordinate x
     * @param blockY coordinate y
     * @param blockZ coordinate z
     */
    @Override
    public void onVehiclePositionChange(BaseVehicle vehicle,
            int blockX, int blockY, int blockZ) {
        if (!minecartControlBlocks && !minecartTrackMessages && !minecartDispensers) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;
            
            World world = minecart.getWorld();

            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(world, blockX, blockY - 1, blockZ);
            int underColor = CraftBook.getBlockData(world, blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                // Overflow prevention
                if (Math.abs(minecart.getMotionX()) > minecartMaxSpeed) {
                    minecart.setMotionX(Math.signum(minecart.getMotionX()) * minecartMaxSpeed);
                }
                
                if (Math.abs(minecart.getMotionZ()) > minecartMaxSpeed) {
                    minecart.setMotionZ(Math.signum(minecart.getMotionZ()) * minecartMaxSpeed);
                }
                
                if (under == minecart25xBoostBlock[0] && underColor == minecart25xBoostBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * minecartBoostSmall);
                        minecart.setMotionZ(minecart.getMotionZ() * minecartBoostSmall);
                        return;
                    }
                } else if (under == minecart100xBoostBlock[0] && underColor == minecart100xBoostBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * minecartBoostFull);
                        minecart.setMotionZ(minecart.getMotionZ() * minecartBoostFull);
                        return;
                    }
                } else if (under == minecart50xSlowBlock[0] && underColor == minecart50xSlowBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.5);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.5);
                        return;
                    }
                } else if (under == minecart20xSlowBlock[0] && underColor == minecart20xSlowBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.8);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.8);
                        return;
                    }
                } else if (under == minecartStationClearBlock[0] && underColor == minecartStationClearBlock[1]
                			&& minecart.getPassenger() != null) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                    	stopStation.remove(minecart.getPassenger().getName());
                        //return;
                    }
                } else if (under == minecartDepositBlock[0] && underColor == minecartDepositBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        if (minecart.getType() == Minecart.Type.StorageCart) {
                            Vector pt = new Vector(blockX, blockY, blockZ);
                            int worldType = world.getType().getId();
                            NearbyChestBlockBag bag = new NearbyChestBlockBag(worldType, pt);

                            for (int y = -1; y <= 0; y++) {
                                bag.addSingleSourcePositionExtra(worldType, pt.add(1, y, 0));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(2, y, 0));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(-1, y, 0));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(-2, y, 0));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(0, y, 1));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(0, y, 2));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(0, y, -1));
                                bag.addSingleSourcePositionExtra(worldType, pt.add(0, y, -2));
                            }
                            
                            if (bag.getChestBlockCount() > 0) {
                            	Sign sign = getControllerSign(world, pt.add(0, -1, 0), "[Deposit]");
                                if (sign != null){
                                	
                                	//repeat call protection
                                	long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
                            		if(sign.getText(0).length() > 0)
                            		{
                            			try
                            			{
                            				int hashid = Integer.parseInt(sign.getText(0));
                            				if(hashid == minecart.getEntity().hashCode())
                            				{
                            					if(sign.getText(3).length() > 0)
                            					{
                            						long lastTime = Long.parseLong(sign.getText(3));
                            						if(curtime - lastTime < 2)
                            							return;
                            					}
                            				}
                            				else
                            				{
                            					sign.setText(0, ""+minecart.getEntity().hashCode());
                            				}
                            				sign.setText(3, ""+curtime);
                            			}
                            			catch(NumberFormatException e)
                            			{
                            				
                            			}
                            		}
                            		else
                            		{
                            			sign.setText(0, ""+minecart.getEntity().hashCode());
                            			sign.setText(3, ""+curtime);
                            		}
                                	
                            		//the actual moving
                                	if(sign.getText(2).length() > 0)
                                	{
                                		String[] args = sign.getText(2).split(":", 2);
                                		int type = 0;
                                		int color = -1;
                                		int amount = 0;
                                		
                                		try
                                		{
                                			String[] args2 = args[0].split("@", 2);
                                			type = Integer.parseInt(args2[0]);
                                			if(args2.length > 1)
                                				color = Integer.parseInt(args2[1]);
                                			if(args.length > 1)
                                				amount = Integer.parseInt(args[1]);
                                		}
                                		catch(NumberFormatException e)
                                		{
                                			return;
                                		}
                                		
                                		ItemArrayUtil.moveChestBagToItemArray(
                                				minecart.getStorage(), bag, type, color, amount);
                                	}
                                	else
                                	{
                                		ItemArrayUtil.moveChestBagToItemArray(
                                				minecart.getStorage(), bag);
                                	}
                                } else {
                                	sign = getControllerSign(world, pt.add(0, -1, 0), "[Collect]");
                                	
                                	if(sign != null)
                                	{
                                		//repeat call protection
                                		long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
                                		if(sign.getText(0).length() > 0)
                                		{
                                			try
                                			{
                                				int hashid = Integer.parseInt(sign.getText(0));
                                				if(hashid == minecart.getEntity().hashCode())
                                				{
                                					if(sign.getText(3).length() > 0)
                                					{
                                						long lastTime = Long.parseLong(sign.getText(3));
                                						if(curtime - lastTime < 2)
                                							return;
                                					}
                                				}
                                				else
                                				{
                                					sign.setText(0, ""+minecart.getEntity().hashCode());
                                				}
                                				sign.setText(3, ""+curtime);
                                			}
                                			catch(NumberFormatException e)
                                			{
                                				
                                			}
                                		}
                                		else
                                		{
                                			sign.setText(0, ""+minecart.getEntity().hashCode());
                                			sign.setText(3, ""+curtime);
                                		}
                                	}
                                	
                                	//the actual moving
                                	if(sign != null && sign.getText(2).length() > 0)
                                	{
                                		String[] args = sign.getText(2).split(":", 2);
                                		int type = 0;
                                		int color = -1;
                                		int amount = 0;
                                		
                                		try
                                		{
                                			String[] args2 = args[0].split("@", 2);
                                			type = Integer.parseInt(args2[0]);
                                			if(args2.length > 1)
                                				color = Integer.parseInt(args2[1]);
                                			if(args.length > 1)
                                				amount = Integer.parseInt(args[1]);
                                		}
                                		catch(NumberFormatException e)
                                		{
                                			return;
                                		}
                                		
                                		ItemArrayUtil.moveItemArrayToChestBag(
                                				minecart.getStorage(), bag, type, color, amount);
                                	}
                                	else
                                	{
                                		ItemArrayUtil.moveItemArrayToChestBag(
                                				minecart.getStorage(), bag);
                                	}
                                }
                            }
                        }
                    }

                    return;
                } else if (under == minecartEjectBlock[0] && underColor == minecartEjectBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        Player player = minecart.getPassenger();
                        if (player != null) {
                            // Let's find a place to put the player
                            Location loc = new Location(blockX, blockY, blockZ);
                            Vector signPos = new Vector(blockX, blockY - 2, blockZ);

                            if (CraftBook.getBlockID(world, signPos) == BlockType.SIGN_POST
                                    && Util.doesSignSay(world, signPos, 1, "[Eject]")) {
                                Vector pos = Util.getSignPostOrthogonalBack(world, signPos, 1);

                                // Acceptable sign direction
                                if (pos != null) {
                                    pos = pos.setY(blockY);

                                    // Is the spot free?
                                    if (BlockType.canPassThrough(CraftBook.getBlockID(world, pos.add(0, 1, 0)))
                                            && BlockType.canPassThrough(CraftBook.getBlockID(world, pos))) {
                                        loc = new Location(
                                                pos.getBlockX(),
                                                pos.getBlockY(),
                                                pos.getBlockZ());

                                        ComplexBlock cBlock = world.getComplexBlock(
                                                blockX, blockY - 2, blockZ);

                                        if (cBlock instanceof Sign) {
                                            Sign sign = (Sign)cBlock;
                                            String text = sign.getText(0);
                                            if (text.length() > 0) {
                                                player.sendMessage(Colors.Gold + "You've arrived at: "
                                                        + text);
                                            }
                                        }
                                    }
                                }
                            }

                            loc.x = loc.x + 0.5;
                            loc.y = loc.y + 0.1;
                            loc.z = loc.z + 0.5;
                            loc.dimension = player.getWorld().getType().getId();

                            UtilEntity.mountEntity(player.getEntity(), (OEntity)null); //to eject
                            player.teleportTo(loc);
                        }
                    }

                    return;
                } else if (under == minecartSortBlock[0] && underColor == minecartSortBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[Sort]");
                        
                        if (sign != null) {
                            SortDir dir = SortDir.FORWARD;

                            if (satisfiesCartSort(sign.getText(2), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.LEFT;
                            } else if (satisfiesCartSort(sign.getText(3), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.RIGHT;
                            }

                            int signData = CraftBook.getBlockData(world, 
                                    sign.getX(), sign.getY(), sign.getZ());
                            int newData = 0;
                            Vector targetTrack = null;
                            
                            if (signData == 0x8) { // West
                                if (dir == SortDir.LEFT) {
                                    newData = 9;
                                } else if (dir == SortDir.RIGHT) {
                                    newData = 8;
                                } else {
                                    newData = 0;
                                }
                                targetTrack = new Vector(blockX, blockY, blockZ + 1);
                            } else if (signData == 0x0) { // East
                                if (dir == SortDir.LEFT) {
                                    newData = 7;
                                } else if (dir == SortDir.RIGHT) {
                                    newData = 6;
                                } else {
                                    newData = 0;
                                }
                                targetTrack = new Vector(blockX, blockY, blockZ - 1);
                            } else if (signData == 0xC) { // North
                                if (dir == SortDir.LEFT) {
                                    newData = 6;
                                } else if (dir == SortDir.RIGHT) {
                                    newData = 9;
                                } else {
                                    newData = 1;
                                }
                                targetTrack = new Vector(blockX - 1, blockY, blockZ);
                            } else if (signData == 0x4) { // South
                                if (dir == SortDir.LEFT) {
                                    newData = 8;
                                } else if (dir == SortDir.RIGHT) {
                                    newData = 7;
                                } else {
                                    newData = 1;
                                }
                                targetTrack = new Vector(blockX + 1, blockY, blockZ);
                            }
                            
                            if (targetTrack != null
                                    && CraftBook.getBlockID(world, targetTrack) == BlockType.MINECART_TRACKS) {
                                CraftBook.setBlockData(world, targetTrack, newData);
                            }
                        }
                    }

                    return;
                } else if (craftBlockRecipes != null && under == minecartCraftBlock[0] && underColor == minecartCraftBlock[1]
                		&& (OMathHelper.b(minecart.getEntity().bj) != OMathHelper.b(minecart.getX())
                    			|| OMathHelper.b(minecart.getEntity().bl) != OMathHelper.b(minecart.getZ()))
                		) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test == null || test) {
                        if (minecart.getType() == Minecart.Type.StorageCart && minecart.getStorage() != null) {
                        	
                        	ItemArray<?> cartStorage = minecart.getStorage();
                        	Item[] cartItems = cartStorage.getContents();
                        	
                        	Map<CraftBookItem,Integer> contents = new HashMap<CraftBookItem,Integer>();
                        	
                        	for(int i = 0; i < cartItems.length; i++)
                        	{
                        		Item item = cartItems[i];
                        		if(item == null || item.getAmount() <= 0)
                        			continue;
                        		
                        		int dmg = item.getDamage();
                        		if(BlockType.isDirectionBlock(item.getItemId()))
                        			dmg = 0;
                        		
                        		CraftBookItem cbItem = new CraftBookItem(item.getItemId(), dmg);
                        		if(!contents.containsKey(cbItem))
                        		{
                        			contents.put(cbItem, item.getAmount());
                        		}
                        		else
                        		{
                        			contents.put(cbItem, contents.get(cbItem) + item.getAmount());
                        		}
                        	}
                        	
                        	CauldronRecipe recipe = craftBlockRecipes.find(contents);

                            if(recipe != null)
                            {
                            	List<CraftBookItem> ingredients = new ArrayList<CraftBookItem>(recipe.getIngredients());
                            	
                            	boolean itemsFound = true;
                            	ingredientLoop:
                            	for(int i = 0; i < ingredients.size(); i++)
                            	{
                            		if(ingredients.get(i) == null)
                            			continue;
                            		
                            		CraftBookItem itemType = ingredients.get(i);
                            		
                            		for(int j = 0; j < cartItems.length; j++)
                                	{
                                		Item cartItem = cartItems[j];
                                		
                                		if (cartItem == null || cartItem.getAmount() <= 0
                            				|| itemType.id() != cartItem.getItemId()
                            				|| itemType.color() != cartItem.getDamage()
                            				)
                                		{
                                			continue;
                                		}
                                		
                                		if(cartItem.getAmount() == 1)
                                		{
                                			cartItems[j] = null;
                                		}
                                		else
                                		{
                                			cartItems[j].setAmount(cartItem.getAmount() - 1);
                                		}
                                		
                                		continue ingredientLoop;
                                	}
                            		
                            		itemsFound = false;
                            		
                            		//item not found? did something change to the minecart storage inventory?
                            		
                            		break;
                            	}
                            	
                            	if(itemsFound)
                            	{
                            		for(CraftBookItem cbItem : recipe.getResults())
                            		{
                            			boolean found = false;
                            			for(int i = 0; i < cartItems.length; i++)
                                    	{
                            				if(cartItems[i] != null
                            					&& cartItems[i].getItemId() == cbItem.id()
                            					&& cartItems[i].getDamage() == cbItem.color()
                            					&& cartItems[i].getAmount() > 0
                            					&& cartItems[i].getAmount() < ItemArrayUtil.getStackMax(cartItems[i])
                            					)
                            				{
                            					cartItems[i].setAmount(cartItems[i].getAmount() + 1);
                            					found = true;
                            					break;
                            				}
                                    	}
                            			
                            			if(!found)
                        				{
                            				for(int i = 0; i < cartItems.length; i++)
                                        	{
                            					if(cartItems[i] == null)
                            					{
                            						cartItems[i] = new Item(cbItem.id(), 1, i, cbItem.color());
                            						found = true;
                            						break;
                            					}
                                        	}
                        				}
                            			
                            			if(!found)
                            			{
                            				//no space to add!
                            				return;
                            			}
                                    }
                            		
                            		ItemArrayUtil.setContents(cartStorage, cartItems);
                            	}
                            }
                        }
                    }
                    
                    return;
                } else if (minecartEnableLoadBlock
                			&& under == minecartLoadBlock[0] && underColor == minecartLoadBlock[1]
                            && minecart.isEmpty()
                            && minecart.getType().getType() == 0) {
	                Boolean test = Redstone.testAnyInput(world, underPt);
	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[Load]");
                        
	                	OEntityPlayer eplayer = null;
	                	OWorld oworld = minecart.getWorld().getWorld();
	                	final double DIST = 3.0D;
	                	
                        if (sign != null)
                        {
                        	//load from only a certain direction
                        	int data = CraftBook.getBlockData(world, sign.getX(), sign.getY(), sign.getZ());
                        	
                        	double closeDist = -1.0D;
                        	
                        	for(int i = 0; i < oworld.d.size(); i++)
                        	{
                        		OEntityPlayer tmpplayer = (OEntityPlayer) oworld.d.get(i);
                        		double d2 = tmpplayer.e(minecart.getX(), minecart.getY(), minecart.getZ());
                        		
                        		BaseEntity basePlayer = new BaseEntity(tmpplayer);
                        		
                        		if( (d2 < DIST * DIST) && ((closeDist == -1.0D) || (d2 < closeDist))
                        			&& ( (data == 0x0 && basePlayer.getZ() >= minecart.getZ())
                        				|| (data == 0x4 && basePlayer.getX() <= minecart.getX())
                        				|| (data == 0x8 && basePlayer.getZ() <= minecart.getZ())
                        				|| (data == 0xC && basePlayer.getX() >= minecart.getX())
                        					)
                        			)
                        		{
                        			closeDist = d2;
                        			eplayer = tmpplayer;
                        		}
                        	}
                        }
                        else
                        {
                        	eplayer = oworld.a(minecart.getEntity(), DIST);
                        }
                        
                        if (eplayer != null)
                    	{
                        	UtilEntity.mountEntity(eplayer, minecart.getEntity());
                    	}
	                }
                }
            }

            if (minecartTrackMessages
                    && CraftBook.getBlockID(world, underPt.add(0, -1, 0)) == BlockType.SIGN_POST) {
                Vector signPos = underPt.add(0, -1, 0);
                
                Boolean test = Redstone.testAnyInput(world, signPos);

                if (test == null || test) {
                    ComplexBlock cblock = world.getComplexBlock(
                            signPos.getBlockX(), signPos.getBlockY(), signPos.getBlockZ());

                    if (!(cblock instanceof Sign)) {
                        return;
                    }

                    Sign sign = (Sign)cblock;
                    String line1 = sign.getText(0);

                    if (line1.equalsIgnoreCase("[Print]")) {
                        Player player = minecart.getPassenger();
                        if (player == null) { return; }
                        
                        String name = player.getName();
                        String msg = sign.getText(1) + sign.getText(2) + sign.getText(3);
                        long now = System.currentTimeMillis();

                        if (lastMinecartMsg.containsKey(name)) {
                            String lastMessage = lastMinecartMsg.get(name);
                            if (lastMessage.equals(msg)
                                    && now < lastMinecartMsgTime.get(name) + 3000) {
                                return;
                            }
                        }

                        lastMinecartMsg.put(name, msg);
                        lastMinecartMsgTime.put(name, now);
                        player.sendMessage("> " + msg);
                    }
                }
            }

            if (minecartDispensers && !UtilEntity.isDead(minecart.getEntity()) ) {
                Vector pt = new Vector(blockX, blockY, blockZ);
                Vector depositPt = null;

                if (CraftBook.getBlockID(world, pt.add(1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(world, pt.add(-1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(-1, -1, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(-1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(1, 0, 0);
                } else if (CraftBook.getBlockID(world, pt.add(-1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(world, pt.add(1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(1, -1, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(-1, 0, 0);
                } else if (CraftBook.getBlockID(world, pt.add(0, 0, 1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(world, pt.add(0, 0, -1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(0, -1, -1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(0, 1, -1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, 1);
                } else if (CraftBook.getBlockID(world, pt.add(0, 0, -1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(world, pt.add(0, 0, 1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(0, -1, 1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(world, pt.add(0, 1, 1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, -1);
                }

                if (depositPt != null) {
                    Sign sign = getControllerSign(world, depositPt.add(0, -1, 0), "[Dispenser]");
                    String collectType = sign != null ? sign.getText(2) : "";
                    
                    int worldType = world.getType().getId();
                    NearbyChestBlockBag blockBag = new NearbyChestBlockBag(worldType, depositPt);
                    blockBag.addSingleSourcePosition(worldType, depositPt);
                    blockBag.addSingleSourcePosition(worldType, depositPt.add(1, 0, 0));
                    blockBag.addSingleSourcePosition(worldType, depositPt.add(-1, 0, 0));
                    blockBag.addSingleSourcePosition(worldType, depositPt.add(0, 0, 1));
                    blockBag.addSingleSourcePosition(worldType, depositPt.add(0, 0, -1));

                    Minecart.Type type = minecart.getType();
                    if (type == Minecart.Type.Minecart) {
                        try {
                            blockBag.storeBlock(ItemType.MINECART);
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                        }
                    } else if (type == Minecart.Type.StorageCart) {
                        try {
                            ItemArrayUtil.moveItemArrayToChestBag(
                                    minecart.getStorage(), blockBag);

                            if (collectType.equalsIgnoreCase("Storage") || collectType.equalsIgnoreCase("All")) {
                                blockBag.storeBlock(ItemType.STORAGE_MINECART);
                            } else {
                                blockBag.storeBlock(ItemType.MINECART);
                                blockBag.storeBlock(BlockType.CHEST);
                            }
                            
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                            // Ran out of space
                        }
                    } else if (type == Minecart.Type.PoweredMinecart) {
                        try {
                            if (collectType.equalsIgnoreCase("Powered") || collectType.equalsIgnoreCase("All")) {
                                blockBag.storeBlock(ItemType.POWERED_MINECART);
                            } else {
                                blockBag.storeBlock(ItemType.MINECART);
                                blockBag.storeBlock(BlockType.FURNACE);
                            }
                            minecart.destroy();
                        } catch (BlockSourceException e) {
                            // Ran out of space
                        }
                    }
                    
                    blockBag.flushChanges();
                }
            }
        }
    }

    /**
     * Called when a vehicle enters or leaves a block
     *
     * @param vehicle the vehicle
     */
    @Override
    public void onVehicleUpdate(BaseVehicle vehicle) {
        if (!minecartControlBlocks && !unoccupiedCoast) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;

            World world = minecart.getWorld();
            
            int blockX = (int)Math.floor(minecart.getX());
            int blockY = (int)Math.floor(minecart.getY());
            int blockZ = (int)Math.floor(minecart.getZ());
            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(world, blockX, blockY - 1, blockZ);
            int underColor = CraftBook.getBlockData(world, blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == minecartStationBlock[0] && underColor == minecartStationBlock[1]) {
                    Boolean test = Redstone.testAnyInput(world, underPt);

                    if (test != null) {
                        if (!test) {
                            minecart.setMotion(0, 0, 0);
                            return;
                        } else {
                            ComplexBlock cblock = world.getComplexBlock(
                                    blockX, blockY - 2, blockZ);

                            // Maybe it's the sign directly below
                            if (cblock == null || !(cblock instanceof Sign)) {
                                cblock = world.getComplexBlock(
                                        blockX, blockY - 3, blockZ);
                            }

                            if (cblock != null && cblock instanceof Sign) {
                                Sign sign = (Sign)cblock;
                                String line2 = sign.getText(1);

                                if (!line2.equalsIgnoreCase("[Station]")) {
                                    return;
                                }

                                String line3 = sign.getText(2);
                                String line4 = sign.getText(3);
                                
                                if (line3.equalsIgnoreCase("Pulse")
                                        || line4.equalsIgnoreCase("Pulse")) {
                                    return;
                                }

                                Vector motion  = null;
                                int data = CraftBook.getBlockData(world, 
                                        blockX, cblock.getY(), blockZ);
                                
                                if (data == 0x0) {
                                    motion = new Vector(0, 0, -minecartBoostLaunch);
                                } else if (data == 0x4) {
                                    motion = new Vector(minecartBoostLaunch, 0, 0);
                                } else if (data == 0x8) {
                                    motion = new Vector(0, 0, minecartBoostLaunch);
                                } else if (data == 0xC) {
                                    motion = new Vector(-minecartBoostLaunch, 0, 0);
                                }

                                if (motion != null) {
                                    if (!MathUtil.isSameSign(minecart.getMotionX(), motion.getX())
                                            || minecart.getMotionX() < motion.getX()) {
                                        minecart.setMotionX(motion.getX());
                                    }
                                    if (!MathUtil.isSameSign(minecart.getMotionY(), motion.getY())
                                            || minecart.getMotionY() < motion.getY()) {
                                        minecart.setMotionY(motion.getY());
                                    }
                                    if (!MathUtil.isSameSign(minecart.getMotionZ(), motion.getZ())
                                            || minecart.getMotionZ() < motion.getZ()) {
                                        minecart.setMotionZ(motion.getZ());
                                    }
                                    
                                    return;
                                }
                            }
                        }
                    }
                }
                else if(under == minecartReverseBlock[0] && underColor == minecartReverseBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(world, underPt);
                	if (test == null || test)
                	{
                		OEntityMinecart eminecart = minecart.getEntity();
                		if(OMathHelper.b(eminecart.bj) == OMathHelper.b(minecart.getX())
                		   && OMathHelper.b(eminecart.bk) == OMathHelper.b(minecart.getY())
                		   && OMathHelper.b(eminecart.bl) == OMathHelper.b(minecart.getZ()))
                			return;
                		
                		Vector signPos = new Vector(blockX, blockY - 2, blockZ);
                		boolean reverseX = true;
                		boolean reverseZ = true;
                		
                		// Directed reverse block
                		if (CraftBook.getBlockID(world, signPos) == BlockType.SIGN_POST
                			&& Util.doesSignSay(world, signPos, 1, "[Reverse]"))
                		{
                			Vector dir = Util.getSignPostOrthogonalBack(world, signPos, 1).subtract(signPos);
                			
                			// Acceptable sign direction
                			if (dir != null)
                			{
                				if (MathUtil.isSameSign(minecart.getMotionX(),dir.getBlockX()))
                				{
                					reverseX = false;
                				}
                				if (MathUtil.isSameSign(minecart.getMotionZ(),dir.getBlockZ()))
                				{
                					reverseZ = false;
                				}
                			}
                		}
                		
                		if (reverseX) {
                            minecart.setMotionX(minecart.getMotionX() * -1);
                        }
                        if (reverseZ) {
                            minecart.setMotionZ(minecart.getMotionZ() * -1);
                        }
                        
                        return;
                	}
                }
                else if (under == minecartDirectionBlock[0] && underColor == minecartDirectionBlock[1]
                        && minecart.getPassenger() != null)
                {
					Boolean test = Redstone.testAnyInput(world, underPt);
					
					if (test == null || test)
					{
						if(minecart.getMotionX() != 0.0 || minecart.getMotionZ() != 0.0)
						{
							Player player = minecart.getPassenger();
							player.sendMessage(Colors.Gold+"Face the direction you want to go, then type: "+Colors.White+"/cbgo");
							
							minecart.setMotionX(0);
							minecart.setMotionZ(0);
							
							//don't return so we can print any message sign.
						}
						else
						{
							//return so we dont print message again.
							return;
						}
					}
				}
				else if (under == minecartLaunchBlock[0] && underColor == minecartLaunchBlock[1])
				{
					Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[Launch]");
					if(sign != null)
					{
						Boolean test = Redstone.testAnyInput(world, underPt);
						
						if (test == null || test)
						{
							Boolean launch = null;
							
							if(sign.getText(2).length() == 0 && sign.getText(3).length() == 0)
							{
								launch = !minecart.isEmpty();
							}
							else if(satisfiesCartSort(sign.getText(2), minecart,
									new Vector(blockX, blockY, blockZ)))
							{
								launch = true;
							}
							else if(satisfiesCartSort(sign.getText(3), minecart,
									new Vector(blockX, blockY, blockZ)))
							{
								launch = false;
							}
							else if(sign.getText(2).length() == 0)
							{
								launch = true;
							}
							else if(sign.getText(3).length() == 0)
							{
								launch = false;
							}
							
							//Do nothing for launch == null i guess...
							if(launch == null)
								return;
							
							if(launch)
							{
								int data = CraftBook.getBlockData(world, sign.getX(), sign.getY(), sign.getZ());
								
								Vector motion = null;
								if (data == 0x0) {
									motion = new Vector(0, 0, -minecartBoostLaunch);
								} else if (data == 0x4) {
									motion = new Vector(minecartBoostLaunch, 0, 0);
								} else if (data == 0x8) {
									motion = new Vector(0, 0, minecartBoostLaunch);
								} else if (data == 0xC) {
									motion = new Vector(-minecartBoostLaunch, 0, 0);
								} else {
									return;
							}
							
							if(motion != null)
								minecart.setMotion(motion.getX(), motion.getY(), motion.getZ());
							}
							else
							{
								minecart.setMotionX(0);
								minecart.setMotionZ(0);
							}
							
							return;
						}
					}
                }
                else if(under == minecartLiftBlock[0] && underColor == minecartLiftBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(world, underPt);
                	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[CartLift]");
	                	
	                	//works the same as [Sort]
	                	if (sign != null) {
                            SortDir dir = SortDir.FORWARD;
                            
                            //LEFT = UP
                            //RIGHT = DOWN

                            if (satisfiesCartSort(sign.getText(2), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.LEFT;
                            } else if (satisfiesCartSort(sign.getText(3), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.RIGHT;
                            }
                            
                            Sign destSign = null;
                            Vector dest = null;
                            
                            if(dir == SortDir.FORWARD)
                            {
                            	return;
                            }
                            else if(dir == SortDir.LEFT)
                            {
                            	//up
                            	for(int y = blockY + 1; y <= CraftBook.MAP_BLOCK_HEIGHT - 1; y++)
                            	{
                            		if (CraftBook.getBlockID(world, blockX, y, blockZ) == minecartLiftBlock[0] &&
                            			CraftBook.getBlockData(world, blockX, y, blockZ) == minecartLiftBlock[1] &&
                            			CraftBook.getBlockID(world, blockX, y+1, blockZ) == BlockType.MINECART_TRACKS)
                            		{
                            			destSign = getControllerSign(world, blockX, y, blockZ, "[CartLift]");
                            			
                            			dest = new Vector(blockX, y, blockZ);
                            			break;
                            		}
                            	}
                            }
                            else if(dir == SortDir.RIGHT)
                            {
                            	//down
                            	for(int y = blockY - 2; y >= 1; y--)
                            	{
                            		if (CraftBook.getBlockID(world, blockX, y, blockZ) == minecartLiftBlock[0] &&
                            			CraftBook.getBlockData(world, blockX, y, blockZ) == minecartLiftBlock[1] &&
                            			CraftBook.getBlockID(world, blockX, y+1, blockZ) == BlockType.MINECART_TRACKS)
                            		{
                            			destSign = getControllerSign(world, blockX, y, blockZ, "[CartLift]");
                            			
                            			dest = new Vector(blockX, y, blockZ);
                            			break;
                            		}
                            	}
                            }
                            
                            Player player = minecart.getPassenger();
                            
                            if(dest == null)
                            {
                            	if(player != null)
                            		player.sendMessage(Colors.Rose+"No lift found.");
                            	return;
                            }
                            
                            if(!BlockType.canPassThrough(CraftBook.getBlockID(world, blockX, dest.getBlockY()+2, blockZ)))
                            {
                            	if(player != null)
                            		player.sendMessage(Colors.Rose+"The lift is obstructed!");
                            	return;
                            }
                            
                            Location targetTrack = null;
                            Vector motion = null;
                            int wantedDir = -1;
                            
                            if(destSign != null)
                            {
                            	wantedDir = CraftBook.getBlockData(world, destSign.getX(), destSign.getY(), destSign.getZ());
                            	if(wantedDir != 0 && wantedDir != 4 && wantedDir != 8 && wantedDir != 12)
                            	{
                            		wantedDir = -1;
                            	}
                            }
                            
                            //if wantedDir == -1 then find a track that exists
                            if((wantedDir == -1 || wantedDir == 8)
                            		&& CraftBook.getBlockID(world, blockX, dest.getBlockY()+1, blockZ + 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX, dest.getBlockY()+1, blockZ + 1) == 0)
                            {
                            	//west
                            	targetTrack = new Location(blockX, dest.getBlockY()+1, blockZ + 1, 0, 0);
                            	motion = new Vector(0, 0, minecartBoostLaunch);
                            }
                            if((wantedDir == -1 || wantedDir == 4)
                            		&& CraftBook.getBlockID(world, blockX + 1, dest.getBlockY()+1, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX + 1, dest.getBlockY()+1, blockZ) == 1)
                            {
                            	//south
                            	targetTrack = new Location(blockX + 1, dest.getBlockY()+1, blockZ, 270, 0);
                            	motion = new Vector(minecartBoostLaunch, 0, 0);
                            }
                            if((wantedDir == -1 || wantedDir == 0)
                            		&& CraftBook.getBlockID(world, blockX, dest.getBlockY()+1, blockZ - 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX, dest.getBlockY()+1, blockZ - 1) == 0)
                            {
                            	//east
                            	targetTrack = new Location(blockX, dest.getBlockY()+1, blockZ - 1, 180, 0);
                            	motion = new Vector(0, 0, -minecartBoostLaunch);
                            }
                            if((wantedDir == -1 || wantedDir == 12)
                            		&& CraftBook.getBlockID(world, blockX - 1, dest.getBlockY()+1, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX - 1, dest.getBlockY()+1, blockZ) == 1)
                            {
                            	//north
                            	targetTrack = new Location(blockX - 1, dest.getBlockY()+1, blockZ, 90, 0);
                            	motion = new Vector(-minecartBoostLaunch, 0, 0);
                            }
                            
                            if(targetTrack != null)
                            {
                            	if(player != null && destSign != null && destSign.getText(0).length() > 0)
                            		player.sendMessage(Colors.Gold+destSign.getText(0));
                            	
                            	targetTrack.dimension = minecart.getWorld().getType().getId();
                            	minecart.teleportTo(targetTrack);
                            	minecart.setMotion(motion.getX(), 0, motion.getZ());
                            }
                            else
                            {
                            	//spams... remove after this is no longer a new feature
                            	//if(player != null)
                            		//player.sendMessage(Colors.Rose+"No connected track found at lift.");
                            }
                            return;
                        }
	                }
                }
                else if(under == minecartDelayBlock[0] && underColor == minecartDelayBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(world, underPt);
                	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[Delay]");
	                	
	                	if (sign != null)
	                	{
	                		int delay = 0;
                        	long time = 0;
                        	try
                        	{
                        		delay = Integer.parseInt(sign.getText(2));
                        		
                        		if(sign.getText(3).length() <= 0)
                        			time = 0;
                        		else
                        			time = Long.parseLong(sign.getText(3));
                        	}
                        	catch(NumberFormatException e)
                        	{
                        		return;
                        	}
                        	
                        	long curtime = (long)Math.floor(System.currentTimeMillis() / 1000);
                        	
                        	//is this check really needed?
                        	//it sees if the year is 33658 or up! That's just a "little" away from 2011!
                        	//
                        	//if(curtime > 1000000000000000L)
                        		//curtime = curtime % 1000000000000000L;
                        	
                        	long diff = curtime - time;
                        	
                        	//if greater than 1 hour...
                        	if(diff > 3600)
                        		time = 0;
                        	
                        	//if cart was removed while on delay, give 5 sec till start over
                        	if(diff - delay > 5)
                        	{
                        		time = 0;
                        	}
                        	
                        	if(time == 0 || diff < delay)
                        	{
                        		//hold
                        		minecart.setMotionX(0);
			                    minecart.setMotionZ(0);
                        	}
                        	else
                        	{
                        		int data = CraftBook.getBlockData(world, sign.getX(), sign.getY(), sign.getZ());
		                		
                        		Location targetTrack = null;
		                		Vector motion = null;
		                		if (data == 0x0)
		                		{
		                			targetTrack = new Location(blockX, blockY, blockZ - 1, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(0, 0, -minecartBoostLaunch);
		        	            }
		                		else if (data == 0x4)
		        	            {
		                			targetTrack = new Location(blockX + 1, blockY, blockZ, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(minecartBoostLaunch, 0, 0);
		        	            }
		        	            else if (data == 0x8)
		        	            {
		        	            	targetTrack = new Location(blockX, blockY, blockZ + 1, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(0, 0, minecartBoostLaunch);
		        	            }
		        	            else if (data == 0xC)
		        	            {
		        	            	targetTrack = new Location(blockX - 1, blockY, blockZ, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(-minecartBoostLaunch, 0, 0);
		        	            }
		        	            else
		        	            {
		        	                return;
		        	            }
		                		
		                		if(motion != null)
		                		{
		                			targetTrack.dimension = minecart.getWorld().getType().getId();
		                			minecart.teleportTo(targetTrack);
		                			minecart.setMotion(motion.getX(), motion.getY(), motion.getZ());
		                		}
		                		
		                		//can use this instead of teleporting, but has chance to cause a minecart to
		                		//get stuck on laggy servers. So commented-out.
		                		//
		                		//if(diff - delay > 2)
		                		//{
		                			time = 0;
		                			curtime = 0;
		                		//}
                        	}
                        	
                        	if(time == 0)
                        	{
                        		sign.setText(3, ""+curtime);
                        	}
	                	}
	                }
                }
                else if(under == minecartWarpBlock[0] && underColor == minecartWarpBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(world, underPt);
                	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(world, blockX, blockY - 1, blockZ, "[CartWarp]");
	                	
	                	//works the same as [Sort]
	                	if (sign != null) {
                            SortDir dir = SortDir.FORWARD;
                            
                            //LEFT = Warp
                            //RIGHT = Warp

                            if (satisfiesCartSort(sign.getText(2), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.LEFT;
                            } else if (satisfiesCartSort(sign.getText(3), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.RIGHT;
                            }
                            
                            if(dir != SortDir.LEFT && dir != SortDir.RIGHT)
                            {
                            	return;
                            }
                            
                            //warp
                            Player player = minecart.getPassenger();
                            
                            CBWarpObject warp = CBWarp.getWarp(sign.getText(0), false);
                            if(warp == null)
                            {
                            	if(player != null)
                            		player.sendMessage(Colors.Rose+"CBWarp not found: "+sign.getText(0));
                            	return;
                            }
                            
                            world = warp.LOCATION.getWorld();
                            blockX = (int)Math.floor(warp.LOCATION.x);
                            blockY = (int)Math.floor(warp.LOCATION.y);
                            blockZ = (int)Math.floor(warp.LOCATION.z);
                            
                            if(!BlockType.canPassThrough(CraftBook.getBlockID(world, blockX, blockY, blockZ)))
                            {
                            	if(player != null)
                            		player.sendMessage(Colors.Rose+"The warp is obstructed!");
                            	return;
                            }
                            
                            Location targetTrack = null;
                            Vector motion = null;
                            int wantedDir = -1;
                            
                            Sign destSign = getControllerSign(world, blockX, blockY - 1, blockZ, "[CartWarp]");
                            
                            if(destSign != null)
                            {
                            	wantedDir = CraftBook.getBlockData(world, destSign.getX(), destSign.getY(), destSign.getZ());
                            	if(wantedDir != 0 && wantedDir != 4 && wantedDir != 8 && wantedDir != 12)
                            	{
                            		wantedDir = -1;
                            	}
                            }
                            
                            //if wantedDir == -1 then find a track that exists
                            if((wantedDir == -1 || wantedDir == 8)
                            		&& CraftBook.getBlockID(world, blockX, blockY, blockZ + 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX, blockY, blockZ + 1) == 0)
                            {
                            	//west
                            	targetTrack = new Location(blockX, blockY, blockZ + 1, 0, 0);
                            	motion = new Vector(0, 0, minecartBoostLaunch);
                            }
                            if((wantedDir == -1 || wantedDir == 4)
                            		&& CraftBook.getBlockID(world, blockX + 1, blockY, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX + 1, blockY, blockZ) == 1)
                            {
                            	//south
                            	targetTrack = new Location(blockX + 1, blockY, blockZ, 270, 0);
                            	motion = new Vector(minecartBoostLaunch, 0, 0);
                            }
                            if((wantedDir == -1 || wantedDir == 0)
                            		&& CraftBook.getBlockID(world, blockX, blockY, blockZ - 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX, blockY, blockZ - 1) == 0)
                            {
                            	//east
                            	targetTrack = new Location(blockX, blockY, blockZ - 1, 180, 0);
                            	motion = new Vector(0, 0, -minecartBoostLaunch);
                            }
                            if((wantedDir == -1 || wantedDir == 12)
                            		&& CraftBook.getBlockID(world, blockX - 1, blockY, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(world, blockX - 1, blockY, blockZ) == 1)
                            {
                            	//north
                            	targetTrack = new Location(blockX - 1, blockY, blockZ, 90, 0);
                            	motion = new Vector(-minecartBoostLaunch, 0, 0);
                            }
                            
                            if(targetTrack != null)
                            {
                            	targetTrack.dimension = warp.LOCATION.dimension;
                            	targetTrack.y += 0.6200000047683716D;
                            	CraftBook.teleportEntity(minecart, targetTrack);
                            	minecart.setMotion(motion.getX(), 0, motion.getZ());
                            }
                            else
                            {
                            	warp.LOCATION.y += 0.6200000047683716D;
                            	CraftBook.teleportEntity(minecart, warp.LOCATION);
                            }
                            return;
                        }
	                }
                }
                else if(minecartEnableDestroyBlock && under == minecartDestroyBlock[0] && underColor == minecartDestroyBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(world, underPt);
                	
	                if (test == null || test)
	                {
	                	if(!UtilEntity.isDead(minecart.getEntity()) && minecart.getType() != Minecart.Type.StorageCart)
	                	{
	                		UtilEntity.mountEntity(minecart.getEntity(), null);
	                		minecart.destroy();
	                	}
	                	return;
	                }
                }
            }
            
            int block = CraftBook.getBlockID(world, blockX, blockY, blockZ);
            if (slickPressurePlates
                    && (block == BlockType.STONE_PRESSURE_PLATE
                    || block == BlockType.WOODEN_PRESSURE_PLATE)) {
                // Numbers from code
                minecart.setMotion(minecart.getMotionX() / 0.55,
                                   0,
                                   minecart.getMotionZ() / 0.55);
            }

            if (unoccupiedCoast && minecart.getPassenger() == null) {
                minecart.setMotionX(minecart.getMotionX() * 1.018825);
                minecart.setMotionZ(minecart.getMotionZ() * 1.018825);
            }
        }
    }

    /**
     * Called when vehicle receives damage
     *
     * @param vehicle
     * @param attacker entity that dealt the damage
     * @param damage
     * @return false to set damage
     */
    @Override
    public boolean onVehicleDamage(BaseVehicle vehicle,
            BaseEntity attacker, int damage) {

        if (!inCartControl) {
            return false;
        }

        Player passenger = vehicle.getPassenger();

        // Player.equals() now works correctly as of recent hMod versions
        if (passenger != null && attacker != null && vehicle instanceof Minecart
                && attacker.isPlayer()
                && attacker.getPlayer().equals(passenger)) {
            double speed = Math.sqrt(Math.pow(vehicle.getMotionX(), 2)
                    + Math.pow(vehicle.getMotionY(), 2)
                    + Math.pow(vehicle.getMotionZ(), 2));

            if (speed > 0.01) { // Stop the cart
                vehicle.setMotion(0, 0, 0);
            } else {
                // From hey0's code, and then stolen from WorldEdit
                double rot = (passenger.getRotation() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }

                String dir = etc.getCompassPointForDirection(rot);
                
                if (dir.equals("N")) {
                    vehicle.setMotion(0, 0, minecartBoostFromRider);
                } else if(dir.equals("NE")) {
                    vehicle.setMotion(-minecartBoostFromRider, 0, minecartBoostFromRider);
                } else if(dir.equals("E")) {
                    vehicle.setMotion(-minecartBoostFromRider, 0, 0);
                } else if(dir.equals("SE")) {
                    vehicle.setMotion(-minecartBoostFromRider, 0, -minecartBoostFromRider);
                } else if(dir.equals("S")) {
                    vehicle.setMotion(0, 0, -minecartBoostFromRider);
                } else if(dir.equals("SW")) {
                    vehicle.setMotion(minecartBoostFromRider, 0, -minecartBoostFromRider);
                } else if(dir.equals("W")) {
                    vehicle.setMotion(minecartBoostFromRider, 0, 0);
                } else if(dir.equals("NW")) {
                    vehicle.setMotion(minecartBoostFromRider, 0, minecartBoostFromRider);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Called when someone places a block. Return true to prevent the placement.
     * 
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return true if you want to undo the block placement
     */
    public boolean onBlockPlace(Player player, Block blockPlaced,
            Block blockClicked, Item itemInHand) {
        
    	World world = player.getWorld();
        if (blockPlaced.getType() == BlockType.MINECART_TRACKS) {
            int under = CraftBook.getBlockID(world, blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
            int underColor = CraftBook.getBlockData(world, blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
            
            if (minecartControlBlocks && under == minecartStationBlock[0] && underColor == minecartStationBlock[1]) {
                Sign sign = getControllerSign(world, blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ(), "[Station]");
                Vector pt = new Vector(blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
                
                boolean needsRedstone = Redstone.testAnyInput(world, pt) == null;

                if (sign == null && needsRedstone) {
                    player.sendMessage(Colors.Gold
                            + "Two things to do: Wire the block and place a [Station] sign.");
                } else if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "Place a [Station] sign 1-2 blocks underneath.");
                } else if (needsRedstone) {
                    player.sendMessage(Colors.Rose
                            + "To make the station work, wire it up with redstone.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart station created.");
                }
            } else if (minecartControlBlocks && under == minecart25xBoostBlock[0] && underColor == minecart25xBoostBlock[1]) {
                player.sendMessage(Colors.Gold + "Minecart boost block created.");
            } else if (minecartControlBlocks && under == minecart100xBoostBlock[0] && underColor == minecart100xBoostBlock[1]) {
                player.sendMessage(Colors.Gold + "Minecart boost block created.");
            } else if (minecartControlBlocks && under == minecart50xSlowBlock[0] && underColor == minecart50xSlowBlock[1]) {
                player.sendMessage(Colors.Gold + "Minecart brake block created.");
            } else if (minecartControlBlocks && under == minecart20xSlowBlock[0] && underColor == minecart20xSlowBlock[1]) {
                player.sendMessage(Colors.Gold + "Minecart brake block created.");
            } else if (minecartControlBlocks && under == minecartReverseBlock[0] && underColor == minecartReverseBlock[1]) {
                player.sendMessage(Colors.Gold + "Minecart reverse block created.");
            } else if (minecartControlBlocks && under == minecartSortBlock[0] && underColor == minecartSortBlock[1]) {
                Sign sign = getControllerSign(world, blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[Sort]");
                //Vector pt = new Vector(blockPlaced.getX(),
                //    blockPlaced.getY() - 1, blockPlaced.getZ());

                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "A [Sort] sign is still needed.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart sort block created.");
                }
            } else if (minecartControlBlocks && under == minecartDirectionBlock[0] && underColor == minecartDirectionBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart direction block created.");
            } else if (minecartControlBlocks && under == minecartLiftBlock[0] && underColor == minecartLiftBlock[1]) {
            	Sign sign = getControllerSign(world, blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[CartLift]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "Cart Lift destination created. A [CartLift] sign is needed to lift up or down.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Cart Lift block created.");
                }
            } else if (minecartControlBlocks && under == minecartLaunchBlock[0] && underColor == minecartLaunchBlock[1]) {
            	Sign sign = getControllerSign(world, blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[Launch]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "A "+Colors.White+"[Launch] sign"+Colors.Rose+" is still needed.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Launch block created.");
                }
            } else if (minecartControlBlocks && under == minecartDelayBlock[0] && underColor == minecartDelayBlock[1]) {
            	Sign sign = getControllerSign(world, blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[Delay]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "A [Delay] sign is still needed.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Delay block created.");
                }
            } else if (minecartControlBlocks && under == minecartStationClearBlock[0] && underColor == minecartStationClearBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart station clearing block created.");
            } else if (minecartControlBlocks && under == minecartCraftBlock[0] && underColor == minecartCraftBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart craft block created.");
            } else if (minecartControlBlocks && minecartEnableLoadBlock && under == minecartLoadBlock[0]
                       && underColor == minecartLoadBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart load block created.");
            } else if (minecartControlBlocks && minecartEnableWarpBlock && under == minecartWarpBlock[0]
                    && underColor == minecartWarpBlock[1]) {
            	Sign sign = getControllerSign(world, blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[CartWarp]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "Cart Warp destination created. A [CartWarp] sign is needed to warp Minecarts.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Cart Warp block created.");
                }
            } else if (minecartControlBlocks && minecartEnableDestroyBlock && under == minecartDestroyBlock[0]
                    && underColor == minecartDestroyBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart destroy block created.");
            }
        }
        
        return false;
    }

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(Player player, Sign sign) {
    	World world = player.getWorld();
    	
        int type = CraftBook.getBlockID(world, 
                sign.getX(), sign.getY(), sign.getZ());

        String line1 = sign.getText(0);
        String line2 = sign.getText(1);

        // Station
        if (line2.equalsIgnoreCase("[Station]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Station]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Station sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Sort
        } else if (line2.equalsIgnoreCase("[Sort]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Sort]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Sort sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Dispenser
        } else if (line2.equalsIgnoreCase("[Dispenser]")) {
            int data = CraftBook.getBlockData(world, 
                    sign.getX(), sign.getY(), sign.getZ());

            if (type == BlockType.WALL_SIGN) {
                player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                return true;
            } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                return true;
            }
            
            listener.informUser(player);
            
            sign.setText(1, "[Dispenser]");
            sign.update();
        
            player.sendMessage(Colors.Gold + "Dispenser sign detected.");
        // Print
        } else if (line1.equalsIgnoreCase("[Print]")) {
            listener.informUser(player);
            
            sign.setText(0, "[Print]");
            sign.update();
        
            player.sendMessage(Colors.Gold + "Message print block detected.");
        // Cart Lift
        // should combine code into function....
        } else if (line2.equalsIgnoreCase("[CartLift]")) {
            listener.informUser(player);
            
            sign.setText(1, "[CartLift]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Cart Lift sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Launch
        } else if (line2.equalsIgnoreCase("[Launch]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Launch]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Launch sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Delay
        } else if (line2.equalsIgnoreCase("[Delay]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Delay]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else {
                	String line3 = sign.getText(2);
                	int delay = 0;
                	try
                	{
                		delay = Integer.parseInt(line3);
                	}
                	catch(NumberFormatException e)
                	{
                		player.sendMessage(Colors.Rose + "The third line must contain a number.");
                        CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                        return true;
                	}
                	
                	if(delay <= 0 || delay > 3600)
                	{
                		player.sendMessage(Colors.Rose + "Delay must be 1 to 3600");
                        CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                        return true;
                	}
                }
                
                sign.setText(3, "");
                sign.update();
                
                player.sendMessage(Colors.Gold + "Delay sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Load
        } else if (line2.equalsIgnoreCase("[Load]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Load]");
            sign.update();
            
            if (minecartControlBlocks && minecartEnableLoadBlock) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Load sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
         // Teleport
        } else if (line2.equalsIgnoreCase("[CartWarp]")) {
        	
        	if(!player.canUseCommand("/cbwarp"))
        	{
        		player.sendMessage(Colors.Rose + "You do not have permission to make that.");
                CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                return true;
        	}
        	
            listener.informUser(player);
            
            sign.setText(1, "[CartWarp]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(world, 
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                else if(!line1.isEmpty() && !CBWarp.warpExists(line1, false))
                {
                	player.sendMessage(Colors.Rose + "CBWarp not found: "+line1);
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                if(line1.isEmpty())
                {
                	sign.setText(2, "Direction");
                    sign.update();
                }
                else if(sign.getText(2).isEmpty() && sign.getText(3).isEmpty())
                {
                	sign.setText(2, "ALL");
                    sign.update();
                }
                
                player.sendMessage(Colors.Gold + "Warp sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        }
        
        return false;
    }

    /**
     * Called when a player enter or leaves a vehicle
     * 
     * @param vehicle the vehicle
     * @param player the player
     */
    public void onVehicleEnter(BaseVehicle vehicle, HumanEntity player) {
        if (vehicle instanceof Minecart) {
            if (decayWatcher != null) {
                decayWatcher.trackEnter((Minecart)vehicle);
            }
            
            if(minecartCollisionType == MinecartCollisionType.GHOST)
    		{
            	OEntityMinecart ecart = (OEntityMinecart)vehicle.getEntity();
            	if(vehicle.getPassenger() != null)
                {
            		ecart.bG = 0.98F;
            		ecart.bH = 0.7F;
                }
            	else
            	{
            		ecart.bG = 0.001F;
            		ecart.bH = 0.001F;
            	}
    		}
            
            
            if (minecartDestroyOnExit && vehicle.getPassenger() != null && !UtilEntity.isDead(vehicle.getEntity()) ) {
                vehicle.destroy();
                
                if (minecartDropOnExit) {
                    player.getPlayer().giveItem(new Item(ItemType.MINECART, 1));
                }
            }
        }
    }

    /**
     * Called when a vehicle is destroyed
     * 
     * @param vehicle the vehicle
     */
    public void onVehicleDestroyed(BaseVehicle vehicle) {
        if (decayWatcher != null && vehicle instanceof Minecart) {
            decayWatcher.forgetMinecart((Minecart)vehicle);
        }
    }
    
    /**
     * Called when a collision occurs with a vehicle and an entity.
     * 
     * @param vehicle
     *            the vehicle
     * @param collisioner
     * @return false to ignore damage
     */
    public Boolean onVehicleCollision(BaseVehicle vehicle, BaseEntity collisioner) {
    	
    	if(minecartCollisionType == MinecartCollisionType.DEFAULT || !(vehicle.getEntity() instanceof OEntityMinecart))
    		return false;
    	
    	Minecart minecart1 = new Minecart((OEntityMinecart)vehicle.getEntity());
    	
    	if( (minecartCollisionType == MinecartCollisionType.GHOST || minecartCollisionType == MinecartCollisionType.PLOW)
    		&& collisioner.getEntity() instanceof OEntityMinecart)
    	{
    		Minecart emptyCart = new Minecart((OEntityMinecart)collisioner.getEntity());
    		
    		if( ( emptyCart.isEmpty() && vehicle.getPassenger() != null)
    			|| (vehicle.isEmpty() && !emptyCart.isEmpty() && emptyCart.getPassenger() != null)
    			)
    		{
    			Minecart playerCart;
                
                if(vehicle.isEmpty())
                {
                	playerCart = new Minecart((OEntityMinecart)collisioner.getEntity());
                	emptyCart = minecart1;
                }
                else
                {
                	playerCart = minecart1;
                	emptyCart = new Minecart((OEntityMinecart)collisioner.getEntity());
                }
    			
                double s = playerCart.getMotionX() * playerCart.getMotionX() + playerCart.getMotionZ() * playerCart.getMotionZ();
                if(s > 0.0)
                {
                	double es = emptyCart.getMotionX() * emptyCart.getMotionX() + emptyCart.getMotionZ() * emptyCart.getMotionZ();
                	if(s > es)
                	{
                		if(minecartCollisionType == MinecartCollisionType.GHOST)
                		{
                			OAxisAlignedBB bb = emptyCart.getEntity().bw.b(0.2000000029802322D, 0.0D, 0.2000000029802322D);
                    		
                    		if(playerCart.getMotionX() != 0)
                    		{
                    			if(playerCart.getMotionX() < 0)
                    				playerCart.setX(bb.a + emptyCart.getMotionX());
                    			else
                    				playerCart.setX(bb.d + emptyCart.getMotionX());
                    		}
                    		if(playerCart.getMotionZ() != 0)
                    		{
                    			if(playerCart.getMotionZ() < 0)
                    				playerCart.setZ(bb.c + emptyCart.getMotionZ());
                    			else
                    				playerCart.setZ(bb.f + emptyCart.getMotionZ());
                    		}
                		}
                		else if(minecartCollisionType == MinecartCollisionType.PLOW
                				&& !UtilEntity.isDead(emptyCart.getEntity())
                				&& emptyCart.getType() != Minecart.Type.StorageCart)
                		{
                			int item = ItemType.MINECART;
                			if(emptyCart.getType() == Minecart.Type.PoweredMinecart)
                				item = ItemType.POWERED_MINECART;
                			
                			emptyCart.destroy();
                			if(vehicle.isEmpty())
                			{
                				playerCart.getPassenger().giveItem(new Item(item, 1));
                			}
                			else
                			{
                				vehicle.getPassenger().giveItem(new Item(item, 1));
                			}
                			
                			emptyCart.getEntity().bw.c(0, 0, 0, 0, 0, 0);
                		}
                	}
                	
                	return true;
                }
    		}
    	}
    	else if(minecartCollisionType.REQUIRES_OWORLD && usingMinecartOWorldFile)
    	{
    		if(minecartCollisionType == MinecartCollisionType.NONE)
    		{
    			return true;
    		}
    		else if( (minecartCollisionType == MinecartCollisionType.PHASE || minecartCollisionType == MinecartCollisionType.PHASE_PLOW)
    				&& (!minecart1.isEmpty()
    					|| (collisioner.getEntity() instanceof OEntityMinecart && UtilEntity.riddenByEntity(collisioner.getEntity()) != null)
    					|| (collisioner.isLiving() && UtilEntity.ridingEntity(collisioner.getEntity()) instanceof OEntityMinecart)
    					)
    				)
    		{
    			if(minecartCollisionType == MinecartCollisionType.PHASE_PLOW && collisioner.getEntity() instanceof OEntityMinecart)
    			{
    				Minecart minecart2 = new Minecart((OEntityMinecart)collisioner.getEntity());
    				plowMinecart(minecart1, minecart2);
    			}
    			
    			return true;
    		}
    		else if(minecartCollisionType == MinecartCollisionType.SMASH
    				|| minecartCollisionType == MinecartCollisionType.SMASH_SCALED
    				|| minecartCollisionType == MinecartCollisionType.RAM
    				|| minecartCollisionType == MinecartCollisionType.RAM_SCALED
    				|| minecartCollisionType == MinecartCollisionType.NO_MERCY
    				|| minecartCollisionType == MinecartCollisionType.NO_MERCY_SCALED
    				)
    		{
    			if(collisioner.getEntity() instanceof OEntityMinecart
    				&& minecartCollisionType != MinecartCollisionType.SMASH
    				&& minecartCollisionType != MinecartCollisionType.SMASH_SCALED
    				)
    			{
    				Minecart minecart2 = new Minecart((OEntityMinecart)collisioner.getEntity());
    				plowMinecart(minecart1, minecart2);
    			}
    			else if(collisioner.isLiving() && !(UtilEntity.ridingEntity(collisioner.getEntity()) instanceof OEntityMinecart))
    			{
    				double s = minecart1.getMotionX() * minecart1.getMotionX()
							+ minecart1.getMotionZ() * minecart1.getMotionZ();
					s = Math.sqrt(s);
					
					if(s <= 0.40000000000000001D)
					{
						return false;
					}
					
    				if( (!collisioner.isPlayer()
    						|| minecart1.getPassenger() == null
    						|| minecart1.getPassenger().getEntity().hashCode() != collisioner.getEntity().hashCode()
    						)
    					&& (!collisioner.isPlayer()
    						|| minecartCollisionType == MinecartCollisionType.NO_MERCY
    						|| minecartCollisionType == MinecartCollisionType.NO_MERCY_SCALED
    						)
    					)
	    			{
	    				int damage = 20;
	    				
	    				if(minecartCollisionType == MinecartCollisionType.SMASH_SCALED
	    					|| minecartCollisionType == MinecartCollisionType.RAM_SCALED
	    					|| minecartCollisionType == MinecartCollisionType.NO_MERCY_SCALED)
	    				{
	    					damage = (int)Math.ceil((s / minecartMaxSpeed) * 20 * 2);
	    				}
	    				
	    				collisioner.getEntity().a(ODamageSource.k, damage);
	    			}
    			}
    			
    			return true;
    		}
    	}
    	
        return false;
    }
    
    private void plowMinecart(Minecart minecart1, Minecart minecart2)
    {
    	if(minecart1.getType() != Minecart.Type.StorageCart && minecart2.getType() != Minecart.Type.StorageCart
			&& ((minecart1.getPassenger() == null) ^ (minecart2.getPassenger() == null))
			)
		{
			int item = ItemType.MINECART;
			Player player;
			
			if(minecart1.isEmpty())
			{
				if(minecart1.getType() == Minecart.Type.PoweredMinecart)
    				item = ItemType.POWERED_MINECART;
				
				minecart1.destroy();
				player = minecart2.getPassenger();
			}
			else
			{
				if(minecart2.getType() == Minecart.Type.PoweredMinecart)
    				item = ItemType.POWERED_MINECART;
				
				minecart2.destroy();
				player = minecart1.getPassenger();
			}
			
			player.giveItem(new Item(item, 1));
		}
    }
    
    /**
     * Called on plugin unload.
     */
    public void disable() {
        if (decayWatcher != null) {
            decayWatcher.disable();
        }
    }
    
    /**
     * Get the controller sign for a block type. The coordinates provided
     * are those of the block (signs are to be underneath). The provided
     * text must be on the second line of the sign.
     * 
     * @param pt
     * @param text
     * @return
     */
    private Sign getControllerSign(World world, Vector pt, String text) {
        return getControllerSign(world, pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ(), text);
    }
    
    /**
     * Get the controller sign for a block type. The coordinates provided
     * are those of the block (signs are to be underneath). The provided
     * text must be on the second line of the sign.
     * 
     * @param x
     * @param y
     * @param z
     * @param text
     * @return
     */
    private Sign getControllerSign(World world, int x, int y, int z, String text) {
        ComplexBlock cblock = world.getComplexBlock(x, y - 1, z);

        if (cblock instanceof Sign
                && ((Sign)cblock).getText(1).equalsIgnoreCase(text)) {
            return (Sign)cblock;
        }
        
        cblock = world.getComplexBlock(x, y - 2, z);

        if (cblock instanceof Sign
                && ((Sign)cblock).getText(1).equalsIgnoreCase(text)) {
            return (Sign)cblock;
        }
        
        return null;
    }
    
    /**
     * Returns true if a filter line satisfies the conditions.
     * 
     * @param line
     * @param minecart
     * @param trackPos
     * @return
     */
    public boolean satisfiesCartSort(String line, Minecart minecart, Vector trackPos) {
    	if(line.length() == 0)
    		return false;
    	
        Player player = minecart.getPassenger();
        
        if (line.equalsIgnoreCase("All")) {
            return true;
        }
        
        if ((line.equalsIgnoreCase("Unoccupied")
                || line.equalsIgnoreCase("Empty"))
                && minecart.isEmpty()) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Storage")
                && minecart.getType() == Minecart.Type.StorageCart) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Powered")
                && minecart.getType() == Minecart.Type.PoweredMinecart) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Minecart")
                && minecart.getType() == Minecart.Type.Minecart) {
            return true;
        }
        
        if ((line.equalsIgnoreCase("Occupied")
                || line.equalsIgnoreCase("Full"))
                && !minecart.isEmpty()) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Animal")
                && UtilEntity.riddenByEntity(minecart.getEntity()) instanceof OEntityAnimal) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Mob")
                && (UtilEntity.riddenByEntity(minecart.getEntity()) instanceof OEntityMob
                        || UtilEntity.riddenByEntity(minecart.getEntity()) instanceof OEntityPlayer)) {
            return true;
        }
        
        if ((line.equalsIgnoreCase("Player")
                || line.equalsIgnoreCase("Ply"))
                && minecart.getPassenger() != null) {
            return true;
        }
        
        if (player != null) {
            String stop = stopStation.get(player.getName());
            if (stop != null && stop.equalsIgnoreCase(line)) {
                return true;
            }
        }
        
        String[] parts = line.split(":");
        
        if (parts.length >= 2) {
            if (player != null && parts[0].equalsIgnoreCase("Held")) {
            	int[] info = getItemInfoFromParts(parts);
            	if(info == null)
            		return false;
            	
            	OItemStack iStack = player.getEntity().k.d();
            	if(iStack != null && player.getItemInHand() >= 0
            	   && contentEqualsItem(player.getItemInHand(), iStack.h(), iStack.a, info))
            	{
            		return true;
            	}
            } else if (player != null && parts[0].equalsIgnoreCase("Group")) {
                if (player.isInGroup(parts[1])) {
                    return true;
                }
            } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
                if (parts[1].equalsIgnoreCase(player.getName())) {
                    return true;
                }
            } else if (player != null && parts[0].equalsIgnoreCase("INV")) {
            	Inventory inv = player.getInventory();
            	if(inv == null)
            		return false;
            	
            	int[] info = getItemInfoFromParts(parts);
            	if(info == null)
            		return false;
            	
            	if(contentsHasItems(inv.getContents(), info))
        			return true;
            	
            } else if (minecart.getType() == Minecart.Type.StorageCart
            		&& ( parts[0].equalsIgnoreCase("SCI")
            				|| parts[0].equalsIgnoreCase("SCI+") )
            		) {
            	
            	int[] info = getItemInfoFromParts(parts);
            	if(info == null)
            		return false;
            	
                StorageMinecart storage = minecart.getStorage();
                if(storage != null)
                {
                	if(parts[0].equalsIgnoreCase("SCI"))
                	{
                		//get from first slot
                		Item scItem = storage.getItemFromSlot(0);
                		if(scItem != null
                			&& contentEqualsItem(scItem.getItemId(), scItem.getDamage(), scItem.getAmount(), info))
                    	{
                    		return true;
                    	}
                	}
                	else if(parts[0].equalsIgnoreCase("SCI+"))
                	{
                		//get from any match
                		Item[] items = storage.getContents();
                		if(contentsHasItems(items, info))
                			return true;
                	}
                }
            } else if (parts[0].equalsIgnoreCase("Mob")) {
                String testMob = parts[1];

                if (UtilEntity.riddenByEntity(minecart.getEntity()) instanceof OEntityLiving) {
                    Mob mob = new Mob((OEntityLiving)UtilEntity.riddenByEntity(minecart.getEntity()));
                    if (testMob.equalsIgnoreCase(mob.getName())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private int[] getItemInfoFromParts(String[] parts)
    {
    	if(parts == null || parts.length < 2)
    		return null;
    	
    	String[] parts2 = parts[1].split("@", 2);
    	int[] item = new int[3];
    	
    	try
    	{
			item[0] = Integer.parseInt(parts2[0]);
			item[1] = -1;
			if(parts2.length > 1)
			{
				item[1] = Integer.parseInt(parts2[1]);
				if(item[1] > 15 || item[1] < 0)
					return null;
			}
			
			item[2] = 1;
			if(parts.length > 2)
			{
				item[2] = Integer.parseInt(parts[2]);
			}
    	}
    	catch(NumberFormatException e)
    	{
    		return null;
    	}
		
		return item;
    }
    
    private boolean contentEqualsItem(int id, int color, int amount, int[] item)
    {
    	if(id == item[0]
		   && (item[1] == -1 || color == item[1])
		   && amount >= item[2])
    	{
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean contentsHasItems(Item[] items, int[] item)
    {
    	int foundAmt = 0;
        for (Item scItem : items)
        {
        	if(scItem != null
    			&& scItem.getItemId() == item[0]
    			&& (item[1] == -1 || scItem.getDamage() == item[1]) )
        	{
        		foundAmt += scItem.getAmount();
        		
        		if(foundAmt >= item[2])
        			return true;
        	}
        }
        
        return false;
    }
    
    private Minecart spawnMinecart(int worldType, double x, double y, double z, int type)
    {
    	OWorldServer oworld = CraftBook.getOWorldServer(worldType);
    	OEntityMinecart oentity = new OEntityMinecart(oworld, x, y, z, type);
    	UtilEntity.spawnEntityInWorld(oworld, oentity);
    	
    	return new Minecart(oentity);
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        lastMinecartMsg.remove(player.getName());
        lastMinecartMsgTime.remove(player.getName());
        stopStation.remove(player.getName());
    }
}
