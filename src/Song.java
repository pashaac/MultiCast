import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by Asadchiy Pavel
 * on 15.12.15.
 */
public class Song implements Iterator<byte[]> {
    public static final Logger LOG = Logger.getLogger(Song.class.getName());
    public static final int PACKAGE_SIZE = 1 << 16;
    public static final int SLEEP_TIME = 200;
    public static final int OFFSET = 4;

    private final String path;
    private final byte[] buffer;

    private int packageCounter;
    private InputStream stream;
    private int prevStreamread;

    public Song(String path) {
        this.path = path;
        this.buffer = new byte[OFFSET + getBytesPerSleepTime()];
        this.packageCounter = 0;
        this.stream = null;
        createStream();
    }

    private void createStream() {
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            LOG.severe("Song: can't create file bytes stream, path = " + path);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return stream == null || stream.available() != 0;
        } catch (IOException e) {
            LOG.severe("IOException occur, when hasNext check, " + e.getMessage());
            throw new IOError(e);
        }
    }

    private void fillBuffer() {
        try {
            prevStreamread = stream.read(buffer, OFFSET, buffer.length - OFFSET);
            final byte[] intBytes = getBytesFromInt(++packageCounter);
            System.arraycopy(intBytes, 0, buffer, 0, intBytes.length);
        } catch (IOException e) {
            LOG.severe("Song: can't read bytes from stream, " + e.getMessage());
        }
    }

    @Override
    public byte[] next() {
        fillBuffer();
        return Arrays.copyOf(buffer, OFFSET + prevStreamread);
    }

    public int getPackageCounter() {
        return packageCounter;
    }

    public static byte[] getBytesFromInt(int number) {
        return ByteBuffer.allocate(OFFSET).putInt(number).array();
    }

    public static int getIntFromBytes(final byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

    public long getSoundMilliseconds() {
        try {
            final Mp3File mp3File = new Mp3File(path);
            return mp3File.getLengthInMilliseconds();
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            LOG.severe("Song: can't create Mp3File, " + e.getMessage());
            return -1;
        }
    }

    public long getSoundBytesSize() {
        try {
            final Mp3File mp3File = new Mp3File(path);
            return mp3File.getLength();
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            LOG.severe("Song: can't create Mp3File, " + e.getMessage());
            return -1;
        }
    }

    public int getBytesPerSleepTime() {
        final double bytesSum = getSoundBytesSize() * SLEEP_TIME;
        return (int) Math.round(bytesSum / getSoundMilliseconds());
    }

}
