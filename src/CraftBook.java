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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.state.StateManager;

/**
 * Entry point for the plugin for hey0's mod.
 *
 * @author sk89q
 */
public class CraftBook extends Plugin {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    private static final File pathToState = new File("world"+File.separator+"craftbook");
    
    /**
     * Listener for the plugin system. This listener handles configuration
     * loading and the bulk of the core functions for CraftBook. Individual
     * features are implemented in the delegate listeners.
     */
    private final CraftBookListener listener =
            new CraftBookListener(this);
    
    /**
     * Tick delayer instance used to delay some events until the next tick.
     * It is used mostly for redstone-related events.
     */
    private final TickDelayer[] delays = new TickDelayer[]{new TickDelayer(), new TickDelayer(), new TickDelayer()};

    /**
     * Used to fake the data value at a point. For the redstone hook, because
     * the data value has not yet been set when the hook is called, its data
     * value is faked by CraftBook. As all calls to get a block's data are
     * routed through CraftBook already, this makes this hack feasible.
     */
    private static Map<Integer, FakeData> fakeData = new HashMap<Integer, FakeData>();
    
    /**
     * CraftBook version, fetched from the .jar's manifest. Used to print the
     * CraftBook version in various places.
     */
    private String version;
    
    /**
     * State manager object.
     */
    private StateManager stateManager = new StateManager();
    
    /**
     * State manager thread. 
     */
    private Thread stateThread = new Thread() {
        public void run() {
            PluginLoader l = etc.getLoader();
            while(l.getPlugin("CraftBook")==CraftBook.this) {
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    try {Thread.sleep(10*60*1000);} catch (InterruptedException e) {}
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    stateManager.save(pathToState);
            }
        }
    };

    /**
     * Delegate listener for mechanisms.
     */
    private final CraftBookDelegateListener mechanisms =
            new MechanismListener(this, listener);
    /**
     * Delegate listener for redstone.
     */
    private final CraftBookDelegateListener redstone =
            new RedstoneListener(this, listener);
    /**
     * Delegate listener for vehicle.
     */
    private final CraftBookDelegateListener vehicle =
            new VehicleListener(this, listener);
    
    private PluginInterface cbRequest = new CBHookFunc();
    
    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        TickPatch.applyPatch();

        registerHook(listener, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(listener, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(listener, "REDSTONE_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(listener, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);

        registerHook(mechanisms, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_RIGHTCLICKED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SERVERCOMMAND", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "PLAYER_MOVE", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "DAMAGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(mechanisms);
        
        registerHook(redstone, "BLOCK_RIGHTCLICKED", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "SIGN_SHOW", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "BLOCK_BROKEN", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "BLOCK_PLACE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(redstone);

        registerHook(vehicle, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "BLOCK_PLACE", PluginListener.Priority.LOW);
        registerHook(vehicle, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_POSITIONCHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_UPDATE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DAMAGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_ENTERED", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_COLLISION", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(vehicle);
        
        PluginLoader loader = etc.getLoader();
        loader.addCustomListener(CBPluginInterface.cbSignMech);
        
        loader.addCustomListener(cbRequest);
        
        Redstone.outputLever = new OutputLever();
        TickPatch.setTickRunnable(Redstone.outputLever, 0);
        
        //for(int i = 0; i < delays.length; i++)
        	//TickPatch.addTask(TickPatch.wrapRunnable(this, delays[i], i), i);
        TickPatch.addTask(TickPatch.wrapRunnable(this, delays[0], 0), 0);
        
        pathToState.mkdirs();
        stateManager.load(pathToState);
        
        stateThread.setName("StateManager");
        stateThread.start();
        
        CBWarp.reload();
    }

    /**
     * Conditionally registers a hook for a listener.
     * 
     * @param name
     * @param priority
     * @return whether the hook was registered correctly
     */
    public boolean registerHook(PluginListener listener,
            String name, PluginListener.Priority priority) {
        try {
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(name);
            etc.getLoader().addListener(hook, listener, this, priority);
            return true;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "CraftBook: Missing hook " + name + "!");
            return false;
        }
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        logger.log(Level.INFO, "CraftBook version " + getVersion() + " loaded");

        // This will also fire the loadConfiguration() methods of delegates
        listener.loadConfiguration();
        
        SignPatch.applyPatch();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        
        /*
        // funny old code from sk89q
        // this was to stop the MinecartMania plugin from disabling CraftBook.
        // Instead of being disabled from MinecartMania, this disables MinecartMania
        // and enables CraftBook again.
        
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            if (element.getClassName().contains("MinecartMania")) {
                etc.getServer().addToServerQueue(new Runnable() {
                    public void run() {
                        try {
                            etc.getLoader().disablePlugin("MinecartMania");
                            logger.warning("Minecart Mania has been disabled.");
                        } finally {
                            etc.getLoader().enablePlugin("CraftBook");
                        }
                    }
                });
                
                return;
            }
        }
        */
    	
    	OWorldServer[] oworlds = etc.getMCServer().e;
        for(int i = 0; i < oworlds.length; i++)
        {
    		for(@SuppressWarnings("rawtypes")
    		Iterator it = oworlds[i].g.iterator(); it.hasNext();)
    		{
    			Object obj = it.next();
    			if(obj instanceof EntitySitting)
    			{
    				((EntitySitting)obj).T();
    			}
    		}
        }

        SignPatch.removePatch();
        stateManager.save(pathToState);
        
        listener.disable();
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    public String getVersion() {
        if (version != null) {
            return version;
        }
        
        Package p = CraftBook.class.getPackage();
        
        if (p == null) {
            p = Package.getPackage("com.sk89q.craftbook");
        }
        
        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();
            
            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    public TickDelayer getDelay(int worldIndex) {
    	if(worldIndex < 0 || worldIndex >= delays.length)
    		return null;
    	
    	//[TODO]: change if ever needed multi-world delay lists
    	worldIndex = 0;
    	
        return delays[worldIndex];
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }

    protected static int getBlockID(int worldType, int x, int y, int z) {
        return getBlockID(getWorld(worldType), x, y, z);
    }

    protected static int getBlockID(int worldType, Vector pt) {
        return getBlockID(getWorld(worldType), pt);
    }
    
    protected static int getBlockID(World world, int x, int y, int z) {
    	return world.getBlockIdAt(x, y, z);
    }
    
    protected static int getBlockID(World world, Vector pt) {
        return world.getBlockIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    protected static int getBlockData(int worldType, int x, int y, int z) {
    	return getBlockData(getWorld(worldType), worldType, new BlockVector(x, y, z));
    }

    protected static int getBlockData(int worldType, Vector pt) {
    	return getBlockData(getWorld(worldType), worldType, pt.toBlockVector());
    }
    
    protected static int getBlockData(World world, int x, int y, int z) {
    	return getBlockData(world, world.getType().getId(), new BlockVector(x, y, z));
    }

    protected static int getBlockData(World world, Vector pt) {
    	return getBlockData(world, world.getType().getId(), pt.toBlockVector());
    }
    
    protected static int getBlockData(World world, int worldType, BlockVector bVec)
    {
    	FakeData fdata = fakeData.get(worldType);
        if (fdata != null && fdata.pos.equals(bVec))
        {
            return fdata.val;
        }
        return world.getBlockData(bVec.getBlockX(), bVec.getBlockY(), bVec.getBlockZ());
    }

    protected static boolean setBlockID(int worldType, int x, int y, int z, int type) {
    	return setBlockID(getWorld(worldType), x, y, z, type);
    }

    protected static boolean setBlockID(int worldType, Vector pt, int type) {
        return setBlockID(worldType, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }
    
    protected static boolean setBlockID(World world, Vector pt, int type) {
        return setBlockID(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }
    
    protected static boolean setBlockID(World world, int x, int y, int z, int type) {
        if (y < 127 && BlockType.isBottomDependentBlock(getBlockID(world, x, y + 1, z))) {
            world.setBlockAt(0, x, y + 1, z);
        }
        return world.setBlockAt(type, x, y, z);
    }

    protected static boolean setBlockData(int worldType, int x, int y, int z, int data) {
        return setBlockData(getWorld(worldType), x, y, z, data);
    }

    protected static boolean setBlockData(int worldType, Vector pt, int data) {
        return setBlockData(worldType, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }
    
    protected static boolean setBlockData(World world, Vector pt, int data) {
        return setBlockData(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }
    
    protected static boolean setBlockData(World world, int x, int y, int z, int data) {
        return world.setBlockData(x, y, z, data);
    }
    
    protected static boolean setBlockIdAndData(int worldType, int x, int y, int z, int id, int data)
    {
    	return setBlockIdAndData(getWorld(worldType), x, y, z, id, data);
    }

    protected static boolean setBlockIdAndData(int worldType, Vector pt, int id, int data) {
        return setBlockIdAndData(worldType, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), id, data);
    }
    
    protected static boolean setBlockIdAndData(World world, Vector pt, int id, int data) {
        return setBlockIdAndData(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), id, data);
    }
    
    protected static boolean setBlockIdAndData(World world, int x, int y, int z, int id, int data)
    {
    	if(data == 0)
    		return setBlockID(world, x, y, z, id);
    	
    	boolean result = setBlockID(world, x, y, z, id);
    	setBlockData(world, x, y, z, data);
    	
        return result;
    }
    
    protected static OWorld getOWorld(int worldType) {
        return etc.getMCServer().a(worldType);
    }
    
    protected static OWorldServer getOWorldServer(int worldType) {
    	OWorld world = getOWorld(worldType);
    	if(world instanceof OWorldServer)
    		return (OWorldServer)world;
    	return null;
    }
    
    /*
     * @return Returns Minecraft's index for the OWorldServer array.
     * This is based on the Minecraft method:
     * MinecraftServer.a(int)
     * Note: may change with future updates due to obfuscation.
     */
    protected static int getWorldIndex(int worldType)
    {
    	if(worldType == -1)
    		return 1;
    	
    	return 0;
    }
    
    protected static World getWorld(int worldType) {
        return etc.getServer().getWorld(worldType);
    }
    
    protected static SignText getSignText(int worldType, Vector pt) {
        return getSignText(getWorld(worldType), pt);
    }
    
    protected static SignText getSignText(World world, Vector pt) {
        ComplexBlock cblock = world.getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (cblock instanceof Sign) {
            return new SignTextImpl((Sign)cblock);
        }
        
        return null;
    }

    public static void dropSign(int worldType, Vector pt) {
        dropSign(worldType, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    
    public static void dropSign(int worldType, int x, int y, int z) {
        dropSign(getWorld(worldType), x, y, z);
    }
    
    public static void dropSign(World world, Vector pt) {
        dropSign(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    
    public static void dropSign(World world, int x, int y, int z) {
    	world.setBlockAt(0, x, y, z);
    	world.dropItem(x, y, z, 323);
    }

    protected static void fakeBlockData(int worldType, int x, int y, int z, int data) {
    	fakeData.put(worldType, new FakeData(new BlockVector(x, y, z), data));
    }

    protected static void fakeBlockData(int worldType, Vector pt, int data) {
    	fakeData.put(worldType, new FakeData(pt.toBlockVector(), data));
    }

    protected static void clearFakeBlockData(int worldType) {
        fakeData.remove(worldType);
    }
    
    public static class FakeData
    {
    	public BlockVector pos;
    	
    	/**
         * Used to fake the data value at a point. See fakedataPos.
         */
    	public int val;
    	
    	public FakeData(){}
    	
    	public FakeData(BlockVector pos, int val)
    	{
    		this.pos = pos;
    		this.val = val;
    	}
    }
    
    //[TODO]: temporary fix for warping out of The End. Remove if Canary adds a work around to Minecraft's "feature"
    public static void teleportPlayer(Player player, Location location)
    {
    	boolean isEnd = player.getWorld().getType() == World.Type.END;
		
		player.teleportTo(location);
		
		if(isEnd && location.dimension != World.Type.END.getId())
		{
			World world = location.getWorld();
			boolean found = false;
			for(@SuppressWarnings("rawtypes")
    		Iterator it = world.getWorld().g.iterator(); it.hasNext();)
    		{
				Object obj = it.next();
    			
    			if(!(obj instanceof OEntityPlayerMP))
    			{
    				continue;
    			}
    			
    			if(((OEntityPlayerMP)obj).hashCode() == player.getEntity().hashCode())
    			{
    				found = true;
    				break;
    			}
    		}
			
			if(!found)
			{
				UtilEntity.spawnEntityInWorld(world.getWorld(), player.getEntity());
				player.getEntity().c(location.x, player.getY(), location.z, player.getRotation(), player.getPitch());
				world.getWorld().a(player.getEntity(), false);
			}
		}
    }
    
    /* Allows entities (Minecarts, items, Mobs, etc) to teleport to other worlds (Nether, The End, etc)
     * Also enables riders to teleport with the teleporting entity. Ex: Players riding minecarts.
     * 
     * NOTE: because of how Minecraft works, entities will be DELETED if teleported to an unloaded chunk!
     * 
     */
    public static void teleportEntity(BaseEntity entity, Location location)
    {
    	if(entity == null || UtilEntity.isDead(entity.getEntity()))
    		return;
    	
    	World oldWorld = entity.getWorld();
    	World.Type worldType = oldWorld.getType();
    	
    	if(worldType.getId() != location.dimension)
    	{
    		World newWorld = location.getWorld();
    		OEntity rider = UtilEntity.riddenByEntity(entity.getEntity());
    		
    		if(rider != null)
    		{
    			UtilEntity.mountEntity(rider, entity.getEntity());
    			if(rider instanceof OEntityPlayerMP)
    			{
    				CraftBook.teleportPlayer(new Player((OEntityPlayerMP)rider), location);
    			}
    			else
    			{
    				CraftBook.teleportEntity(new BaseEntity((OEntity)rider), location);
    			}
    		}
    		
    		oldWorld.getWorld().f(entity.getEntity());
    		entity.getEntity().bE = false;
    		
    		location.y += 0.6200000047683716D;
    		
    		oldWorld.getWorld().a(entity.getEntity(), false);
    		
    		entity.teleportTo(location);
    		UtilEntity.spawnEntityInWorld(newWorld.getWorld(), entity.getEntity());
    		newWorld.getWorld().a(entity.getEntity(), false);
    		
    		entity.getEntity().a(newWorld.getWorld());
    		
    		if(rider != null)
    		{
    			UtilEntity.mountEntity(rider, entity.getEntity());
    		}
    	}
    	
    	entity.teleportTo(location);
    }
}
