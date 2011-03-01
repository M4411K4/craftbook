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
    private boolean minecartCollisionTypeHelper = false;
    
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
    
    private int minecartCollisionType = 0;

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
        if(properties.containsKey("minecart-enable-collision-type-helper"))
        	minecartCollisionTypeHelper = properties.getBoolean("minecart-enable-collision-type-helper", false);
        
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
        	String mct = properties.getString("minecart-collision-type", "default");
        	if(mct.equalsIgnoreCase("ghost"))
        		minecartCollisionType = 1;
        	else if(mct.equalsIgnoreCase("plow"))
        		minecartCollisionType = 2;
        	else
        		minecartCollisionType = 0;
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
        		player.getEntity().aE instanceof OEntityMinecart
        		)
        {
        	OEntityMinecart eminecart = (OEntityMinecart)player.getEntity().aE;
        	
        	Vector blockpt = new Vector((int)Math.floor(eminecart.aJ), (int)Math.floor(eminecart.aK), (int)Math.floor(eminecart.aL));
        	
        	int under = CraftBook.getBlockID(blockpt.getBlockX(), blockpt.getBlockY() - 1, blockpt.getBlockZ());
        	int underColor = CraftBook.getBlockData(blockpt.getBlockX(), blockpt.getBlockY() - 1, blockpt.getBlockZ());
        	
        	if(under == minecartDirectionBlock[0] && underColor == minecartDirectionBlock[1])
        	{
        		//make sure tracks are straight? probably doesn't matter, so I'll leave it out for now.
        		Vector point = Util.getFrontPoint(player.getRotation(), blockpt.getBlockX(), blockpt.getBlockY(), blockpt.getBlockZ());
    			if(CraftBook.getBlockID(point) != BlockType.MINECART_TRACKS)
    			{
    				player.sendMessage(Colors.Rose+"There are no connected tracks to go that way.");
    			}
    			else
    			{
    				eminecart.b(point.getX(), point.getY(), point.getZ(), player.getRotation(), 0);
        			eminecart.aM = (point.getX() - blockpt.getBlockX()) * 0.3;
    				eminecart.aN = 0.0;
    				eminecart.aO = (point.getZ() - blockpt.getBlockZ()) * 0.3;
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
    public void onDirectWireInput(Vector pt, boolean isOn, Vector changed) {
        int type = CraftBook.getBlockID(pt);
        
        // Minecart dispenser
        if (minecartDispensers && type == BlockType.CHEST
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            
            // Rising edge-triggered only
            if (!isOn) {
                return;
            }
            
            Vector signPos = pt.add(0, -2, 0);

            Sign sign = getControllerSign(pt.add(0, -1, 0), "[Dispenser]");
            
            if (sign == null) {
                return;
            }

            String collectType = sign != null ? sign.getText(2) : "";
            boolean push = sign != null ? sign.getText(3).contains("Push") : false;

            Vector dir = Util.getSignPostOrthogonalBack(signPos, 1)
                    .subtract(signPos);
            Vector depositPt = pt.add(dir.multiply(2.5));

            /*if (CraftBook.getBlockID(depositPt) != BlockType.MINECART_TRACKS) {
                return;
            }*/

            NearbyChestBlockBag blockBag = new NearbyChestBlockBag(pt);
            blockBag.addSingleSourcePosition(pt);
            blockBag.addSingleSourcePosition(pt.add(1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(-1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(0, 0, 1));
            blockBag.addSingleSourcePosition(pt.add(0, 0, -1));

            try {
                Minecart minecart;
                
                if (collectType.equalsIgnoreCase("Storage")) {
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
                    
                    minecart = new Minecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            Minecart.Type.StorageCart);
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
                    
                    minecart = new Minecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            Minecart.Type.PoweredMinecart);
                } else {
                    blockBag.fetchBlock(ItemType.MINECART);
                    minecart = new Minecart(
                            depositPt.getX(),
                            depositPt.getY(),
                            depositPt.getZ(),
                            Minecart.Type.Minecart);
                }
                
                if (push) {
                    int data = CraftBook.getBlockData(signPos);
                    
                    if (data == 0x0) {
                        minecart.setMotion(0, 0, -0.3);
                    } else if (data == 0x4) {
                        minecart.setMotion(0.3, 0, 0);
                    } else if (data == 0x8) {
                        minecart.setMotion(0, 0, 0.3);
                    } else if (data == 0xC) {
                        minecart.setMotion(-0.3, 0, 0);
                    }
                }
            } catch (BlockSourceException e) {
                // No minecarts
            }
        // Minecart station
        } else if (minecartControlBlocks && type == minecartStationBlock[0]
                && CraftBook.getBlockData(pt) == minecartStationBlock[1]
                && CraftBook.getBlockID(pt.add(0, 1, 0)) == BlockType.MINECART_TRACKS
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());

            // Maybe it's the sign directly below
            if (cblock == null || !(cblock instanceof Sign)) {
                cblock = etc.getServer().getComplexBlock(
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
            int data = CraftBook.getBlockData(
                    pt.getBlockX(), cblock.getY(), pt.getBlockZ());
            
            if (data == 0x0) {
                motion = new Vector(0, 0, -0.3);
            } else if (data == 0x4) {
                motion = new Vector(0.3, 0, 0);
            } else if (data == 0x8) {
                motion = new Vector(0, 0, 0.3);
            } else if (data == 0xC) {
                motion = new Vector(-0.3, 0, 0);
            } else {
                return;
            }

            for (BaseEntity ent : etc.getServer().getEntityList()) {
                if (ent instanceof Minecart) {
                    Minecart minecart = (Minecart)ent;
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

            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);
            int underColor = CraftBook.getBlockData(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                // Overflow prevention
                if (Math.abs(minecart.getMotionX()) > 100) {
                    minecart.setMotionX(Math.signum(minecart.getMotionX()) * 100);
                }
                
                if (Math.abs(minecart.getMotionZ()) > 100) {
                    minecart.setMotionZ(Math.signum(minecart.getMotionZ()) * 100);
                }
                
                if (under == minecart25xBoostBlock[0] && underColor == minecart25xBoostBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 1.25);
                        minecart.setMotionZ(minecart.getMotionZ() * 1.25);
                        return;
                    }
                } else if (under == minecart100xBoostBlock[0] && underColor == minecart100xBoostBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 2);
                        minecart.setMotionZ(minecart.getMotionZ() * 2);
                        return;
                    }
                } else if (under == minecart50xSlowBlock[0] && underColor == minecart50xSlowBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.5);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.5);
                        return;
                    }
                } else if (under == minecart20xSlowBlock[0] && underColor == minecart20xSlowBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.8);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.8);
                        return;
                    }
                } else if (under == minecartReverseBlock[0] && underColor == minecartReverseBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        Vector signPos = new Vector(blockX, blockY - 2, blockZ);
                        boolean reverseX = true;
                        boolean reverseZ = true;

                        // Directed reverse block
                        if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                && Util.doesSignSay(signPos, 1, "[Reverse]")) {
                            Vector dir = Util.getSignPostOrthogonalBack(signPos, 1)
                                    .subtract(signPos);

                            // Acceptable sign direction
                            if (dir != null) {
                                if (MathUtil.isSameSign(minecart.getMotionX(),
                                        dir.getBlockX())) {
                                    reverseX = false;
                                }
                                if (MathUtil.isSameSign(minecart.getMotionZ(),
                                        dir.getBlockZ())) {
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
                } else if (under == minecartDepositBlock[0] && underColor == minecartDepositBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        if (minecart.getType() == Minecart.Type.StorageCart) {
                            Vector pt = new Vector(blockX, blockY, blockZ);
                            NearbyChestBlockBag bag = new NearbyChestBlockBag(pt);

                            for (int y = -1; y <= 0; y++) {
                                bag.addSingleSourcePosition(pt.add(1, y, 0));
                                bag.addSingleSourcePosition(pt.add(2, y, 0));
                                bag.addSingleSourcePosition(pt.add(-1, y, 0));
                                bag.addSingleSourcePosition(pt.add(-2, y, 0));
                                bag.addSingleSourcePosition(pt.add(0, y, 1));
                                bag.addSingleSourcePosition(pt.add(0, y, 2));
                                bag.addSingleSourcePosition(pt.add(0, y, -1));
                                bag.addSingleSourcePosition(pt.add(0, y, -2));
                            }
                            
                            if (bag.getChestBlockCount() > 0) {
                                if (getControllerSign(pt.add(0, -1, 0), "[Deposit]") != null) {
                                    ItemArrayUtil.moveChestBagToItemArray(
                                            minecart.getStorage(), bag);
                                } else {
                                    ItemArrayUtil.moveItemArrayToChestBag(
                                            minecart.getStorage(), bag);
                                }
                            }
                        }
                    }

                    return;
                } else if (under == minecartEjectBlock[0] && underColor == minecartEjectBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        Player player = minecart.getPassenger();
                        if (player != null) {
                            // Let's find a place to put the player
                            Location loc = new Location(blockX, blockY, blockZ);
                            Vector signPos = new Vector(blockX, blockY - 2, blockZ);

                            if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                    && Util.doesSignSay(signPos, 1, "[Eject]")) {
                                Vector pos = Util.getSignPostOrthogonalBack(signPos, 1);

                                // Acceptable sign direction
                                if (pos != null) {
                                    pos = pos.setY(blockY);

                                    // Is the spot free?
                                    if (BlockType.canPassThrough(CraftBook.getBlockID(pos.add(0, 1, 0)))
                                            && BlockType.canPassThrough(CraftBook.getBlockID(pos))) {
                                        loc = new Location(
                                                pos.getBlockX(),
                                                pos.getBlockY(),
                                                pos.getBlockZ());

                                        ComplexBlock cBlock = etc.getServer().getComplexBlock(
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

                            player.teleportTo(loc);
                        }
                    }

                    return;
                } else if (under == minecartSortBlock[0] && underColor == minecartSortBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test == null || test) {
                        Sign sign = getControllerSign(blockX, blockY - 1, blockZ, "[Sort]");
                        
                        if (sign != null) {
                            SortDir dir = SortDir.FORWARD;

                            if (satisfiesCartSort(sign.getText(2), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.LEFT;
                            } else if (satisfiesCartSort(sign.getText(3), minecart,
                                    new Vector(blockX, blockY, blockZ))) {
                                dir = SortDir.RIGHT;
                            }

                            int signData = CraftBook.getBlockData(
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
                                    && CraftBook.getBlockID(targetTrack) == BlockType.MINECART_TRACKS) {
                                CraftBook.setBlockData(targetTrack, newData);
                            }
                        }
                    }

                    return;
                } else if (under == minecartDirectionBlock[0] && underColor == minecartDirectionBlock[1]
                           && minecart.getPassenger() != null) {
	                Boolean test = Redstone.testAnyInput(underPt);
	
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
                } else if (under == minecartLaunchBlock[0] && underColor == minecartLaunchBlock[1]) {
                	
                	Sign sign = getControllerSign(blockX, blockY - 1, blockZ, "[Launch]");
                	if(sign != null)
                	{
		                Boolean test = Redstone.testAnyInput(underPt);
		                
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
		                		int data = CraftBook.getBlockData(sign.getX(), sign.getY(), sign.getZ());
		                		
		                		Vector motion = null;
		                		if (data == 0x0) {
		        	                motion = new Vector(0, 0, -0.3);
		        	            } else if (data == 0x4) {
		        	                motion = new Vector(0.3, 0, 0);
		        	            } else if (data == 0x8) {
		        	                motion = new Vector(0, 0, 0.3);
		        	            } else if (data == 0xC) {
		        	                motion = new Vector(-0.3, 0, 0);
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
                } else if (minecartEnableLoadBlock
                			&& under == minecartLoadBlock[0] && underColor == minecartLoadBlock[1]
                            && minecart.isEmpty()
                            && minecart.getType().getType() == 0) {
	                Boolean test = Redstone.testAnyInput(underPt);
	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(blockX, blockY - 1, blockZ, "[Load]");
                        
	                	OEntityMinecart eminecart = minecart.getEntity();
	                	OEntityPlayer eplayer = null;
	                	
	                	final double DIST = 3.0D;
	                	
                        if (sign != null)
                        {
                        	//load from only a certain direction
                        	int data = CraftBook.getBlockData(sign.getX(), sign.getY(), sign.getZ());
                        	
                        	double closeDist = -1.0D;
                        	OWorld world = eminecart.aF;
                        	for(int i = 0; i < world.d.size(); i++)
                        	{
                        		OEntityPlayer tmpplayer = (OEntityPlayer) world.d.get(i);
                        		double d2 = tmpplayer.d(eminecart.aJ, eminecart.aK, eminecart.aL);
                        		
                        		if( (d2 < DIST * DIST) && ((closeDist == -1.0D) || (d2 < closeDist))
                        			&& ( (data == 0x0 && tmpplayer.aL >= eminecart.aL)
                        				|| (data == 0x4 && tmpplayer.aJ <= eminecart.aJ)
                        				|| (data == 0x8 && tmpplayer.aL <= eminecart.aL)
                        				|| (data == 0xC && tmpplayer.aJ >= eminecart.aJ)
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
                        	eplayer = eminecart.aF.a(eminecart, DIST);
                        }
                        
                        if (eplayer != null)
                    	{
                    		eplayer.b(eminecart);
                    	}
	                }
                }
            }

            if (minecartTrackMessages
                    && CraftBook.getBlockID(underPt.add(0, -1, 0)) == BlockType.SIGN_POST) {
                Vector signPos = underPt.add(0, -1, 0);
                
                Boolean test = Redstone.testAnyInput(signPos);

                if (test == null || test) {
                    ComplexBlock cblock = etc.getServer().getComplexBlock(
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

            if (minecartDispensers) {
                Vector pt = new Vector(blockX, blockY, blockZ);
                Vector depositPt = null;

                if (CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(-1, -1, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(-1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(1, 0, 0);
                } else if (CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(1, -1, 0)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(1, 1, 0)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(-1, 0, 0);
                } else if (CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, -1, -1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, 1, -1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, 1);
                } else if (CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.CHEST
                        && (CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, -1, 1)) == BlockType.MINECART_TRACKS
                        || CraftBook.getBlockID(pt.add(0, 1, 1)) == BlockType.MINECART_TRACKS)) {
                    depositPt = pt.add(0, 0, -1);
                }

                if (depositPt != null) {
                    Sign sign = getControllerSign(depositPt.add(0, -1, 0), "[Dispenser]");
                    String collectType = sign != null ? sign.getText(2) : "";
                    
                    NearbyChestBlockBag blockBag = new NearbyChestBlockBag(depositPt);
                    blockBag.addSingleSourcePosition(depositPt);
                    blockBag.addSingleSourcePosition(depositPt.add(1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(-1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, 1));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, -1));

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

                            if (collectType.equalsIgnoreCase("Storage")) {
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
                            if (collectType.equalsIgnoreCase("Powered")) {
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

            if(minecartCollisionTypeHelper)
            {
            	OEntityMinecart ecart = minecart.getEntity();
            	@SuppressWarnings("rawtypes")
				List localList = ecart.aF.b(ecart, ecart.aT.b(0.2000000029802322D, 0.0D, 0.2000000029802322D));
                if ((localList != null) && (localList.size() > 0))
                    for (int i3 = 0; i3 < localList.size(); i3++) {
                        OEntity localOEntity = (OEntity) localList.get(i3);
                        if ((localOEntity != ecart.aD) && (localOEntity.e_()) && ((localOEntity instanceof OEntityMinecart)))
                        {
                        	vehicleCollision(vehicle, localOEntity);
                        }
                    }
            }
            
            int blockX = (int)Math.floor(minecart.getX());
            int blockY = (int)Math.floor(minecart.getY());
            int blockZ = (int)Math.floor(minecart.getZ());
            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);
            int underColor = CraftBook.getBlockData(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == minecartStationBlock[0] && underColor == minecartStationBlock[1]) {
                    Boolean test = Redstone.testAnyInput(underPt);

                    if (test != null) {
                        if (!test) {
                            minecart.setMotion(0, 0, 0);
                            return;
                        } else {
                            ComplexBlock cblock = etc.getServer().getComplexBlock(
                                    blockX, blockY - 2, blockZ);

                            // Maybe it's the sign directly below
                            if (cblock == null || !(cblock instanceof Sign)) {
                                cblock = etc.getServer().getComplexBlock(
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
                                int data = CraftBook.getBlockData(
                                        blockX, cblock.getY(), blockZ);
                                
                                if (data == 0x0) {
                                    motion = new Vector(0, 0, -0.3);
                                } else if (data == 0x4) {
                                    motion = new Vector(0.3, 0, 0);
                                } else if (data == 0x8) {
                                    motion = new Vector(0, 0, 0.3);
                                } else if (data == 0xC) {
                                    motion = new Vector(-0.3, 0, 0);
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
                else if(under == minecartLiftBlock[0] && underColor == minecartLiftBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(underPt);
                	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(blockX, blockY - 1, blockZ, "[CartLift]");
	                	
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
                            	for(int y = blockY + 1; y <= 127; y++)
                            	{
                            		if (CraftBook.getBlockID(blockX, y, blockZ) == minecartLiftBlock[0] &&
                            			CraftBook.getBlockData(blockX, y, blockZ) == minecartLiftBlock[1] &&
                            			CraftBook.getBlockID(blockX, y+1, blockZ) == BlockType.MINECART_TRACKS)
                            		{
                            			destSign = getControllerSign(blockX, y, blockZ, "[CartLift]");
                            			
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
                            		if (CraftBook.getBlockID(blockX, y, blockZ) == minecartLiftBlock[0] &&
                            			CraftBook.getBlockData(blockX, y, blockZ) == minecartLiftBlock[1] &&
                            			CraftBook.getBlockID(blockX, y+1, blockZ) == BlockType.MINECART_TRACKS)
                            		{
                            			destSign = getControllerSign(blockX, y, blockZ, "[CartLift]");
                            			
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
                            
                            if(!BlockType.canPassThrough(CraftBook.getBlockID(blockX, dest.getBlockY()+2, blockZ)))
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
                            	wantedDir = CraftBook.getBlockData(destSign.getX(), destSign.getY(), destSign.getZ());
                            	if(wantedDir != 0 && wantedDir != 4 && wantedDir != 8 && wantedDir != 12)
                            	{
                            		wantedDir = -1;
                            	}
                            }
                            
                            //if wantedDir == -1 then find a track that exists
                            if((wantedDir == -1 || wantedDir == 8)
                            		&& CraftBook.getBlockID(blockX, dest.getBlockY()+1, blockZ + 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(blockX, dest.getBlockY()+1, blockZ + 1) == 0)
                            {
                            	//west
                            	targetTrack = new Location(blockX, dest.getBlockY()+1, blockZ + 1, 0, 0);
                            	motion = new Vector(0, 0, 0.3);
                            }
                            if((wantedDir == -1 || wantedDir == 4)
                            		&& CraftBook.getBlockID(blockX + 1, dest.getBlockY()+1, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(blockX + 1, dest.getBlockY()+1, blockZ) == 1)
                            {
                            	//south
                            	targetTrack = new Location(blockX + 1, dest.getBlockY()+1, blockZ, 270, 0);
                            	motion = new Vector(0.3, 0, 0);
                            }
                            if((wantedDir == -1 || wantedDir == 0)
                            		&& CraftBook.getBlockID(blockX, dest.getBlockY()+1, blockZ - 1) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(blockX, dest.getBlockY()+1, blockZ - 1) == 0)
                            {
                            	//east
                            	targetTrack = new Location(blockX, dest.getBlockY()+1, blockZ - 1, 180, 0);
                            	motion = new Vector(0, 0, -0.3);
                            }
                            if((wantedDir == -1 || wantedDir == 12)
                            		&& CraftBook.getBlockID(blockX - 1, dest.getBlockY()+1, blockZ) == BlockType.MINECART_TRACKS
                            		&& CraftBook.getBlockData(blockX - 1, dest.getBlockY()+1, blockZ) == 1)
                            {
                            	//north
                            	targetTrack = new Location(blockX - 1, dest.getBlockY()+1, blockZ, 90, 0);
                            	motion = new Vector(-0.3, 0, 0);
                            }
                            
                            if(targetTrack != null)
                            {
                            	if(player != null && destSign != null && destSign.getText(0).length() > 0)
                            		player.sendMessage(Colors.Gold+destSign.getText(0));
                            	
                            	minecart.teleportTo(targetTrack);
                            	minecart.setMotion(motion.getX(), 0, motion.getZ());
                            }
                            else
                            {
                            	//spams... remove after this is no longer a new feature
                            	if(player != null)
                            		player.sendMessage(Colors.Rose+"No connected track found at lift.");
                            }
                            return;
                        }
	                }
                }
                else if(under == minecartDelayBlock[0] && underColor == minecartDelayBlock[1])
                {
                	Boolean test = Redstone.testAnyInput(underPt);
                	
	                if (test == null || test)
	                {
	                	Sign sign = getControllerSign(blockX, blockY - 1, blockZ, "[Delay]");
	                	
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
                        		int data = CraftBook.getBlockData(sign.getX(), sign.getY(), sign.getZ());
		                		
                        		Location targetTrack = null;
		                		Vector motion = null;
		                		if (data == 0x0)
		                		{
		                			targetTrack = new Location(blockX, blockY, blockZ - 1, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(0, 0, -0.3);
		        	            }
		                		else if (data == 0x4)
		        	            {
		                			targetTrack = new Location(blockX + 1, blockY, blockZ, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(0.3, 0, 0);
		        	            }
		        	            else if (data == 0x8)
		        	            {
		        	            	targetTrack = new Location(blockX, blockY, blockZ + 1, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(0, 0, 0.3);
		        	            }
		        	            else if (data == 0xC)
		        	            {
		        	            	targetTrack = new Location(blockX - 1, blockY, blockZ, minecart.getRotation(), minecart.getPitch());
		        	                motion = new Vector(-0.3, 0, 0);
		        	            }
		        	            else
		        	            {
		        	                return;
		        	            }
		                		
		                		if(motion != null)
		                		{
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
            }
            
            int block = CraftBook.getBlockID(blockX, blockY, blockZ);
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
        if (passenger != null && vehicle instanceof Minecart
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
                    vehicle.setMotion(-0.1, 0, 0);
                } else if(dir.equals("NE")) {
                    vehicle.setMotion(-0.1, 0, -0.1);
                } else if(dir.equals("E")) {
                    vehicle.setMotion(0, 0, -0.1);
                } else if(dir.equals("SE")) {
                    vehicle.setMotion(0.1, 0, -0.1);
                } else if(dir.equals("S")) {
                    vehicle.setMotion(0.1, 0, 0);
                } else if(dir.equals("SW")) {
                    vehicle.setMotion(0.1, 0, 0.1);
                } else if(dir.equals("W")) {
                    vehicle.setMotion(0, 0, 0.1);
                } else if(dir.equals("NW")) {
                    vehicle.setMotion(-0.1, 0, 0.1);
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
        
        if (blockPlaced.getType() == BlockType.MINECART_TRACKS) {
            int under = CraftBook.getBlockID(blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
            int underColor = CraftBook.getBlockData(blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
            
            if (minecartControlBlocks && under == minecartStationBlock[0] && underColor == minecartStationBlock[1]) {
                Sign sign = getControllerSign(blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ(), "[Station]");
                Vector pt = new Vector(blockPlaced.getX(),
                    blockPlaced.getY() - 1, blockPlaced.getZ());
                
                boolean needsRedstone = Redstone.testAnyInput(pt) == null;

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
                Sign sign = getControllerSign(blockPlaced.getX(),
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
            	Sign sign = getControllerSign(blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[CartLift]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "Cart Lift destination created. A [CartLift] sign is needed to lift up or down.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Cart Lift block created.");
                }
            } else if (minecartControlBlocks && under == minecartLaunchBlock[0] && underColor == minecartLaunchBlock[1]) {
            	Sign sign = getControllerSign(blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[Launch]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "A [Launch] sign is still needed.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Launch block created.");
                }
            } else if (minecartControlBlocks && under == minecartDelayBlock[0] && underColor == minecartDelayBlock[1]) {
            	Sign sign = getControllerSign(blockPlaced.getX(),
                        blockPlaced.getY() - 1, blockPlaced.getZ(), "[Delay]");
            	
                if (sign == null) {
                    player.sendMessage(Colors.Rose
                            + "A [Delay] sign is still needed.");
                } else {
                    player.sendMessage(Colors.Gold
                            + "Minecart Delay block created.");
                }
            } else if (minecartControlBlocks && minecartEnableLoadBlock && under == minecartLoadBlock[0]
                       && underColor == minecartLoadBlock[1]) {
            	player.sendMessage(Colors.Gold + "Minecart load block created.");
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
        int type = CraftBook.getBlockID(
                sign.getX(), sign.getY(), sign.getZ());

        String line1 = sign.getText(0);
        String line2 = sign.getText(1);

        // Station
        if (line2.equalsIgnoreCase("[Station]")) {
            listener.informUser(player);
            
            sign.setText(1, "[Station]");
            sign.update();
            
            if (minecartControlBlocks) {
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
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
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Sort sign detected.");
            } else {
                player.sendMessage(Colors.Rose
                        + "Minecart control blocks are disabled on this server.");
            }
        // Dispenser
        } else if (line2.equalsIgnoreCase("[Dispenser]")) {
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
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
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
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
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
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
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
                        CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                        return true;
                	}
                	
                	if(delay <= 0 || delay > 3600)
                	{
                		player.sendMessage(Colors.Rose + "Delay must be 1 to 3600");
                        CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
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
                int data = CraftBook.getBlockData(
                        sign.getX(), sign.getY(), sign.getZ());

                if (type == BlockType.WALL_SIGN) {
                    player.sendMessage(Colors.Rose + "The sign must be a sign post.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else if (data != 0x0 && data != 0x4 && data != 0x8 && data != 0xC) {
                    player.sendMessage(Colors.Rose + "The sign cannot be at an odd angle.");
                    CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                    return true;
                }
                
                player.sendMessage(Colors.Gold + "Load sign detected.");
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
            
            if(minecartCollisionType == 1)
    		{
            	OEntityMinecart ecart = (OEntityMinecart)vehicle.getEntity();
            	if(vehicle.getPassenger() != null)
                {
            		ecart.bc = 0.98F;
            		ecart.bd = 0.7F;
                }
            	else
            	{
            		ecart.bc = 0.001F;
            		ecart.bd = 0.001F;
            	}
    		}
            
            
            if (minecartDestroyOnExit && vehicle.getPassenger() != null && !vehicle.getEntity().ba) {
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
    	return vehicleCollision(vehicle, collisioner.getEntity());
    }
    
    private Boolean vehicleCollision(BaseVehicle vehicle, OEntity collisioner) {
    	
    	if(minecartCollisionType == 0)
    		return false;
    	
    	if(vehicle instanceof Minecart && collisioner instanceof OEntityMinecart)
    	{
    		if((collisioner.aD == null && vehicle.getPassenger() != null)
    			|| (vehicle.isEmpty() && collisioner.aD != null && BaseEntity.isPlayer(collisioner.aD))
    			)
    		{
                OEntity eplayercart;
                OEntity eemptycart;
                
                if(vehicle.isEmpty())
                {
                	eplayercart = collisioner;
                	eemptycart = vehicle.getEntity();
                }
                else
                {
                	eplayercart = vehicle.getEntity();
                	eemptycart = collisioner;
                }
    			
                double s = eplayercart.aM * eplayercart.aM + eplayercart.aO * eplayercart.aO;
                if(s > 0.0)
                {
                	double es = eemptycart.aM * eemptycart.aM + eemptycart.aO * eemptycart.aO;
                	if(s > es)
                	{
                		if(minecartCollisionType == 1)
                		{
                			OAxisAlignedBB bb = eemptycart.aT.b(0.2000000029802322D, 0.0D, 0.2000000029802322D);
                    		
                    		if(eplayercart.aM != 0)
                    		{
                    			if(eplayercart.aM < 0)
                    				eplayercart.aJ = bb.a + eemptycart.aM;
                    			else
                    				eplayercart.aJ = bb.d + eemptycart.aM;
                    		}
                    		if(eplayercart.aO != 0)
                    		{
                    			if(eplayercart.aO < 0)
                    				eplayercart.aL = bb.c + eemptycart.aO;
                    			else
                    				eplayercart.aL = bb.f + eemptycart.aO;
                    		}
                		}
                		else if(minecartCollisionType == 2 && !eemptycart.ba && ((OEntityMinecart)eemptycart).d != 1)
                		{
                			int item = ItemType.MINECART;
                			if(((OEntityMinecart)eemptycart).d == 2)
                				item = ItemType.POWERED_MINECART;
                			
                			eemptycart.C();
                			if(vehicle.isEmpty())
                			{
                				//wonder why hmod only gave us the BaseEntity
                				//would be nice to have access to the "cart" var....
                				for (BaseEntity ent : etc.getServer().getEntityList())
                				{
                	                if (ent instanceof Minecart && ent.getEntity().hashCode() == eplayercart.hashCode())
                	                {
                	                	((Minecart)ent).getPassenger().giveItem(new Item(item, 1));
                	                	break;
                	                }
                				}
                			}
                			else
                			{
                				vehicle.getPassenger().giveItem(new Item(item, 1));
                			}
                			
                			eemptycart.aT.c(0, 0, 0, 0, 0, 0);
                		}
                	}
                	
                	return true;
                }
    		}
    	}
    	
        return false;
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
    private Sign getControllerSign(Vector pt, String text) {
        return getControllerSign(pt.getBlockX(), pt.getBlockY(),
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
    private Sign getControllerSign(int x, int y, int z, String text) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(x, y - 1, z);

        if (cblock instanceof Sign
                && ((Sign)cblock).getText(1).equalsIgnoreCase(text)) {
            return (Sign)cblock;
        }
        
        cblock = etc.getServer().getComplexBlock(x, y - 2, z);

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
                && minecart.getEntity().aD instanceof OEntityAnimals) {
            return true;
        }
        
        if (line.equalsIgnoreCase("Mob")
                && (minecart.getEntity().aD instanceof OEntityMobs
                        || minecart.getEntity().aD instanceof OEntityPlayer)) {
            return true;
        }
        
        if ((line.equalsIgnoreCase("Player")
                || line.equalsIgnoreCase("Ply"))
                && minecart.getPassenger() != null) {
            return true;
        }
        
        if (player != null) {
            String stop = stopStation.get(player.getName());
            if (stop != null && stop.equals(line)) {
                return true;
            }
        }
        
        String[] parts = line.split(":");
        
        if (parts.length >= 2) {
            if (player != null && parts[0].equalsIgnoreCase("Held")) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    if (player.getItemInHand() == item) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                }
            } else if (player != null && parts[0].equalsIgnoreCase("Group")) {
                if (player.isInGroup(parts[1])) {
                    return true;
                }
            } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
                if (parts[1].equalsIgnoreCase(player.getName())) {
                    return true;
                }
            } else if (parts[0].equalsIgnoreCase("Mob")) {
                String testMob = parts[1];

                if (minecart.getEntity().aD instanceof OEntityLiving) {
                    Mob mob = new Mob((OEntityLiving)minecart.getEntity().aD);
                    if (testMob.equalsIgnoreCase(mob.getName())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
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
