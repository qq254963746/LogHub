package loghub.senders;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loghub.Event;
import loghub.NamedArrayBlockingQueue;
import loghub.Sender;
import loghub.configuration.Beans;
import loghub.configuration.Properties;

@Beans({"port", "destination"})
public class Udp extends Sender {

    private static final Logger logger = LogManager.getLogger();

    private int port = 0;
    private String destination = "127.0.0.1";
    DatagramSocket socket;
    InetAddress IPAddress;

    public Udp(NamedArrayBlockingQueue inQueue) {
        super(inQueue);
    }

    @Override
    public boolean send(Event event) {
        byte[] msg = getEncoder().encode(event);

        DatagramPacket packet = new DatagramPacket(msg, msg.length, IPAddress, port);
        try {
            socket.send(packet);
            return true;
        } catch (IOException e) {
            logger.error("unable to send message: {}", e);
            logger.throwing(Level.DEBUG, e);
            return false;
        }
    }

    @Override
    public String getSenderName() {
        return null;
    }

    @Override
    public boolean configure(Properties properties) {
        try {
            IPAddress = InetAddress.getByName(destination);
            socket = new DatagramSocket();
            return super.configure(properties);
        } catch (UnknownHostException e) {
            logger.error("Can't resolve destination address '{}': {}", destination, e.getMessage());
        } catch (SocketException e) {
            logger.error("Can't start socket: {}", e.getMessage());
        }
        return false;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
