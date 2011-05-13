package com.sk89q.craftbook.music.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.sk89q.craftbook.music.IMusicPlayer;
import com.sk89q.craftbook.music.MusicNote;
import com.sk89q.craftbook.music.MusicNoteKey;
import com.sk89q.craftbook.music.parser.DefaultMusicParser;

public class TextSongMedia extends ExternalMedia
{
	private ArrayList<MusicNoteKey> musicKeys;
	
	private int currentTrackPos = 0;
	
	public TextSongMedia(IMusicPlayer musicPlayer, String song)
	{
		super(musicPlayer, song);
	}
	
	public boolean loadSong()
	{
		finished = true;
		currentNote = 0;
		currentTrackPos = 0;
		
		File file = new File(MEDIA_DIR + SONG + ".txt");
		
		if (!file.exists())
			return false;
		
		FileInputStream fs = null;
		BufferedReader br = null;
		
		try
		{
	    	fs = new FileInputStream(file);
	    	br = new BufferedReader(new InputStreamReader(fs));
	    	
	    	//type of music file
	    	String fileFormat = br.readLine();
	    	
	    	//song title
	    	title = br.readLine();
	    	
	    	//song composer
	    	author = br.readLine();
	    	
	    	if(rate < 1)
	    	{
	    		try
	    		{
	    			rate = Integer.parseInt(br.readLine());
	    		}
	    		catch(NumberFormatException e)
	    		{
	    			rate = -1;
	    		}
	    		
	    		if(rate < 1)
	    		{
	    			rate = 5;
	    		}
	    	}
	    	else
	    	{
	    		//skip line 4
	    		br.readLine();
	    	}
	    	
	    	if(rate > MUSIC_PLAYER.getMaxRate())
	    		rate = MUSIC_PLAYER.getMaxRate();
	    	
	    	if(fileFormat.equalsIgnoreCase("default"))
	    	{
	    		musicKeys = DefaultMusicParser.parse(br, MUSIC_PLAYER.getMaxBeatDuration(), MUSIC_PLAYER.getMaxTextLines());
	    	}
	    	else if(fileFormat.equalsIgnoreCase("default2"))
	    	{
	    		musicKeys = DefaultMusicParser.parse2(br, MUSIC_PLAYER.getMaxBeatDuration(), MUSIC_PLAYER.getMaxTextLines());
	    	}
	    	else if(fileFormat.equalsIgnoreCase("tune1"))
	    	{
	    		musicKeys = DefaultMusicParser.parseTune1(br.readLine());
	    	}
	    	else
	    	{
	    		//not a recognized music file type
	    		musicKeys = null;
				return false;
	    	}
		}
		catch(FileNotFoundException e)
		{
			musicKeys = null;
			return false;
		}
		catch(IOException e)
		{
			musicKeys = null;
			return false;
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e)
			{
				
			}
		}
		
		finished = false;
		return true;
	}
	
	public boolean playNextNote()
	{
		if(currentTrackPos >= musicKeys.size())
		{
			finished = true;
			return false;
		}
		
		if(currentNote == musicKeys.get(currentTrackPos).getKey())
		{
			ArrayList<MusicNote> notes = musicKeys.get(currentTrackPos).getNotes();
			
			MUSIC_PLAYER.playNotes(notes);
			
			currentTrackPos++;
		}
		
		
		currentNote++;
		return true;
	}
	
	public void reset()
	{
		currentNote = 0;
		currentTrackPos = 0;
		finished = musicKeys == null;
	}
}
