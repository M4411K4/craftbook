package com.sk89q.craftbook.music;

import java.util.ArrayList;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MusicMidiTrack
{
	public final Track TRACK;
	
	private boolean finished = false;
	private int octave = 2; //lowest octave
	
	public int instrument = 0;
	public int position = 0;
	
	public byte playMode = 0;
	
	public MusicMidiTrack(Track track)
	{
		TRACK = track;
	}
	
	public ArrayList<MusicNote> nextTick(double tick)
	{
		if(tick >= TRACK.ticks())
		{
			finished = true;
			return null;
		}
		
		ArrayList<MusicNote> notes = new ArrayList<MusicNote>();
		
		while(position < TRACK.size() - 1)
		{
			position++;
			
			MidiEvent event = TRACK.get(position);
			if(event == null)
				continue;
			
			if(TRACK.get(position).getTick() > tick)
			{
				position--;
				break;
			}
			
			if(event.getMessage() instanceof MetaMessage)
			{
				if(event.getMessage().getStatus() == 0x2F)
				{
					finished = true;
					break;
				}
				continue;
			}
			
			if(!(event.getMessage() instanceof ShortMessage))
				continue;
			
			ShortMessage message = (ShortMessage) event.getMessage();
			
			if(message.getCommand() != ShortMessage.NOTE_ON)
				continue;
			
			int note = message.getData1();
			
			switch(playMode)
			{
				case 1:
					note = getShiftedOctaveNote(note);
					break;
				case 2:
					note = getCappedOctaveNote(note);
					break;
				default:
					note = getCycledOctaveNote(note);
			}
			
			if(note < 0)
				continue;
			
			notes.add(new MusicNote(instrument, (byte)note));
		}
		
		return notes;
	}
	
	public void reset()
	{
		finished = false;
		position = 0;
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public int getOctave()
	{
		return octave;
	}
	
	public void setOctave(int octave)
	{
		this.octave = octave;
	}
	
	/*
	 * Any note lower than the lowest note become the lowest note.
	 * Any note higher than the highest note become the highest note.
	 */
	private int getCappedOctaveNote(int note)
	{
		int min = 18 + 12 * octave;
		int pitch = note - min;
		
		if(pitch < 0)
			pitch = 0;
		else if(pitch > 24)
			pitch = 24;
		
		return pitch;
	}
	
	/*
	 * Shifts octaves lower than the min octave to the min octave and
	 * octaves higher than the max octave to the max octave. Other octaves
	 * inbetween are left alone.
	 */
	private int getShiftedOctaveNote(int note)
	{
		int min = 18 + 12 * octave;
		int pitch = note - min;
		
		while(pitch < 0)
		{
			pitch += 12;
		}
		while(pitch > 24)
		{
			pitch -= 12;
		}
		
		return pitch;
	}
	
	/*
	 * Cycles through the octaves. Sets pairs of octaves as the same high-low
	 * octaves.
	 * The code is easier to understand than me attempting to explain it if
	 * you know what % does.
	 */
	private int getCycledOctaveNote(int note)
	{
		return (note - 6) % 24;
	}
}
