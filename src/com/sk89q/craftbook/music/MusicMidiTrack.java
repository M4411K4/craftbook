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
	private int octave = 24; //octave 2
	
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
			
			note = (note - 6) % octave;
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
	
	/*
	 * shift 1 is start octave 3
	 * shift 2 is start octave 2
	 */
	public void setOctaveShift(int shift)
	{
		octave = shift * 12;
	}
}
