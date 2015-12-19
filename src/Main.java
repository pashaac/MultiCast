import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by Asadchiy Pavel
 * on 15.12.15.
 */
public class Main {

    public final static String DEFAULT_ADDRESS_1 = "225.1.2.6";
    public final static String DEFAULT_ADDRESS_2 = "225.1.2.7";
    public final static String DEFAULT_ADDRESS_3 = "225.1.2.8";

    public final static int PORT = 8888;
    public final static Level LOG_LEVEL = Level.SEVERE;

    public static void main(String[] args) throws InterruptedException {
        final MulticastServer multicastServer1 = new MulticastServer(DEFAULT_ADDRESS_1, PORT, "songs/Watsky.mp3");
        final MulticastServer multicastServer2 = new MulticastServer(DEFAULT_ADDRESS_2, PORT, "songs/Eminem.mp3");
        final MulticastServer multicastServer3 = new MulticastServer(DEFAULT_ADDRESS_3, PORT, "songs/Umbrella.mp3");

        final MulticastClient multicastClient = new MulticastClient(DEFAULT_ADDRESS_1, PORT);

        final Thread threadClient = new Thread(multicastClient);
        final Thread threadServer1 = new Thread(multicastServer1);
        final Thread threadServer2 = new Thread(multicastServer2);
        final Thread threadServer3 = new Thread(multicastServer3);
        threadServer1.start();
        threadServer2.start();
        threadServer3.start();
        threadClient.start();
    }

    public static void cloneSongFile(final String songPath) throws IOException {
        final Song song = new Song(songPath);
        final FileOutputStream fileOutputStream = new FileOutputStream(songPath + "_" + songPath.hashCode() + ".mp3");
        long size = 0;
        while (song.hasNext()) {
            byte[] buffer = song.next();
            size += buffer.length;
            fileOutputStream.write(buffer);
            System.out.println(buffer.length);
        }
        System.out.println(size / 1024 + "Kb");
        System.out.println(size * 1.0 / 1024 / 1024 + "Mb");
    }
}
