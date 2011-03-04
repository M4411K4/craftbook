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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.MCX120;
import com.sk89q.craftbook.ic.MCX121;

/**
 * Listener for mechanisms.
 * 
 * @author sk89q
 */
public class MechanismListener extends CraftBookDelegateListener {
    /**
     * Tracks copy saves to prevent flooding.
     */
    private Map<String,Long> lastCopySave =
            new HashMap<String,Long>();
    
    private boolean checkPermissions;
    private boolean checkCreatePermissions;
    private boolean redstoneToggleAreas = true;
    private int maxToggleAreaSize;
    private int maxUserToggleAreas;
    private boolean useBookshelves = true;
    private String bookReadLine;
    private Cauldron cauldronModule;
    private boolean useElevators = true;
    private boolean useGates = true;
    private boolean redstoneGates = true;
    private boolean useLightSwitches = true;
    private boolean useBridges = true;
    private boolean redstoneBridges = true;
    private boolean useDoors = true;
    private boolean redstoneDoors = true;
    private boolean useHiddenSwitches = true;
    private boolean useToggleAreas;
    private boolean dropBookshelves = true;
    private double dropAppleChance = 0;
    private boolean enableAmmeter = true;
    private boolean usePageReader = true;
    private boolean usePageWriter = false;
    private int pageMaxCharacters = 400;
    private int maxPages = 20;
    private boolean usePageSwitches = true;

    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public MechanismListener(CraftBook craftBook, CraftBookListener listener) {
        super(craftBook, listener);
    }

    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        maxToggleAreaSize = Math.max(0, properties.getInt("toggle-area-max-size", 5000));
        maxUserToggleAreas = Math.max(0, properties.getInt("toggle-area-max-per-user", 30));
        
        if(properties.containsKey("pagereader-enable"))
        	usePageReader = properties.getBoolean("pagereader-enable", true);
        if(properties.containsKey("pagewriter-enable"))
        	usePageWriter = properties.getBoolean("pagewriter-enable", false);
        if(properties.containsKey("page-max-characters"))
        	pageMaxCharacters = properties.getInt("page-max-characters", 400);
        if(properties.containsKey("max-pages"))
        	maxPages = properties.getInt("max-pages", 20);
        if(properties.containsKey("page-hidden-switches-enable"))
        	usePageSwitches = properties.getBoolean("page-hidden-switches-enable", true);

        useBookshelves = properties.getBoolean("bookshelf-enable", true);
        bookReadLine = properties.getString("bookshelf-read-text", "You pick out a book...");
        useLightSwitches = properties.getBoolean("light-switch-enable", true);
        useGates = properties.getBoolean("gate-enable", true);
        redstoneGates = properties.getBoolean("gate-redstone", true);
        useElevators = properties.getBoolean("elevators-enable", true);
        useBridges = properties.getBoolean("bridge-enable", true);
        redstoneBridges = properties.getBoolean("bridge-redstone", true);
        useHiddenSwitches = properties.getBoolean("door-enable", true);
        Bridge.allowedBlocks = Util.toBlockIDSet(properties.getString("bridge-blocks", "4,5,20,43"));
        Bridge.maxLength = properties.getInt("bridge-max-length", 30);
        useDoors = properties.getBoolean("door-enable", true);
        redstoneDoors = properties.getBoolean("door-redstone", true);
        Door.allowedBlocks = Util.toBlockIDSet(properties.getString("door-blocks", "1,3,4,5,17,20,35,43,44,45,47,80,82"));
        Door.maxLength = properties.getInt("door-max-length", 30);
        dropBookshelves = properties.getBoolean("drop-bookshelves", true);
        try {
            dropAppleChance = Double.parseDouble(properties.getString("apple-drop-chance", "0.5")) / 100.0;
        } catch (NumberFormatException e) {
            dropAppleChance = -1;
            logger.warning("Invalid apple drop chance setting in craftbook.properties");
        }
        useHiddenSwitches = properties.getBoolean("hidden-switches-enable", true);
        useToggleAreas = properties.getBoolean("toggle-areas-enable", true);
        redstoneToggleAreas = properties.getBoolean("toggle-areas-redstone", true);
        checkPermissions = properties.getBoolean("check-permissions", false);
        checkCreatePermissions = properties.getBoolean("check-create-permissions", false);
        cauldronModule = null;
        enableAmmeter = properties.getBoolean("ammeter", true);

        loadCauldron();
    }
    
    /**
     * Load the cauldron.
     */
    private void loadCauldron() {
        if (properties.getBoolean("cauldron-enable", true)) {
            try {
                CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");

                if (recipes.size() != 0) {
                    cauldronModule = new Cauldron(recipes);
                    logger.info(recipes.size()
                            + " cauldron recipe(s) loaded");
                } else {
                    logger.warning("cauldron-recipes.txt had no recipes");
                }
            } catch (FileNotFoundException e) {
                logger.info("cauldron-recipes.txt not found: " + e.getMessage());
                try {
                    logger.info("Looked in: " + (new File(".")).getCanonicalPath());
                } catch (IOException ioe) {
                    // Eat error
                }
            } catch (IOException e) {
                logger.warning("cauldron-recipes.txt not loaded: " + e.getMessage());
            }
        } else {
            cauldronModule = null;
        }
    }

    /**
     * Called before the console command is parsed. Return true if you don't
     * want the server command to be parsed by the server.
     * 
     * @param split
     * @return false if you want the command to be parsed.
     */
    public boolean onConsoleCommand(String[] split) {
        if (split[0].equalsIgnoreCase("reload-cauldron")) {
            loadCauldron();
            return true;
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
    public void onDirectWireInput(final Vector pt,
            final boolean isOn, final Vector changed) {
        
        int type = CraftBook.getBlockID(pt);
        
        // Sign gates
        if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof Sign)) {
                return;
            }

            final Sign sign = (Sign)cblock;
            final String line2 = sign.getText(1);

            // Gate
            if (useGates && redstoneGates
                    && (line2.equalsIgnoreCase("[Gate]")
                    || line2.equalsIgnoreCase("[DGate]"))) {
                BlockBag bag = getBlockBag(pt);
                bag.addSourcePosition(pt);

                // A gate may toggle or not
                try {
                    GateSwitch.setGateState(pt, bag, isOn,
                            line2.equalsIgnoreCase("[DGate]"));
                } catch (BlockSourceException e) {
                }

            // Bridges
            } else if (useBridges != false
                    && redstoneBridges
                    && type == BlockType.SIGN_POST
                    && line2.equalsIgnoreCase("[Bridge]")) {
                craftBook.getDelay().delayAction(
                    new TickDelayer.Action(pt.toBlockVector(), 2) {
                        @Override
                        public void run() {
                            BlockBag bag = listener.getBlockBag(pt);
                            bag.addSourcePosition(pt);
                            
                            Bridge bridge = new Bridge(pt);
                            if (isOn) {
                                bridge.setActive(bag);
                            } else {
                                bridge.setInactive(bag);
                            }
                        }
                    });

            // Doors
            } else if (useDoors != false
                    && redstoneDoors
                    && type == BlockType.SIGN_POST
                    && (line2.equalsIgnoreCase("[Door Up]")
                        || line2.equalsIgnoreCase("[Door Down]"))) {
                craftBook.getDelay().delayAction(
                    new TickDelayer.Action(pt.toBlockVector(), 2) {
                        @Override
                        public void run() {
                            BlockBag bag = getBlockBag(pt);
                            bag.addSourcePosition(pt);
                            
                            Door door = new Door(pt);
                            if (isOn) {
                                door.setActive(bag);
                            } else {
                                door.setInactive(bag);
                            }
                        }
                    });

            // Toggle areas
            } else if (useToggleAreas && redstoneToggleAreas
                    && (line2.equalsIgnoreCase("[Toggle]")
                    || line2.equalsIgnoreCase("[Area]"))) {                
                craftBook.getDelay().delayAction(
                    new TickDelayer.Action(pt.toBlockVector(), 2) {
                        @Override
                        public void run() {
                            BlockBag bag = listener.getBlockBag(pt);
                            bag.addSourcePosition(pt);

                            ToggleArea area = new ToggleArea(pt, listener.getCopyManager());
                            
                            if (isOn) { 
                                area.setActive(bag);
                            } else {
                                area.setInactive(bag);
                            }
                        }
                    });
            } else if (usePageReader && usePageSwitches
                    && line2.equalsIgnoreCase("[Book][X]")) {
            	
            	Vector redstonept = Util.getWallSignBack(pt, -1);
            	if(changed.equals(redstonept)
            		&& CraftBook.getBlockID(redstonept) == BlockType.REDSTONE_WIRE)
            	{
            		craftBook.getDelay().delayAction(
            			new TickDelayer.Action(pt.toBlockVector(), 2) {
            				@Override
            				public void run() {
            					Vector blockpt = Util.getWallSignBack(pt, 1);
                	            
        		            	int x = blockpt.getBlockX();
        		                int y = blockpt.getBlockY();
        		                int z = blockpt.getBlockZ();
        		                String hiddenType = "[Book][X]";
        		                
        		                setHiddenSwitch(hiddenType, isOn, x, y - 1, z);
        		                setHiddenSwitch(hiddenType, isOn, x, y + 1, z);
        		                setHiddenSwitch(hiddenType, isOn, x - 1, y, z);
        		                setHiddenSwitch(hiddenType, isOn, x + 1, y, z);
        		                setHiddenSwitch(hiddenType, isOn, x, y, z - 1);
        		                setHiddenSwitch(hiddenType, isOn, x, y, z + 1);
        		                
        		                etc.getServer().updateBlockPhysics(x, y+1, z, CraftBook.getBlockData(x, y+1, z));
        		                etc.getServer().updateBlockPhysics(x, y-1, z, CraftBook.getBlockData(x, y-1, z));
                            }
                        });
            	}
            }
        }
    }

    /**
     * Called when a block is hit with the primary attack.
     * 
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(Player player, Block block) {
        // Random apple drops
        if (dropAppleChance > 0 && block.getType() == BlockType.LEAVES
                && checkPermission(player, "/appledrops")) {
            if (block.getStatus() == 3 || block.getStatus() == 2) {
                if (Math.random() <= dropAppleChance) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            ItemType.APPLE);
                }
            }

        // Bookshelf drops and Page reset
        } else if (block.getType() == BlockType.BOOKCASE) {
        	
            if (dropBookshelves && block.getStatus() == 3 && checkPermission(player, "/bookshelfdrops")) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            BlockType.BOOKCASE);
            }
            else if(usePageReader && block.getStatus() == 0 && checkPermission(player, "/readpages"))
            {
            	//page reset
            	Sign sign = Util.getWallSignNextTo(block.getX(), block.getY(), block.getZ());
            	
            	if(sign != null && (sign.getText(1).equalsIgnoreCase("[Book]") || sign.getText(1).equalsIgnoreCase("[Book][X]")) )
            	{
            		PageWriter.resetPage(sign);
            	}
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
        CraftBookPlayer ply = new CraftBookPlayerImpl(player);
        SignTextImpl signText = new SignTextImpl(sign);
        Vector pt = new Vector(sign.getX(), sign.getY(), sign.getZ());
        
        String line2 = sign.getText(1);
        
        // Gate
        if (line2.equalsIgnoreCase("[Gate]") || line2.equalsIgnoreCase("[DGate]")) {
            if (checkCreatePermissions && !player.canUseCommand("/makegate")) {
                player.sendMessage(Colors.Rose
                        + "You don't have permission to make gates.");
                CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                return true;
            }
            
            sign.setText(1, line2.equalsIgnoreCase("[Gate]") ? "[Gate]" : "[DGate]");
            sign.update();
            
            listener.informUser(player);
            
            if (useGates) {
                player.sendMessage(Colors.Gold + "Gate created!");
            } else {
                player.sendMessage(Colors.Rose + "Gates are disabled on this server.");
            }
            
        // Light switch
        } else if (line2.equalsIgnoreCase("[|]")
                || line2.equalsIgnoreCase("[I]")) {
            if (checkCreatePermissions && !player.canUseCommand("/makelightswitch")) {
                player.sendMessage(Colors.Rose
                        + "You don't have permission to make light switches.");
                CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                return true;
            }
            
            sign.setText(1, "[I]");
            sign.update();
            
            listener.informUser(player);
            
            if (useLightSwitches) {
                player.sendMessage(Colors.Gold + "Light switch created!");
            } else {
                player.sendMessage(Colors.Rose + "Light switches are disabled on this server.");
            }

        // Elevator
        } else if (line2.equalsIgnoreCase("[Lift Up]")
                || line2.equalsIgnoreCase("[Lift Down]")
                || line2.equalsIgnoreCase("[Lift]")) {
            if (checkCreatePermissions && !player.canUseCommand("/makeelevator")) {
                player.sendMessage(Colors.Rose
                        + "You don't have permission to make elevators.");
                CraftBook.dropSign(sign.getX(), sign.getY(), sign.getZ());
                return true;
            }

            if (line2.equalsIgnoreCase("[Lift Up]")) {
                sign.setText(1, "[Lift Up]");
            } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                sign.setText(1, "[Lift Down]");
            } else if (line2.equalsIgnoreCase("[Lift]")) {
                sign.setText(1, "[Lift]");
            }
            sign.update();
            
            listener.informUser(player);
            
            if (useElevators) {
                if (line2.equalsIgnoreCase("[Lift Up]")) {
                    if (Elevator.hasLinkedLift(pt, true)) {
                        player.sendMessage(Colors.Gold
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.Gold
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                    if (Elevator.hasLinkedLift(pt, false)) {
                        player.sendMessage(Colors.Gold
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.Gold
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                } else if (line2.equalsIgnoreCase("[Lift]")) {
                    if (Elevator.hasLinkedLift(pt, true)
                            || Elevator.hasLinkedLift(pt, false)) {
                        player.sendMessage(Colors.Gold
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.Gold
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                }
            } else {
                player.sendMessage(Colors.Rose + "Elevators are disabled on this server.");
            }
        
        // Toggle areas
        } else if (line2.equalsIgnoreCase("[Area]")
                || line2.equalsIgnoreCase("[Toggle]")) {
            listener.informUser(player);
            
            if (useToggleAreas) {
                if (hasCreatePermission(ply, "maketogglearea")
                        && ToggleArea.validateEnvironment(ply, pt, signText)) {
                    signText.flushChanges();
                } else {
                    CraftBook.dropSign(pt);
                }
            } else {
                ply.printError("Area toggles are disabled on this server.");
            }

        // Bridges
        } else if (line2.equalsIgnoreCase("[Bridge]")) {
            listener.informUser(player);

            if (useBridges) {
                if (hasCreatePermission(ply, "makebridge")
                        && Bridge.validateEnvironment(ply, pt, signText)) {
                    signText.flushChanges();
                } else {
                    CraftBook.dropSign(pt);
                }
            } else {
                player.sendMessage(Colors.Rose + "Bridges are disabled on this server.");
            }
            
         // Page Reader
        } else if (line2.equalsIgnoreCase("[Book]") || line2.equalsIgnoreCase("[Book][X]")) {
            listener.informUser(player);
            
            if (usePageReader)
            {
            	//PageWriter.validateEnvironment() handles the permissions.
            	if(PageWriter.validateEnvironment(ply, pt, signText))
            	{
            		signText.flushChanges();
            	}
            	else
            	{
                    CraftBook.dropSign(pt);
            	}
            }
            else
            {
                ply.printError("Page readers are disabled on this server.");
            }
            
        // Doors
        } else if (line2.equalsIgnoreCase("[Door Up]")
                || line2.equalsIgnoreCase("[Door Down]")) {
            listener.informUser(player);

            if (useDoors) {
                if (hasCreatePermission(ply, "makedoor")
                        && Door.validateEnvironment(ply, pt, signText)) {
                    signText.flushChanges();
                } else {
                    CraftBook.dropSign(pt);
                }
            } else {
                player.sendMessage(Colors.Rose + "Doors are disabled on this server.");
            }
        }

        return false;
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    @Override
    public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
        try {
            // Discriminate against attempts that would actually place blocks
            boolean isPlacingBlock = item.getItemId() >= 1
                    && item.getItemId() <= 256;
            // 1 to work around empty hands bug in hMod
            
            if (!isPlacingBlock) {
                handleBlockUse(player, blockClicked, item.getItemId());
            }
        } catch (OutOfBlocksException e) {
            player.sendMessage(Colors.Rose + "Uh oh! Ran out of: " + Util.toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby block sources have the necessary");
            player.sendMessage(Colors.Rose + "materials.");
        } catch (OutOfSpaceException e) {
            player.sendMessage(Colors.Rose + "No room left to put: " + Util.toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby block sources have free slots.");
        } catch (BlockSourceException e) {
            player.sendMessage(Colors.Rose + "Error: " + e.getMessage());
        }
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    private boolean handleBlockUse(Player player, Block blockClicked,
            int itemInHand)
            throws BlockSourceException {

        int current = -1;

        // Ammeter
        if (enableAmmeter && itemInHand == 263) { // Coal
            int type = blockClicked.getType();
            int data = CraftBook.getBlockData(blockClicked.getX(),
                    blockClicked.getY(), blockClicked.getZ());
            
            if (type == BlockType.LEVER) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_TORCH_ON) {
                current = 15;
            } else if (type == BlockType.REDSTONE_TORCH_OFF) {
                current = 0;
            } else if (type == BlockType.STONE_BUTTON) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_WIRE) {
                current = data;
            }

            if (current > -1) {
                player.sendMessage(Colors.Yellow + "Ammeter: "
                        + Colors.Yellow + "["
                        + Colors.Yellow + Util.repeatString("|", current)
                        + Colors.Black + Util.repeatString("|", 15 - current)
                        + Colors.Yellow + "] "
                        + Colors.White
                        + current + " A");
            } else {
                player.sendMessage(Colors.Yellow + "Ammeter: " + Colors.Red + "Not supported.");
            }

            return false;
        }

        int plyX = (int)Math.floor(player.getLocation().x);
        int plyY = (int)Math.floor(player.getLocation().y);
        int plyZ = (int)Math.floor(player.getLocation().z);
        
        // Page reading
        if(usePageReader
        		&& blockClicked.getType() == BlockType.BOOKCASE
        		&& checkPermission(player, "/readpages")
        		)
        {
        	Sign sign = Util.getWallSignNextTo(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
        	
        	if(sign != null && (sign.getText(1).equalsIgnoreCase("[Book]") || sign.getText(1).equalsIgnoreCase("[Book][X]")) )
        	{
            	PageWriter.readPage(player, sign);
            	
            	if(usePageSwitches && sign.getText(1).equalsIgnoreCase("[Book][X]"))
            	{
            		int x = blockClicked.getX();
                    int y = blockClicked.getY();
                    int z = blockClicked.getZ();
                    String type = "[Book][X]";
                    
                    toggleHiddenSwitch(type, x, y - 1, z);
                    toggleHiddenSwitch(type, x, y + 1, z);
                    toggleHiddenSwitch(type, x - 1, y, z);
                    toggleHiddenSwitch(type, x + 1, y, z);
                    toggleHiddenSwitch(type, x, y, z - 1);
                    toggleHiddenSwitch(type, x, y, z + 1);
                    
                    etc.getServer().updateBlockPhysics(x, y+1, z, CraftBook.getBlockData(x, y+1, z));
	                etc.getServer().updateBlockPhysics(x, y-1, z, CraftBook.getBlockData(x, y-1, z));
            	}
            	
            	return true;
        	}
        }

        // Book reading
        if (useBookshelves
                && blockClicked.getType() == BlockType.BOOKCASE
                && checkPermission(player, "/readbooks")) {
            BookReader.readBook(player, bookReadLine);
            return true;

        // Sign buttons
        } else if (blockClicked.getType() == BlockType.WALL_SIGN ||
                blockClicked.getType() == BlockType.SIGN_POST ||
                CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN ||
                CraftBook.getBlockID(plyX, plyY, plyZ) == BlockType.WALL_SIGN) {
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            // Because sometimes the player is *inside* the block with a sign,
            // it becomes impossible for the player to select the sign but
            // may try anyway, so we're fudging detection for this case
            Vector pt;
            if (blockClicked.getType() == BlockType.WALL_SIGN
                    || blockClicked.getType() == BlockType.SIGN_POST) {
                pt = new Vector(x, y, z);
            } else if (CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN) {
                pt = new Vector(plyX, plyY + 1, plyZ);
                x = plyX;
                y = plyY + 1;
                z = plyZ;
            } else {
                pt = new Vector(plyX, plyY, plyZ);
                x = plyX;
                y = plyY;
                z = plyZ;
            }

            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;
                String line2 = sign.getText(1);

                // Gate
                if (useGates
                        && (line2.equalsIgnoreCase("[Gate]")
                                || line2.equalsIgnoreCase("[DGate]"))
                        && checkPermission(player, "/gate")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);

                    // A gate may toggle or not
                    if (GateSwitch.toggleGates(pt, bag,
                            line2.equalsIgnoreCase("[DGate]"))) {
                        player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
                    }
                
                // Light switch
                } else if (useLightSwitches &&
                        (line2.equalsIgnoreCase("[|]") || line2.equalsIgnoreCase("[I]"))
                        && checkPermission(player, "/lightswitch")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);
                    
                    return LightSwitch.toggleLights(pt, bag);

                // Elevator
                } else if (useElevators
                        && (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]"))
                        && checkPermission(player, "/elevator")) {

                    // Go up or down?
                    boolean up = line2.equalsIgnoreCase("[Lift Up]");
                    Elevator.performLift(player, pt, up);
                    
                    return true;

                // Toggle areas
                } else if (useToggleAreas != false
                        && (line2.equalsIgnoreCase("[Toggle]")
                        || line2.equalsIgnoreCase("[Area]"))
                        && checkPermission(player, "/togglearea")) {
                    
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);
                    
                    ToggleArea area = new ToggleArea(pt, listener.getCopyManager());
                    area.playerToggle(new CraftBookPlayerImpl(player), bag);

                    // Tell the player of missing blocks
                    Map<Integer,Integer> missing = bag.getMissing();
                    if (missing.size() > 0) {
                        for (Map.Entry<Integer,Integer> entry : missing.entrySet()) {
                            player.sendMessage(Colors.Rose + "Missing "
                                    + entry.getValue() + "x "
                                    + Util.toBlockName(entry.getKey()));
                        }
                    }
                    
                    return true;

                // Bridges
                } else if (useBridges
                        && blockClicked.getType() == BlockType.SIGN_POST
                        && line2.equalsIgnoreCase("[Bridge]")
                        && checkPermission(player, "/bridge")) {
                    
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);
                    
                    Bridge bridge = new Bridge(pt);
                    bridge.playerToggleBridge(new CraftBookPlayerImpl(player), bag);
                    
                    return true;

                // Doors
                } else if (useDoors
                        && blockClicked.getType() == BlockType.SIGN_POST
                        && (line2.equalsIgnoreCase("[Door Up]")
                                || line2.equalsIgnoreCase("[Door Down]"))
                        && checkPermission(player, "/door")) {
                    
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);
                    
                    Door door = new Door(pt);
                    door.playerToggleDoor(new CraftBookPlayerImpl(player), bag);
                    
                    return true;
                }
            }

        // Cauldron
        } else if (cauldronModule != null
                && checkPermission(player, "/cauldron")) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            cauldronModule.preCauldron(new Vector(x, y, z), player);

        }

        // Hidden switches
        if (useHiddenSwitches
                && itemInHand <= 0
                && blockClicked.getType() != BlockType.SIGN_POST
                && blockClicked.getType() != BlockType.WALL_SIGN
                && !BlockType.isRedstoneBlock(blockClicked.getType())) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();
            String type = "[X]";

            toggleHiddenSwitch(type, x, y - 1, z);
            toggleHiddenSwitch(type, x, y + 1, z);
            toggleHiddenSwitch(type, x - 1, y, z);
            toggleHiddenSwitch(type, x + 1, y, z);
            toggleHiddenSwitch(type, x, y, z - 1);
            toggleHiddenSwitch(type, x, y, z + 1);
            
            return true;
        }

        return false;
    }
    
    /**
     * Toggle a hidden switch.
     * 
     * @param pt
     */
    private void toggleHiddenSwitch(String type, int x, int y, int z) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(x, y, z);
        
        if (cblock instanceof Sign) {
            Sign sign = (Sign)cblock;
            
            if (sign.getText(1).equalsIgnoreCase(type)) {
                Redstone.toggleOutput(new Vector(x, y - 1, z));
                Redstone.toggleOutput(new Vector(x, y + 1, z));
                Redstone.toggleOutput(new Vector(x - 1, y, z));
                Redstone.toggleOutput(new Vector(x + 1, y, z));
                Redstone.toggleOutput(new Vector(x, y, z - 1));
                Redstone.toggleOutput(new Vector(x, y, z + 1));
            }
        }
    }
    
    private void setHiddenSwitch(String type, boolean state, int x, int y, int z) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(x, y, z);
        
        if (cblock instanceof Sign) {
            Sign sign = (Sign)cblock;
            
            if (sign.getText(1).equalsIgnoreCase(type)) {
                Redstone.setOutput(new Vector(x, y - 1, z), state);
                Redstone.setOutput(new Vector(x, y + 1, z), state);
                Redstone.setOutput(new Vector(x - 1, y, z), state);
                Redstone.setOutput(new Vector(x + 1, y, z), state);
                Redstone.setOutput(new Vector(x, y, z - 1), state);
                Redstone.setOutput(new Vector(x, y, z + 1), state);
            }
        }
    }
    
    /**
     * Called when a command is run
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCheckedCommand(Player player, String[] split)
            throws InsufficientArgumentsException, LocalWorldEditBridgeException {
        
        if ((split[0].equalsIgnoreCase("/savearea")
                && Util.canUse(player, "/savearea"))
                || (split[0].equalsIgnoreCase("/savensarea")
                && Util.canUse(player, "/savensarea"))) {
            boolean namespaced = split[0].equalsIgnoreCase("/savensarea");
            
            Util.checkArgs(split, namespaced ? 2 : 1, -1, split[0]);

            String id;
            String namespace;
            
            if (namespaced) {
                id = Util.joinString(split, " ", 2);
                namespace = split[1];

                if (namespace.equalsIgnoreCase("@")) {
                    namespace = "global";
                } else {
                    if (!CopyManager.isValidNamespace(namespace)) {
                        player.sendMessage(Colors.Rose + "Invalid namespace name. For the global namespace, use @");
                        return true;
                    }
                    namespace = "~" + namespace;
                }
            } else {
                id = Util.joinString(split, " ", 1);
                String nameNamespace = player.getName();
                
                // Sign lines can only be 15 characters long while names
                // can be up to 16 characters long
                if (nameNamespace.length() > 15) {
                    nameNamespace = nameNamespace.substring(0, 15);
                }

                if (!CopyManager.isValidNamespace(nameNamespace)) {
                    player.sendMessage(Colors.Rose + "You have an invalid player name.");
                    return true;
                }
                
                namespace = "~" + nameNamespace;
            }

            if (!CopyManager.isValidName(id)) {
                player.sendMessage(Colors.Rose + "Invalid area name.");
                return true;
            }
            
            try {
                Vector min = LocalWorldEditBridge.getRegionMinimumPoint(player);
                Vector max = LocalWorldEditBridge.getRegionMaximumPoint(player);
                Vector size = max.subtract(min).add(1, 1, 1);

                // Check maximum size
                if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > maxToggleAreaSize) {
                    player.sendMessage(Colors.Rose + "Area is larger than allowed "
                            + maxToggleAreaSize + " blocks.");
                    return true;
                }
                
                // Check to make sure that a user doesn't have too many toggle
                // areas (to prevent flooding the server with files)
                if (maxUserToggleAreas >= 0 && !namespace.equals("global")) {
                    int count = listener.getCopyManager().meetsQuota(
                            namespace, id, maxUserToggleAreas);

                    if (count > -1) {
                        player.sendMessage(Colors.Rose + "You are limited to "
                                + maxUserToggleAreas + " toggle area(s). You have "
                                + count + " areas.");
                        return true;
                    }
                }

                // Prevent save flooding
                Long lastSave = lastCopySave.get(player.getName());
                long now = System.currentTimeMillis();
                
                if (lastSave != null) {
                    if (now - lastSave < 1000 * 3) {
                        player.sendMessage(Colors.Rose + "Please wait before saving again.");
                        return true;
                    }
                }
                
                lastCopySave.put(player.getName(), now);
                
                // Copy
                CuboidCopy copy = new CuboidCopy(min, size);
                copy.copy();
                
                logger.info(player.getName() + " saving toggle area with folder '"
                        + namespace + "' and ID '" + id + "'.");
                
                // Save
                try {
                    listener.getCopyManager().save(namespace, id, copy);
                    if (namespaced) {
                        player.sendMessage(Colors.Gold + "Area saved as '"
                                + id + "' under the specified namespace.");
                    } else {
                        player.sendMessage(Colors.Gold + "Area saved as '"
                                + id + "' under your player.");
                    }
                } catch (IOException e) {
                    player.sendMessage(Colors.Rose + "Could not save area: " + e.getMessage());
                }
            } catch (NoClassDefFoundError e) {
                player.sendMessage(Colors.Rose + "WorldEdit.jar does not exist in plugins/.");
            }

            return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx120") && player.canUseCommand("/mcx120"))
        {
        	//[NOTE]: kept here for now since it's just one IC. Should move to RedstoneListener
        	//if more are made.
        	
        	if(split.length < 2)
        	{
        		player.sendMessage(Colors.Gold + "Usage: /mcx120 [band name] <on/off/state>");
        		player.sendMessage(Colors.Rose + "You must specify a band name after /mcx120.");
        		player.sendMessage(Colors.Gold + "Optional \"on\" or \"off\" or \"state\". Will toggle if left blank.");
        	}
        	else
        	{
        		Boolean out = MCX120.airwaves.get(split[1]);
        		if(out == null)
        		{
        			player.sendMessage(Colors.Rose + "Could not find band name: "+split[1]);
        		}
        		else
        		{
        			if(split.length > 2)
        			{
        				if(split[2].equalsIgnoreCase("on"))
        					out = true;
        				else if(split[2].equalsIgnoreCase("off"))
        					out = false;
        				else if(split[2].equalsIgnoreCase("state"))
        				{
        					String state = out ? "on" : "off";
        					player.sendMessage(Colors.Gold + "Command IC "+split[1]+" current state: "+Colors.White+state);
        					return true;
        				}
        				else
        				{
        					player.sendMessage(Colors.Rose + "Unknown command option: "+split[2]);
        					return true;
        				}
        			}
        			else
        				out = !out;
        			
        			MCX120.airwaves.put(split[1], out);
        			
        			String state = out ? "on" : "off";
        			player.sendMessage(Colors.Gold + "Command IC turned: "+Colors.White+state);
        		}
        	}
        	
        	return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx120list") && player.canUseCommand("/mcx120list"))
        {
        	String out = "";
        	for (Map.Entry<String, Boolean> entry : MCX120.airwaves.entrySet())
        	{
        		String color;
        		Boolean state = entry.getValue();
        		
        		if(state == null)
        			color = Colors.Gray;
        		else
        			color = state ? Colors.Green : Colors.Red;
        		
        		out += " ["+color+entry.getKey()+Colors.White+"]";
        	}
        	if(out.length() == 0)
        		player.sendMessage(Colors.Red + "No command ICs found.");
        	else
        	{
        		player.sendMessage(out);
        	}
        	
        	return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx121") && player.canUseCommand("/mcx121"))
        {
        	//lol, nothing like adding more mess!
        	
        	if(split.length < 3)
        	{
        		player.sendMessage(Colors.Gold + "Usage: /mcx121 [band name] [password] <on/off/state>");
        		player.sendMessage(Colors.Rose + "You must specify a band name and password after /mcx121.");
        		player.sendMessage(Colors.Gold + "Optional \"on\" or \"off\" or \"state\". Will toggle if left blank.");
        	}
        	else
        	{
        		Boolean out = MCX121.airwaves.get(split[1]);
        		if(out == null)
        		{
        			player.sendMessage(Colors.Rose + "Could not find band name: "+split[1]);
        		}
        		else
        		{
        			Boolean ispass = MCX121Pass.isPassword(split[1], split[2]);
        			
        			if(ispass == null)
        			{
        				player.sendMessage(Colors.Rose + "Password file not found!");
        			}
        			else if(ispass)
        			{
        				if(split.length > 3)
            			{
            				if(split[3].equalsIgnoreCase("on"))
            					out = true;
            				else if(split[3].equalsIgnoreCase("off"))
            					out = false;
            				else if(split[3].equalsIgnoreCase("state"))
            				{
            					String state = out ? "on" : "off";
            					player.sendMessage(Colors.Gold + "Command IC "+split[1]+" current state: "+Colors.White+state);
            					return true;
            				}
            				else
            				{
            					player.sendMessage(Colors.Rose + "Unknown command option: "+split[3]);
            					return true;
            				}
            			}
            			else
            				out = !out;
            			
            			MCX121.airwaves.put(split[1], out);
            			
            			String state = out ? "on" : "off";
            			player.sendMessage(Colors.Gold + "Command IC turned: "+Colors.White+state);
        			}
        			else
            		{
            			player.sendMessage(Colors.Rose + "Incorrect password used!");
            			logger.log(Level.INFO, player.getName()+" used an incorrect mcx121 password.");
            		}
        		}
        		
        	}
        	
        	return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx121pass") && player.canUseCommand("/mcx121pass"))
        {
        	if(split.length < 3
        		|| (split[1].equalsIgnoreCase("add") && split.length < 4)
        		|| (split[1].equalsIgnoreCase("change") && split.length < 5) )
        	{
        		player.sendMessage(Colors.Gold + "Usage: adding or changing passwords for [MCX121]");
        		player.sendMessage(Colors.Gold + "/mcx121pass add [band name] [password]");
        		player.sendMessage(Colors.Gold + "/mcx121pass change [band name] [current pass] [new pass]");
        		player.sendMessage(Colors.Gold + "/mcx121pass has [band name]");
        	}
        	else
        	{
        		Boolean changed;
        		
        		if(split[1].equalsIgnoreCase("add"))
        		{
        			if(split[3].length() > 15 || split[3].length() < 3)
        			{
        				player.sendMessage(Colors.Rose + "Passwords must be 3 to 15 characters long.");
    					return true;
        			}
        			
        			changed = MCX121Pass.setPassword(split[2], split[3]);
        		}
        		else if(split[1].equalsIgnoreCase("change"))
        		{
        			if(split[4].length() > 15 || split[4].length() < 3)
        			{
        				player.sendMessage(Colors.Rose + "Passwords must be 3 to 15 characters long.");
    					return true;
        			}
        			
        			changed = MCX121Pass.setPassword(split[2], split[3], split[4]);
        		}
        		else if(split[1].equalsIgnoreCase("has"))
        		{
        			Boolean haspass = MCX121Pass.hasPassword(split[2]);
        			if(haspass == null)
        			{
        				player.sendMessage(Colors.Rose + "Could not find password file!");
        			}
        			else if(haspass)
        			{
        				player.sendMessage(Colors.Gold + "Password "+Colors.White+split[2]+Colors.Gold+" exists");
        			}
        			else
        			{
        				player.sendMessage(Colors.Gold + "Password "+Colors.White+split[2]+Colors.Gold+" does not exist");
        			}
        			return true;
        		}
        		else
        		{
        			player.sendMessage(Colors.Rose + "Unknown command option: "+split[1]);
					return true;
        		}
        		
        		if(changed == null)
        		{
        			player.sendMessage(Colors.Rose + "Could not edit or create the password file!");
        			logger.log(Level.INFO, player.getName()+" attempted to set mcx121 pass, but password"
        					+" file could not be changed or edited.");
        		}
        		else if(changed)
        		{
        			player.sendMessage(Colors.Gold + "> Password Set");
        			player.sendMessage(Colors.Rose + "NOTE: Passwords are "+Colors.White+"NOT"+Colors.Rose
        					+" encrypted! Admins "+Colors.White+"CAN"+Colors.Rose+" read them!");
        			
        			//logger.log(Level.INFO, player.getName()+" set mcx121 pass");
        		}
        		else
        		{
        			if(split[1].equalsIgnoreCase("change"))
        			{
        				player.sendMessage(Colors.Rose + "Failed to set password. Incorrect password entered.");
        			}
        			else
        			{
        				player.sendMessage(Colors.Rose + "Failed to set password. Password exists or invalid band name");
        				player.sendMessage(Colors.Rose + "Change existing passwords with:");
        				player.sendMessage(Colors.Rose + "/mcx121pass change [name] [current pass] [new pass]");
        			}
        			
        			logger.log(Level.INFO, player.getName()+" attempted to set a mcx121 password, but failed. Bad password?");
        		}
        	}
        	
        	return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx121remove") && player.canUseCommand("/mcx121remove"))
        {
        	if(split.length < 2)
        	{
        		player.sendMessage(Colors.Gold + "Usage: /mcx121remove [band name]");
        	}
        	else
        	{
        		Boolean removed = MCX121Pass.setPassword(split[1], "0", "", true);
        		
        		if(removed == null)
        		{
        			player.sendMessage(Colors.Rose + "Could not edit or create the password file!");
        			logger.log(Level.INFO, player.getName()+" attempted to remove mcx121 password, but could not "
        					+"edit or create the password file.");
        		}
        		else if(removed)
        		{
        			player.sendMessage(Colors.Gold + "Password "+Colors.White+split[1]+Colors.Gold+" removed!");
        			logger.log(Level.INFO, player.getName()+" removed mcx121 password");
        		}
        		else
        		{
        			player.sendMessage(Colors.Rose + "Failed to remove password: "+split[1]);
        			logger.log(Level.INFO, player.getName()+" failed to remove mcx121 password");
        		}
        	}
        	
        	return true;
        }
        else if(split[0].equalsIgnoreCase("/mcx121list") && player.canUseCommand("/mcx121list"))
        {
        	String out = "";
        	for (Map.Entry<String, Boolean> entry : MCX121.airwaves.entrySet())
        	{
        		String color;
        		Boolean state = entry.getValue();
        		
        		if(state == null)
        			color = Colors.Gray;
        		else
        			color = state ? Colors.Green : Colors.Red;
        		
        		out += " ["+color+entry.getKey()+Colors.White+"]";
        	}
        	if(out.length() == 0)
        		player.sendMessage(Colors.Red + "No command ICs found.");
        	else
        	{
        		player.sendMessage(out);
        	}
        	
        	return true;
        }
        else if(usePageWriter && split[0].equalsIgnoreCase("/cbpage") && player.canUseCommand("/cbpage"))
        {
        	PageWriter.handleCommand(player, split, pageMaxCharacters, maxPages);
        	return true;
        }
        else if(usePageWriter && split[0].equalsIgnoreCase("/admincbpage") && player.canUseCommand("/admincbpage"))
        {
        	PageWriter.handleNSCommand(player, split, pageMaxCharacters, maxPages);
        	return true;
        }

        return false;
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static CauldronCookbook readCauldronRecipes(String path)
            throws IOException {
        
        File file = new File(path);
        FileReader input = null;
        CauldronCookbook cookbook = new CauldronCookbook();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank lines
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
                    continue;
                }

                String[] parts = line.split(":");
                
                if (parts.length < 3) {
                    logger.log(Level.WARNING, "Invalid cauldron recipe line in "
                            + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<Integer> ingredients = parseCauldronItems(parts[1]);
                    List<Integer> results = parseCauldronItems(parts[2]);
                    String[] groups = null;
                    
                    if (parts.length >= 4 && parts[3].trim().length() > 0) {
                        groups = parts[3].split(",");
                    }
                    
                    CauldronRecipe recipe =
                            new CauldronRecipe(name, ingredients, results, groups);
                    cookbook.add(recipe);
                }
            }

            return cookbook;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Parse a list of cauldron items.
     * 
     * @param list
     * @return
     */
    private static List<Integer> parseCauldronItems(String list) {
        String[] parts = list.split(",");

        List<Integer> out = new ArrayList<Integer>();

        for (String part : parts) {
            int multiplier = 1;

            try {
                // Multiplier
                if (part.matches("^.*\\*([0-9]+)$")) {
                    int at = part.lastIndexOf("*");
                    multiplier = Integer.parseInt(
                            part.substring(at + 1, part.length()));
                    part = part.substring(0, at);
                }

                try {
                    for (int i = 0; i < multiplier; i++) {
                        out.add(Integer.valueOf(part));
                    }
                } catch (NumberFormatException e) {
                    int item = etc.getDataSource().getItem(part);

                    if (item > 0) {
                        for (int i = 0; i < multiplier; i++) {
                            out.add(item);
                        }
                    } else {
                        logger.log(Level.WARNING, "Cauldron: Unknown item " + part);
                    }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                logger.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
            }
        }

        return out;
    }
    
    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkPermission(Player player, String command) {
        return !checkPermissions || player.canUseCommand(command);
    }
    
    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean hasCreatePermission(CraftBookPlayer player, String permission) {
        if (!checkCreatePermissions || player.hasPermission(permission)) {
            return true;
        } else {
            player.printError("You don't have permission to make that.");
            return false;
        }
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        lastCopySave.remove(player.getName());
    }
}
