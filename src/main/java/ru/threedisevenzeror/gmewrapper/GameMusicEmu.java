package ru.threedisevenzeror.gmewrapper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by ThreeDISevenZeroR on 24.06.2016.
 *
 * Game Music Emu Java Wrapper
 */
public class GameMusicEmu {

    private static final GameMusicEmuNative lib = GameMusicEmuNative.instance;

    public interface TrackInfo {

        /**
         * times in milliseconds; -1 if unknown<br>
         * total length, if file specifies it
         */
        int getLength();

        /**
         * length of song up to looping section
         */
        int getIntroLength();

        /**
         * length of looping section
         */
        int getLoopLength();

        /**
         * Length if available, otherwise intro_length+loop_length*2 if available,<br>
         * otherwise a default of 150000 (2.5 minutes).
         */
        int getPlayLength();

        String getSystem();
        String getGame();
        String getSong();
        String getAuthor();
        String getCopyright();
        String getComment();
        String getDumper();
    }

    private Pointer gme;
    private GameMusicEmuNative.Equalizer equalizer;

    protected int sampleRate;
    protected double stereoDepth;
    protected double tempo;
    protected boolean ignoreSilence;
    protected boolean accuracy;
    protected int fadeStartMsec;
    protected short[] shortBuffer;

    public GameMusicEmu(int sampleRate) {
        this.sampleRate = sampleRate;
        stereoDepth = 1;
        tempo = 1;
        ignoreSilence = true;
        accuracy = true;
        fadeStartMsec = Integer.MAX_VALUE;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        free();
    }

    public void loadFile(String filePath) {
        free();

        PointerByReference ref = new PointerByReference();
        checkError(lib.gme_open_file(filePath, ref, sampleRate));
        gme = ref.getValue();

        applySettings();
    }

    public void loadFile(InputStream stream) throws IOException {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        byte[] buffer = new byte[65535];
        int readed;

        while ((readed = stream.read(buffer)) != -1) {
            file.write(buffer, 0, readed);
        }

        loadFile(file.toByteArray());
    }

    public void loadFile(byte[] bytes) {
        free();

        PointerByReference ref = new PointerByReference();
        checkError(lib.gme_open_data(bytes, bytes.length, ref, sampleRate));
        gme = ref.getValue();

        applySettings();
    }

    public int getVoiceCount() {
        return lib.gme_voice_count(gme);
    }

    public String[] getVoiceNames() {
        String[] names = new String[getVoiceCount()];
        for (int i = 0; i < names.length; i++) {
            names[i] = lib.gme_voice_name(gme, i);
        }
        return names;
    }

    public String getSystemName() {
        return lib.gme_type_system(lib.gme_type(gme));
    }

    public int getTrackCount() {
        return lib.gme_track_count(gme);
    }

    public double getStereoDepth() {
        return stereoDepth;
    }

    public void setStereoDepth(double value) {
        stereoDepth = value;
        lib.gme_set_stereo_depth(gme, value);
    }

    public void startTrack(int track) {
        checkError(lib.gme_start_track(gme, track));
    }

    public TrackInfo getTrackInfo(int track) {
        PointerByReference ref = new PointerByReference();
        checkError(lib.gme_track_info(gme, ref, track));
        GameMusicEmuNative.TrackInfo info = new GameMusicEmuNative.TrackInfo(ref.getValue());
        info.read();
        lib.gme_free_info(ref.getValue());
        return info;
    }

    public void seek(long value, TimeUnit unit) {
        seek((int) unit.toMillis(value));
    }

    public void seek(int msecTime) {
        checkError(lib.gme_seek(gme, msecTime));
    }

    public int getCurrentTime() {
        return lib.gme_tell(gme);
    }

    public int read(byte[] b, int off, int len) {

        if(isTrackEnded()) {
            return -1;
        }

        int size = len / 2;

        if(shortBuffer == null || shortBuffer.length < size) {
            shortBuffer = new short[size];
        }

        checkError(lib.gme_play(gme, size, shortBuffer));

        for(int i = 0; i < size; i++) {
            int index = (i + off) * 2;
            b[index] = (byte) shortBuffer[i];
            b[index + 1] = (byte) (shortBuffer[i] >> 8);
        }

        return len;
    }

    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    public boolean isTrackEnded() {
        return lib.gme_track_ended(gme);
    }

    public boolean isSilenceIgnored() {
        return ignoreSilence;
    }

    public void setIgnoreSilence(boolean ignore) {
        this.ignoreSilence = ignore;
        lib.gme_ignore_silence(gme, ignore);
    }

    public boolean isAccuracyEnabled() {
        return accuracy;
    }

    public void setAccuracyEnabled(boolean enabled) {
        this.accuracy = enabled;
        lib.gme_enable_accuracy(gme, enabled);
    }

    public int getFadeStartMsec() {
        return fadeStartMsec;
    }

    public void setFadeStart(int startMsec) {
        fadeStartMsec = startMsec;
        lib.gme_set_fade(gme, startMsec);
    }

    public double getTreble() {
        return equalizer.treble;
    }

    public void setTreble(double treble) {
        equalizer.treble = treble;
        updateEqualizer();
    }

    public double getBass() {
        return equalizer.bass;
    }

    public void setBass(double bass) {
        equalizer.bass = bass;
        updateEqualizer();
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        lib.gme_set_tempo(gme, tempo);
    }

    public void muteVoice(int voice, boolean isMuted) {
        lib.gme_mute_voice(gme, voice, isMuted);
    }

    public void muteVoices(int muteMask) {
        lib.gme_mute_voices(gme, muteMask);
    }

    public void loadM3U(String path) {
        checkError(lib.gme_load_m3u(gme, path));
    }

    public void loadM3U(byte[] data) {
        checkError(lib.gme_load_m3u_data(gme, data, data.length));
    }

    public void clearPlaylist() {
        lib.gme_clear_playlist(gme);
    }

    public String getLastWarning() {
        return lib.gme_warning(gme);
    }

    protected void updateEqualizer() {
        equalizer.write();
        lib.gme_set_equalizer(gme, equalizer.getPointer());
    }

    protected void loadEqualizer() {
        lib.gme_equalizer(gme, equalizer);
    }

    protected void applySettings() {

        if(equalizer == null) {
            equalizer = new GameMusicEmuNative.Equalizer();
            loadEqualizer();
        } else {
            updateEqualizer();
        }

        setStereoDepth(stereoDepth);
        setIgnoreSilence(ignoreSilence);
        setAccuracyEnabled(accuracy);
        setFadeStart(fadeStartMsec);
        updateEqualizer();
        setTempo(tempo);
    }

    protected void free() {
        if(gme != null) {
            lib.gme_delete(gme);
        }
    }

    protected void checkError(String message) {
        if (message != null) {
            throw new Error(message);
        }
    }
}
