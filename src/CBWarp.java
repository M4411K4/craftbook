import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sk89q.craftbook.WorldLocation;


public class CBWarp
{
	private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
	private static final String BASE_PATH = "world"+File.separator+"craftbook"+File.separator;
	private static final Map<String, CBWarpObject> warps = new HashMap<String, CBWarpObject>();
	private static final Map<String, CBWarpObject> passWarps = new HashMap<String, CBWarpObject>();
	
	public static enum WarpError
	{
		IO_ERROR (Colors.Rose+"an IO error occurred."),
		FILE_CREATION (Colors.Rose+"failed to create warp file."),
		DIR_CREATION (Colors.Rose+"failed to create warp directory."),
		FILE_NOT_FOUND (Colors.Rose+"warp file not found."),
		WARP_EXISTS (Colors.Rose+"warp already exists."),
		WARP_NOT_FOUND (Colors.Rose+"warp not found."),
		BAD_FILE (Colors.Rose+"warp file is corrupted."),
		PASS_SIZE (Colors.Rose+"invalid password size. Size must be 3 to 15 characters long"),
		INCORRECT_PASS (Colors.Rose+"incorrect password. (server logged)"),
		WARP_NAME_SIZE (Colors.Rose+"invalid warp name size. Size must be 1 to 15 characters long"),
		WARP_NAME_INVALID (Colors.Rose+"invalid warp name. Name must not be a number.");
		
		public final String MESSAGE;
		WarpError(String playerMessage)
		{
			MESSAGE = playerMessage;
		}
	}
	
	public static boolean warpExists(String name, boolean hasPass)
	{
		name = name.toLowerCase();
		if(hasPass)
			return passWarps.containsKey(name);
		else
			return warps.containsKey(name);
	}
	
	public static CBWarpObject getWarp(String name, boolean hasPass)
	{
		name = name.toLowerCase();
		if(hasPass)
			return passWarps.get(name);
		else
			return warps.get(name);
	}
	
	public static WarpError warp(Player player, String name, String pass)
	{
		boolean hasPass = pass != null && !pass.isEmpty();
		if(hasPass)
		{
			if(pass.length() < 3 || pass.length() > 15)
				return WarpError.PASS_SIZE;
			pass = encrypt(pass);
		}
		
		name = name.toLowerCase();
		
		CBWarpObject warpObj;
		if(hasPass)
			warpObj = passWarps.get(name);
		else
			warpObj = warps.get(name);
		
		if(warpObj == null)
			return WarpError.WARP_NOT_FOUND;
		
		if(hasPass && !pass.equals(warpObj.PASSWORD))
		{
			logger.warning(player.getName()+" used incorrect password while trying to warp to "+name);
			return WarpError.INCORRECT_PASS;
		}
		
		CraftBook.teleportPlayer(player,  warpObj.LOCATION);
		//player.teleportTo(warpObj.LOCATION);
		
		if(warpObj.MESSAGE == null || warpObj.MESSAGE.isEmpty())
			player.sendMessage(Colors.Rose+"Woosh!");
		else
		{
			String[] messages = warpObj.getMessage();
			for(String message : messages)
				player.sendMessage(Colors.Rose+message);
		}
		return null;
	}
	
	protected static String[] listWarps(int set, boolean passList)
	{
		String[] lines = new String[10];
		
		ArrayList<String> names = new ArrayList<String>();
		if(passList)
		{
			for(String name : passWarps.keySet())
				names.add(name);
		}
		else
		{
			for(String name : warps.keySet())
				names.add(name);
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		
		final int size = names.size();
		final int sets;
		if(size == 0)
			sets = 1;
		else
			sets = (int)Math.floor(size / lines.length) + 1;
		
		if(set > sets)
			set = sets;
		if(set <= 0)
			set = 1;
		
		lines[0] = Colors.Gold+"CBWarps [ "+Colors.White+set+"/"+sets+Colors.Gold+" ]";
		if(passList)
			lines[0] += " Protected List";
		
		set = (set - 1) * (lines.length - 1);
		for(int i = 1; i < lines.length; i++)
		{
			int index = set + i - 1;
			if(index >= size)
				break;
			
			CBWarpObject warpObj;
			String name = names.get(index);
			if(passList)
			{
				warpObj = passWarps.get(name);
				lines[i] = Colors.LightGreen;
			}
			else
			{
				warpObj = warps.get(name);
				lines[i] = Colors.LightBlue;
			}
			
			lines[i] += " "+name + Colors.Rose+" -";
			if(warpObj.TITLE == null || warpObj.TITLE.isEmpty())
				lines[i] += "<no title>";
			else
				lines[i] += warpObj.TITLE;
		}
		
		if(lines[1] == null)
			lines[1] = Colors.Rose+"<no warps found>";
		
		return lines;
	}
	
	/*
	 * @returns null if no error has occurred
	 */
	protected static WarpError addWarp(Player player, String name, WorldLocation location, String title, String pass)
	{
		if(name.isEmpty() || name.length() > 15)
			return WarpError.WARP_NAME_SIZE;
		
		if(name.matches("[0-9]+"))
			return WarpError.WARP_NAME_INVALID;
		
		name = name.toLowerCase();
		
		boolean hasPass = pass != null && !pass.isEmpty();
		if(hasPass)
		{
			if(pass.length() < 3 || pass.length() > 15)
				return WarpError.PASS_SIZE;
			pass = encrypt(pass);
		}
		
		if(warpExists(name, hasPass))
			return WarpError.WARP_EXISTS;
		
		CBWarpObject warpObj = new CBWarpObject(location, title, "", pass);
		
		String path = getPath(hasPass);
		File file = new File(path);
		boolean newFile = false;
		if (!file.exists())
		{
			File folder = file.getParentFile();
			if(!folder.exists()) 
			{
				if(!folder.mkdirs())
					return WarpError.DIR_CREATION;
			}
			
			try
			{
				file.createNewFile();
				newFile = true;
			}
			catch(IOException e)
			{
				return WarpError.FILE_CREATION;
			}
		}
		
		//skipping creating and using a tmp file as usual...
		
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		
		try
		{
	    	fis = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fis));
	    	
	    	String line = br.readLine();
	    	ArrayList<String> lines = new ArrayList<String>();
	    	while(line != null)
	    	{
	    		lines.add(line);
	    		line = br.readLine();
	    	}
	    	
	    	if(newFile)
	    	{
	    		lines.add("v1.0");
	    		lines.add("#");
	    	}
	    	
	    	int index = 2;
	    	for(; index < lines.size(); index++)
	    	{
	    		String[] params;
	    		if(hasPass)
	    			params = lines.get(index).split(":", 4);
	    		else
	    			params = lines.get(index).split(":", 2);
	    		if(params[0].equalsIgnoreCase(name))
	    		{
	    			//if length is not exactly the amount expected, then consider it corrupted allowing a write over.
	    			if(hasPass && params.length == 4)
	    			{
	    				if(!pass.equals(params[2]))
	    				{
	    					logger.warning(player.getName()+" used incorrect password while setting warp "+name);
	    					return WarpError.INCORRECT_PASS;
	    				}
	    			}
	    			break;
	    		}
	    	}
	    	
	    	StringBuilder outsb = new StringBuilder();
    		outsb.append(name).append(":").append(warpObj.toSaveString());
    		
    		if(index >= lines.size())
    			lines.add(outsb.toString());
    		else
    			lines.set(index, outsb.toString());
	    	
	    	//write file
	    	fos = new FileOutputStream(file, false);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			
			for(String ln : lines)
			{
				if(ln == null)
					break;
				
				writer.write(ln);
				writer.newLine();
			}
		}
		catch(FileNotFoundException e)
		{
			return WarpError.FILE_NOT_FOUND;
		}
		catch(IOException e)
		{
			return WarpError.IO_ERROR;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e) {}
			finally
			{
				try
				{
					if(writer != null)
						writer.close();
				}
				catch(IOException e) {}
			}
		}
		
		if(hasPass)
			passWarps.put(name, warpObj);
		else
			warps.put(name, warpObj);
		
		return null;
	}
	
	protected static WarpError removeWarp(Player player, String name, String password)
	{
		boolean hasPass = password != null && !password.isEmpty();
		if(hasPass)
		{
			if(password.length() < 3 || password.length() > 15)
				return WarpError.PASS_SIZE;
			password = encrypt(password);
		}
		
		name = name.toLowerCase();
		
		if(!warpExists(name, hasPass))
			return WarpError.WARP_NOT_FOUND;
		
		if(hasPass)
		{
			CBWarpObject warpObj = passWarps.get(name);
			
			if(warpObj == null)
				return WarpError.WARP_NOT_FOUND;
			
			if(!warpObj.PASSWORD.equals(password))
			{
				logger.warning(player.getName()+" used incorrect password while removing warp "+name);
				return WarpError.INCORRECT_PASS;
			}
			
			passWarps.remove(name);
		}
		else
		{
			warps.remove(name);
		}
		
		return editWarpFileData(getPath(hasPass), name, null);
	}
	
	protected static WarpError setMessage(Player player, String name, String message, String password)
	{
		return setWarpInfo(player, name, null, message, password);
	}
	
	protected static WarpError setTitle(Player player, String name, String title, String password)
	{
		return setWarpInfo(player, name, title, null, password);
	}
	
	private static WarpError setWarpInfo(Player player, String name, String title, String message, String password)
	{
		boolean hasPass = password != null && !password.isEmpty();
		if(hasPass)
		{
			if(password.length() < 3 || password.length() > 15)
				return WarpError.PASS_SIZE;
			password = encrypt(password);
		}
		
		name = name.toLowerCase();
		
		if(!warpExists(name, hasPass))
			return WarpError.WARP_NOT_FOUND;
		
		CBWarpObject warpObj;
		if(hasPass)
		{
			warpObj = passWarps.get(name);
			
			if(warpObj == null)
				return WarpError.WARP_NOT_FOUND;
			
			if(!warpObj.PASSWORD.equals(password))
			{
				logger.warning(player.getName()+" used incorrect password while changing data for warp "+name);
				return WarpError.INCORRECT_PASS;
			}
		}
		else
		{
			warpObj = warps.get(name);
			if(warpObj == null)
				return WarpError.WARP_NOT_FOUND;
		}
		
		final String newtitle;
		final String newmessage;
		if(title == null)
			newtitle = warpObj.TITLE;
		else
			newtitle = title;
		if(message == null)
			newmessage = warpObj.MESSAGE;
		else
			newmessage = message;
		
		CBWarpObject newobj = new CBWarpObject(warpObj.LOCATION, newtitle, newmessage, warpObj.PASSWORD);
		
		if(hasPass)
		{
			passWarps.put(name, newobj);
		}
		else
		{
			warps.put(name, newobj);
		}
		
		return editWarpFileData(getPath(hasPass), name, newobj.toSaveString());
	}
	
	private static WarpError editWarpFileData(String path, String name, String data)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return WarpError.FILE_NOT_FOUND;
		}
		
		FileInputStream fis = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		
		try
		{
	    	fis = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fis));
	    	
	    	String line = br.readLine();
	    	ArrayList<String> lines = new ArrayList<String>();
	    	while(line != null)
	    	{
	    		lines.add(line);
	    		line = br.readLine();
	    	}
	    	
	    	int index = 2;
	    	for(; index < lines.size(); index++)
	    	{
	    		String[] params = lines.get(index).split(":", 2);
	    		if(params[0].equalsIgnoreCase(name))
	    		{
	    			break;
	    		}
	    	}
	    	
	    	//write file
	    	fos = new FileOutputStream(file, false);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			
			for(int i = 0; i < lines.size(); i++)
			{
				String ln = lines.get(i);
				if(ln == null)
					break;
				
				if(i == index)
				{
					if(data == null)
						continue; //remove
					ln = name + ":" +data;
				}
				
				writer.write(ln);
				writer.newLine();
			}
		}
		catch(FileNotFoundException e)
		{
			return WarpError.FILE_NOT_FOUND;
		}
		catch(IOException e)
		{
			return WarpError.IO_ERROR;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e) {}
			finally
			{
				try
				{
					if(writer != null)
						writer.close();
				}
				catch(IOException e) {}
			}
		}
		
		return null;
	}
	
	protected static WarpError reload()
	{
		WarpError error = reloadMap(warps, false);
		if(error != null)
			return error;
		return reloadMap(passWarps, true);
	}
	
	private static WarpError reloadMap(Map<String, CBWarpObject> map, boolean hasPassword)
	{
		String path = getPath(hasPassword);
		File file = new File(path);
		if(!file.exists())
		{
			return WarpError.FILE_NOT_FOUND;
		}
		
		FileInputStream fis = null;
		BufferedReader br = null;
		try
		{
	    	fis = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fis));
	    	
	    	//skip first 2 lines
	    	br.readLine();
	    	br.readLine();
	    	
	    	map.clear();
			
			String line = br.readLine();
	    	while(line != null)
	    	{
	    		String[] params;
	    		if(hasPassword)
	    			params = line.split(":", 5);
	    		else
	    			params = line.split(":", 4);
	    		
	    		if(params[0] == null || params[0].isEmpty() || params[0].length() > 15
	    			|| (hasPassword && params.length != 5) || (!hasPassword && params.length != 4))
	    		{
	    			logger.warning("CB warp group contains corrupted format. File can not be used. File: "+path);
					map.clear();
					return WarpError.BAD_FILE;
	    		}
	    		
	    		WorldLocation loc = Util.stringToWorldLocation(params[1]);
	    		
	    		if(loc == null || (hasPassword && (params[2] == null || params[2].isEmpty())))
	    		{
	    			logger.warning("CB warp group contains corrupted format. File can not be used. File: "+path);
					map.clear();
					return WarpError.BAD_FILE;
	    		}
	    		
	    		CBWarpObject warpObj;
	    		if(hasPassword)
	    			warpObj = new CBWarpObject(loc, params[3], params[4], params[2]);
	    		else
	    			warpObj = new CBWarpObject(loc, params[2], params[3]);
	    		
	    		map.put(params[0], warpObj);
	    		
	    		line = br.readLine();
	    	}
		}
		catch(FileNotFoundException e)
		{
			return WarpError.FILE_NOT_FOUND;
		}
		catch(IOException e)
		{
			return WarpError.IO_ERROR;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e) {}
		}
		
		return null;
	}
	
	protected static boolean isCorrectPassword(String name, String password)
	{
		if(name.isEmpty())
			return false;
		
		name = name.toLowerCase();
		password = encrypt(password);
		
		CBWarpObject warpObj = passWarps.get(name);
		if(warpObj == null)
			return false;
		
		if(warpObj.PASSWORD.equals(password))
			return true;
		
		return false;
	}
	
	/** Currently NOT a real encryption.
	 * 
	 * @param pass
	 * @return Returns the "encrypted" string, which is currently not a real encryption.
	 */
	private static String encrypt(String pass)
	{
		//obviously this is NOT a real encryption, but
		//leaving this here in case someone wants to
		//create a real encryption.
		//I feel a high/medium security isn't needed for
		//something like this.
		return ""+pass.hashCode();
	}
	
	private static String getPath(boolean password)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(BASE_PATH);
		if(password)
			sb.append("p_");
		sb.append("warps.txt");
		
		return sb.toString();
	}
}
