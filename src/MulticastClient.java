import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Logger;

public class MulticastClient implements Runnable {
    public static final Logger LOG = Logger.getLogger(MulticastClient.class.getName());

    private final String address;
    private final int port;
    private InetAddress myInetAddress;

    public MulticastClient(String address, int port) {
        this.address = address;
        this.port = port;
        LOG.setLevel(Main.LOG_LEVEL);
    }

    @Override
    public void run() {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
            myInetAddress = inetAddress;
        } catch (UnknownHostException e) {
            LOG.severe("Client: can't get InetAddress from address = " + address);
            return;
        }

        final byte[] buffer = new byte[Song.PACKAGE_SIZE];
        try (MulticastSocket clientSocket = new MulticastSocket(port)) {
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream, Song.PACKAGE_SIZE * 20);
            final MusicPlayer musicPlayer = new MusicPlayer(pipedInputStream);
            final Thread musicThread = new Thread(musicPlayer);
            musicThread.start();
            final Terminal terminal = new Terminal();
            final Thread terminalThread = new Thread(terminal);
            terminalThread.start();
            int prevPackageNumber = 0;
            while (!Thread.currentThread().isInterrupted()) {
                clientSocket.joinGroup(inetAddress);
                final DatagramPacket msgPacket = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(msgPacket);
                final int packageServerNumber = Song.getIntFromBytes(Arrays.copyOf(buffer, Song.OFFSET));
                LOG.info("Client: get server package #" + packageServerNumber);
                if (prevPackageNumber < packageServerNumber) {
                    LOG.info("Client: client package #" + packageServerNumber + " with bytes[" + msgPacket.getLength() + "]");
                    pipedOutputStream.write(buffer, Song.OFFSET, msgPacket.getLength() - Song.OFFSET);
                    prevPackageNumber = packageServerNumber;
                } else {
                    LOG.info("Client: skip package from server #" + packageServerNumber + " because client package #" + prevPackageNumber);
                }
                clientSocket.leaveGroup(inetAddress);
                if (terminal.isWasQuite()) {
                    try {
                        musicPlayer.close();
                        pipedInputStream.close();
                        pipedOutputStream.close();
                        musicThread.join();
                        terminalThread.join();
                    } catch (Exception e) {
                        LOG.severe("Can't close player");
                    }
                    break;
                }
                final String address = terminal.getAddress();
                if (address != null) {
                    try {
                        if (address.startsWith(Main.DEFAULT_ADDRESS_1)) {
                            inetAddress = myInetAddress;
                        } else {
                            inetAddress = InetAddress.getByName(address);
                        }
                        terminal.setAddress(null);
                    } catch (UnknownHostException e) {
                        LOG.severe("Client: can't get InetAddress from address = " + address);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            LOG.severe("Client: can't create MulticastSocket, " + e.getMessage());
        }
    }

}
