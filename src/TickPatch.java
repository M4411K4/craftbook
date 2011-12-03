/*    
Drop-in onTick hpok for hMod
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software. It comes withput any warranty, to
the extent permitted by applicable law. You can redistribute it
and/or modify it under the terms of the Do What The Fuck You Want
To Public License, Version 2, as published by Sam hpcevar. See
http://sam.zoy.org/wtfpl/COPYING for more details.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.server.MinecraftServer;

/**
 * <p>Allows plugins to define code to run every tick.</p>
 * 
 * <p>To use, define a Runnable object that will be run each tick, and call the
 *    following in the initialize methpd:</p>
 * 
 * <p>TickPatch.applyPatch();
 *    TickPatch.addTask(TickTask.wrapRunnable(this,onTick));</p>
 * 
 * @authpr Lymia
 */
public class TickPatch extends OEntityTracker {
    @SuppressWarnings("unused")
    private static final Object HP_PATCH_APPLIED = null;
    /**
     * Do not use directly.
     */
    @Deprecated
    public static final CopyOnWriteArrayList<Runnable> TASK_LIST = new CopyOnWriteArrayList<Runnable>();
    //public static final CopyOnWriteArrayList<Runnable> TASK_LIST_NETH = new CopyOnWriteArrayList<Runnable>();
    
    private static Class<OEntityTracker> CLASS = OEntityTracker.class;
    private static Field[] FIELDS = CLASS.getDeclaredFields();
    
    private final int WORLD_INDEX;
    
    private TickPatch(MinecraftServer arg0, OEntityTracker g, int index) {
        super(arg0, index);
        WORLD_INDEX = index;
        if(g.getClass()!=CLASS) throw new RuntimeException("unexpected type for im instance");
        for(Field f:FIELDS) try {
            if(Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            Object o = f.get(g);
            f.setAccessible(true);
            f.set(this, o);
        } catch (Exception e) {
            System.out.println("Failed to copy field: "+f.getName());
            e.printStackTrace();
        }
    }
    
    /**
     * The actual patch method.
     * Should not be called.
     */
    @Deprecated
    public void a() {
        super.a();
        if(WORLD_INDEX == 0)
        {
	        Runnable[] tasks = TASK_LIST.toArray(new Runnable[0]);
	        for(int i=0;i<tasks.length;i++) tasks[i].run();
        }
    }
    
    /**
     * Applies the patch, if not already applied.
     * Call before using addTask or getTaskList().
     */
    public static void applyPatch() {
        MinecraftServer s = etc.getServer().getMCServer();
        //for(int i = 0; i < s.k.length; i++)
        int i = 0;
        {
	        try {
	            s.m[i].getClass().getDeclaredField("HP_PATCH_APPLIED");
	        } catch (SecurityException e) {
	            throw new RuntimeException("unexpected error: cannot use reflection");
	        } catch (NoSuchFieldException e) {
	            s.m[i] = new TickPatch(s,s.m[i],i);
	        }
        }
    }
    /**
     * Adds a new task.
     */
    public static void addTask(Runnable r, int worldIndex) {
    	MinecraftServer s = etc.getServer().getMCServer();
    	
    	if(worldIndex < 0 || worldIndex >= s.m.length)
    		return;
    	
    	getTaskList(worldIndex).add(r);
    }
    /**
     * Retrieves the task list.
     */
    @SuppressWarnings("unchecked")
    public static CopyOnWriteArrayList<Runnable> getTaskList(int index) {
    	
    	//[TODO]: add multi-world tasks? Might use more resources than needed, so for now keep to one.
    	index = 0;
    	
        MinecraftServer s = etc.getServer().getMCServer();
        try {
            return (CopyOnWriteArrayList<Runnable>) s.m[index].getClass().getField("TASK_LIST").get(null);
        } catch (SecurityException e) {
            throw new RuntimeException("unexpected error: cannot use reflection");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("patch not applied");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        }
    }

    /**
     * Wraps a runnable to allow easier use by plugins.
     */
    public static Runnable wrapRunnable(final Plugin p, final Runnable r, final int worldIndex) {
        return new Runnable() {
            private PluginLoader l = etc.getLoader();
            //private MinecraftServer s = etc.getMCServer();
            public void run() {
            	
                CopyOnWriteArrayList<Runnable> taskList = getTaskList(worldIndex);
                if(l.getPlugin(p.getName())!=p)
                	while(taskList.contains(this))
                		getTaskList(worldIndex).remove(this);
                if(p.isEnabled()) r.run();
            }
        };
    }
}