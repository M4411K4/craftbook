import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import com.sk89q.craftbook.music.MusicMidiTrack;
import com.sk89q.craftbook.music.MusicNote;
import com.sk89q.craftbook.music.MusicNoteKey;
import com.sk89q.craftbook.music.parser.DefaultMusicParser;



public class MusicPlayer
{
	private final String SONG;
	private final boolean LOOP;
	private final int MAX_BEAT_DURATION;
	private final int MAX_TEXT_LINES;
	private final int MAX_MIDI_TRACKS;
	private final int MAX_RATE = 10;
	
	private final int x;
	private final int y;
	private final int z;
	
	private String title;
	private String author;
	private int rate;
	private int midiRate;
	private int midiResolution;
	
	private int currentNote = 0;
	private int currentTick = 0;
	private int currentTrackPos = 0;
	
	public boolean pause = false;
	
	private boolean midiMode = false;
	
	private ArrayList<MusicNoteKey> musicKeys;
	private Sequence sequence;
	private MusicMidiTrack[] midiTracks;
	
	public MusicPlayer(String song, int x, int y, int z, PropertiesFile properties)
	{
		this(song, x, y, z, properties, -1, false);
	}
	
	public MusicPlayer(String song, int x, int y, int z, PropertiesFile properties, int rate, boolean loop)
	{
		this.SONG = song;
		this.rate = rate;
		this.LOOP = loop;
		
		currentTick = rate;
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		int duration = 500;
		if(properties.containsKey("music-max-beat-duration"))
		{
			duration = properties.getInt("music-max-beat-duration", duration);
			if(duration < 0)
				duration = 0;
		}
		MAX_BEAT_DURATION = duration;
		
		int linemax = 100;
		if(properties.containsKey("music-max-text-lines"))
		{
			linemax = properties.getInt("music-max-text-lines", linemax);
			if(linemax < 0)
				linemax = 0;
		}
		MAX_TEXT_LINES = linemax;
		
		int trackmax = 10;
		if(properties.containsKey("music-max-midi-tracks"))
		{
			trackmax = properties.getInt("music-max-midi-tracks", trackmax);
			if(trackmax < 0)
				trackmax = 0;
		}
		MAX_MIDI_TRACKS = trackmax;
	}
	
	public void loadSong()
	{
		currentNote = 0;
		currentTrackPos = 0;
		
		File file;
		
		String[] name = SONG.split("\\.", 2);
		
		if(name[0].length() == 0 || !CopyManager.isValidName(name[0]))
			return;
		
		if(name.length > 1 && name[1].equalsIgnoreCase("m"))
		{
			file = new File("cbmusic" + File.separator +
							name[0] + ".mid");
			
			loadMidiSong(file);
		}
		else
		{
			file = new File("cbmusic" + File.separator +
							SONG + ".txt");
			
			loadTextSong(file);
		}
	}
	
	private void loadMidiSong(File file)
	{
		if (!file.exists())
			return;
		
		midiMode = true;
		
		midiRate = rate;
		rate = 0;
		
		if(midiRate < 1)
			midiRate = 4;
		
		if(midiRate > MAX_RATE)
			midiRate = MAX_RATE;
		
		sequence = null;
		try
		{
			sequence = MidiSystem.getSequence(file);
		}
		catch(InvalidMidiDataException e)
		{
			
		}
		catch(IOException e)
		{
			
		}
		
		Track[] tracks = sequence.getTracks();
		
		if(sequence == null || tracks == null)
			return;
		
		midiResolution = sequence.getResolution() / 24;
		
		int trackSize = tracks.length - 1; //we skip the first track
		if(trackSize > MAX_MIDI_TRACKS)
			trackSize = MAX_MIDI_TRACKS;
		
		midiTracks = new MusicMidiTrack[trackSize];
		
		boolean allNull = true;
		for(int i = 0; i < trackSize; i++)
		{
			Track track = tracks[i+1];
			
			if(track.size() < 4)
    			continue;
			
			midiTracks[i] = new MusicMidiTrack(track);
			allNull = false;
		}
		
		if(allNull)
		{
			sequence = null;
			pause = true;
		}
	}
	
	private void loadTextSong(File file)
	{
		if (!file.exists())
			return;
		
		midiMode = false;
		
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
	    	
	    	if(rate > MAX_RATE)
	    		rate = MAX_RATE;
	    	
	    	if(fileFormat.equalsIgnoreCase("default"))
	    	{
	    		musicKeys = DefaultMusicParser.parse(br, MAX_BEAT_DURATION, MAX_TEXT_LINES);
	    	}
	    	if(fileFormat.equalsIgnoreCase("default2"))
	    	{
	    		musicKeys = DefaultMusicParser.parse2(br, MAX_BEAT_DURATION, MAX_TEXT_LINES);
	    	}
	    	else
	    	{
	    		//not a recognized music file type
	    		musicKeys = null;
				pause = true;
	    	}
		}
		catch(FileNotFoundException e)
		{
			musicKeys = null;
			pause = true;
		}
		catch(IOException e)
		{
			musicKeys = null;
			pause = true;
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
	}
	
	public void tick()
	{
		if(pause || (!midiMode && musicKeys == null) || (midiMode && sequence == null) )
			return;
		
		if(currentTick >= rate)
		{
			if(midiMode)
				playNextMidiNote();
			else
				playNextNote();
			
			currentTick = 0;
		}
		else
		{
			currentTick++;
		}
	}
	
	/*
	 * Plays next note on the list.
	 * Assumes noteblock is still at current location.
	 */
	public void playNextNote()
	{
		if(currentTrackPos >= musicKeys.size())
		{
			if(!LOOP)
			{
				pause = true;
				return;
			}
			
			currentNote = 0;
			currentTrackPos = 0;
		}
		
		if(currentNote == musicKeys.get(currentTrackPos).getKey())
		{
			ArrayList<MusicNote> notes = musicKeys.get(currentTrackPos).getNotes();
			
			playNotes(notes);
			
			currentTrackPos++;
		}
		
		
		currentNote++;
	}
	
	public void playNextMidiNote()
	{
		boolean allFinished = true;
		
		double tick = currentNote * midiResolution;// * 0.987291095374788;
		
		for(int i = 0; i < midiTracks.length; i++)
		{
			MusicMidiTrack track = midiTracks[i];
			if(track == null || track.isFinished())
				continue;
			
			allFinished = false;
			
			ArrayList<MusicNote> notes = track.nextTick(tick);
			
			if(notes == null)
				continue;
			
			playNotes(notes);
		}
		
		currentNote += midiRate;
		
		if(allFinished)
		{
			if(!LOOP)
			{
				pause = true;
				return;
			}
			
			currentNote = 0;
			
			for(int i = 0; i < midiTracks.length; i++)
			{
				midiTracks[i].reset();
			}
		}
	}
	
	private void playNotes(ArrayList<MusicNote> notes)
	{
		for(MusicNote note : notes)
		{
			etc.getMCServer().f.a(x, y, z, 64.0D, new OPacket54PlayNoteBlock(x, y, z, note.type, note.pitch));
		}
	}
	
	public boolean isLoaded()
	{
		return musicKeys != null;
	}
	
	public boolean isPlaying()
	{
		return isLoaded() && !pause;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getAuthor()
	{
		return author;
	}
}
