package com.sk89q.craftbook.music.media;

import java.io.File;

import com.sk89q.craftbook.music.IMusicPlayer;

public abstract class ExternalMedia extends Media
{
	protected final String MEDIA_DIR = "cbmusic" + File.separator;
	protected final String SONG;
	
	protected String title;
	protected String author;
	
	protected boolean forceMessage = false;
	
	public ExternalMedia(IMusicPlayer musicPlayer, String song)
	{
		super(musicPlayer);
		SONG = song;
	}
	
	public boolean loadSong()
	{
		return false;
	}
	
	public String getInfoMessage()
	{
		String title;
		if(getTitle().isEmpty())
			title = getSong();
		else
			title = getTitle();
		
		String message = " §6"+MUSIC_PLAYER.getNowPlaying()+"<br>§6> §f"+title;
		
		if(!getAuthor().isEmpty())
			message += " §6by §f"+getAuthor();
		
		return message;
	}
	
	public String getSong()
	{
		return SONG;
	}
	
	public String getTitle()
	{
		if(title == null)
			return "";
		return title;
	}
	
	public String getAuthor()
	{
		if(author == null)
			return "";
		return author;
	}
	
	public boolean isForcedMessage()
	{
		return forceMessage;
	}
}
