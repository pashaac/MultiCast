import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class MulticastServer implements Runnable {
    public static final Logger LOG = Logger.getLogger(MulticastServer.class.getName());

    private final String songPath;
    private final AtomicReference<String> address;
    private volatile int port;

    public MulticastServer(String address, int port, final String path) {
        this.address = new AtomicReference<>(address);
        this.port = port;
        this.songPath = path;
        LOG.setLevel(Main.LOG_LEVEL);
    }

    @Override
    public void run() {
        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address.get());
        } catch (UnknownHostException e) {
            LOG.severe("Can't get InetAddress from address = " + address.get());
            return;
        }
        final Song song = new Song(songPath);
        try (MulticastSocket multicastSocket = new MulticastSocket()) {
            while (song.hasNext() && !Thread.interrupted()) {
                byte[] next = song.next();
                DatagramPacket packet = new DatagramPacket(next, next.length, inetAddress, port);
                multicastSocket.send(packet);
                LOG.info("Server: send package #" + song.getPackageCounter() + ", size = " + next.length);
                multicastSocket.send(packet);
                LOG.info("Server: send package #" + song.getPackageCounter() + ", size = " + next.length);
                try {
                    Thread.sleep(Song.SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOG.severe("Error when thread sleep, " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.severe("Can't create MulticastSocket, " + e.getMessage());
        }
    }
}