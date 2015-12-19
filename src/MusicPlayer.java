import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.logging.Logger;

/**
 * Created by Asadchiy Pavel
 * on 16.12.15.
 */
public class MusicPlayer implements Runnable, AutoCloseable {
    public static final Logger LOG = Logger.getLogger(MusicPlayer.class.getName());

    private final InputStream inputStream;
    private Player player;

    public MusicPlayer(PipedInputStream pipedInputStream) {
        this.inputStream = pipedInputStream;
    }

    @Override
    public void run() {
        try {
            player = new Player(inputStream);
            player.play();
        } catch (JavaLayerException e) {
            LOG.severe("Can't play Player or create it, " + e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void close() throws Exception {
        player.close();
    }
}
