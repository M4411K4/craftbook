package com.sk89q.craftbook.music.media;

import com.sk89q.craftbook.music.IMusicPlayer;

public abstract class Media
{
	protected final IMusicPlayer MUSIC_PLAYER;
	
	protected int rate;
	
	protected int currentNote = 0;
	protected boolean finished = true;
	
	public Media(IMusicPlayer musicPlayer)
	{
		MUSIC_PLAYER = musicPlayer;
	}
	
	public boolean playNextNote()
	{
		return false;
	}
	
	public void reset()
	{
		
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public int getRate()
	{
		return rate;
	}
	
	public void setRate(int rate)
	{
		this.rate = rate;
	}
}
