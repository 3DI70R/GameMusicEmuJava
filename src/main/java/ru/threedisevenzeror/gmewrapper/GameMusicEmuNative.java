package ru.threedisevenzeror.gmewrapper;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ThreeDISevenZeroR on 25.06.2016.
 *
 * Game Music Emu Native JNA objects
 */
interface GameMusicEmuNative extends Library {

    class Equalizer extends Structure {

        public double treble;
        public double bass;
        public double d2;
        public double d3;
        public double d4;
        public double d5;
        public double d6;
        public double d7;
        public double d8;
        public double d9;

        public Equalizer() {

        }

        protected List<? > getFieldOrder() {
            return Arrays.asList("treble", "bass", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9");
        }
    }

    class TrackInfo extends Structure implements GameMusicEmu.TrackInfo {

        public int length;
        public int intro_length;
        public int loop_length;
        public int play_length;

        public String system;
        public String game;
        public String song;
        public String author;
        public String copyright;
        public String comment;
        public String dumper;

        public int i4;
        public int i5;
        public int i6;
        public int i7;
        public int i8;
        public int i9;
        public int i10;
        public int i11;
        public int i12;
        public int i13;
        public int i14;
        public int i15;
        public String s7;
        public String s8;
        public String s9;
        public String s10;
        public String s11;
        public String s12;
        public String s13;
        public String s14;
        public String s15;

        public TrackInfo(Pointer ptr) {
            super(ptr);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            GameMusicEmuNative.instance.gme_free_info(getPointer());
        }

        protected List getFieldOrder() {
            return Arrays.asList(
                    "length",  "intro_length", "loop_length",  "play_length", "i4", "i5", "i6", "i7", "i8", "i9", "i10", "i11",
                    "i12", "i13", "i14", "i15", "system", "game", "song", "author", "copyright", "comment", "dumper", "s7",
                    "s8", "s9", "s10", "s11", "s12", "s13", "s14", "s15");
        }

        public int getLength() {
            return length;
        }

        public int getIntroLength() {
            return intro_length;
        }

        public int getLoopLength() {
            return loop_length;
        }

        public int getPlayLength() {
            return play_length;
        }

        public String getSystem() {
            return system;
        }

        public String getGame() {
            return game;
        }

        public String getSong() {
            return song;
        }

        public String getAuthor() {
            return author;
        }

        public String getCopyright() {
            return copyright;
        }

        public String getComment() {
            return comment;
        }

        public String getDumper() {
            return dumper;
        }
    }

    GameMusicEmuNative instance = (GameMusicEmuNative) Native.loadLibrary("libgme", GameMusicEmuNative.class);

    /**
     * Create emulator and load game music file/data into it. Sets *out to new emulator.
     */
    String gme_open_file(String path, PointerByReference out, int sample_rate);

    /**
     * Same as gme_open_file(), but uses file data already in memory. Makes copy of data.
     */
    String gme_open_data(byte[] data, long size, PointerByReference out, int sample_rate);

    /**
     * Number of tracks available
     */
    int gme_track_count(Pointer gme);

    /**
     * Start a track, where 0 is the first track
     */
    String gme_start_track(Pointer gme, int index);

    /**
     * Generate 'count' 16-bit signed samples info 'out'. Output is in stereo.
     */
    String gme_play(Pointer gme, int count, short[] out);

    /**
     * Finish using emulator and free memory
     */
    void gme_delete(Pointer gme);

    /******** Track position/length ********/

    /**
     * Set time to start fading track out. Once fade ends track_ended() returns true.
     * Fade time can be changed while track is playing.
     */
    void gme_set_fade(Pointer gme, int start_msec);

    /**
     * True if a track has reached its end
     */
    boolean gme_track_ended(Pointer gme);

    /**
     * Number of milliseconds (1000 = one second) played since beginning of track
     */
    int gme_tell(Pointer gme);

    /**
     * Seek to new time in track. Seeking backwards or far forward can take a while.
     */
    String gme_seek(Pointer gme, int msec);

    /******** Informational ********/

    /**
     * Most recent warning string, or NULL if none. Clears current warning after returning. Warning is also cleared when loading a file and starting a track.
     */
    String gme_warning(Pointer gme);

    /**
     * Load m3u playlist file (must be done after loading music)
     */
    String gme_load_m3u(Pointer gme, String path);

    /**
     * Load m3u playlist file from memory (must be done after loading music)
     */
    String gme_load_m3u_data(Pointer gme, byte[] data, long size);

    /**
     * Clear any loaded m3u playlist and any internal playlist that the music format supports (NSFE for example).
     */
    void gme_clear_playlist(Pointer gme);

    /**
     * Gets information for a particular track (length, name, author, etc.).
     * Must be freed after use.
     */
    String gme_track_info(Pointer gme, PointerByReference out, int track);

    /**
     * Frees track information
     */
    void gme_free_info(Pointer gmeInfo);

    /******** Advanced playback ********/

    /**
     * Adjust stereo echo depth, where 0.0 = off and 1.0 = maximum. Has no effect for GYM, SPC, and Sega Genesis VGM music
     */
    void gme_set_stereo_depth(Pointer gme, double depth);

    /**
     * Disable automatic end-of-track detection and skipping of silence at beginning
     * if ignore is true
     */
    void gme_ignore_silence(Pointer gme, boolean ignore);

    /**
     * Adjust song tempo, where 1.0 = normal, 0.5 = half speed, 2.0 = double speed.
     * Track length as returned by track_info() assumes a tempo of 1.0.
     */
    void gme_set_tempo(Pointer gme, double tempo);

    /**
     * Number of voices used by currently loaded file
     */
    int gme_voice_count(Pointer gme);

    /**
     * Name of voice i, from 0 to gme_voice_count() - 1
     */
    String gme_voice_name(Pointer gme, int i);

    /**
     * Mute/unmute voice i, where voice 0 is first voice
     */
    void gme_mute_voice(Pointer gme, int index, boolean mute);

    /**
     * Set muting state of all voices at once using a bit mask, where -1 mutes all
     * voices, 0 unmutes them all, 0x01 mutes just the first voice, etc.
     */
    void gme_mute_voices(Pointer gme, int muting_mask);

    /**
     * Get current frequency equalizater parameters
     */
    void gme_equalizer(Pointer gme, Equalizer out);

    /**
     * Change frequency equalizer parameters
     */
    void gme_set_equalizer(Pointer gme, Pointer equalizer);

    /**
     * Enables/disables most accurate sound emulation options
     */
    void gme_enable_accuracy(Pointer gme, boolean enabled);

    /**
     * Type of this emulator
     */
    Pointer gme_type(Pointer gme);

    /**
     * Name of game system for this music file type
     */
    String gme_type_system(Pointer gmeType);

    /**
     * True if this music file type supports multiple tracks
     */
    boolean gme_type_multitrack(Pointer gmeType);

    /**
     * Determine likely game music type based on first four bytes of file. Returns string containing proper file suffix (i.e. "NSF", "SPC", etc.) or "" if file header is not recognized.
     */
    String gme_identify_header(byte[] header);

    /**
     * Get corresponding music type for file path or extension passed in.
     */
    Pointer gme_identify_extension(String path);

    /** Determine file type based on file's extension or header (if extension isn't recognized). Sets *type_out to type, or 0 if unrecognized or error. */
    String gme_identify_file(String path, PointerByReference gme );

    /**
     * Create new emulator and set sample rate. Returns NULL if out of memory. If you only need
     * track information, pass gme_info_only for sample_rate.
     */
    Pointer gme_new_emu(Pointer typePtr, int sample_rate);

    /**
     * Load music file into emulator
     */
    String gme_load_file(Pointer gme, String path);

    /**
     * Load music file from memory into emulator. Makes a copy of data passed.
     */
    String gme_load_data(Pointer gme, byte[] data, long size);
}
