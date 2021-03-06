package loghub.senders;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loghub.Event;
import loghub.NamedArrayBlockingQueue;
import loghub.Sender;
import loghub.configuration.Beans;

@Beans("destination")
public class Stdout extends Sender {

    private static final Logger logger = LogManager.getLogger();

    PrintStream destination = System.out;
    
    public Stdout(NamedArrayBlockingQueue inQueue) {
        super(inQueue);
    }

    @Override
    public boolean send(Event event) {
        try {
            byte[] msg = getEncoder().encode(event);
            System.out.write(msg);
            System.out.println();
            System.out.flush();
            return true;
        } catch (IOException e) {
            logger.error("failed to output {}: {}", event, e.getMessage());
            logger.throwing(Level.DEBUG, e);
            return false;
        }
        
    }

    @Override
    public String getSenderName() {
        return "stdout";
    }

    public String getDestination() {
        return destination == System.out ? "stdout": "stderr";
    }

    public void setDestination(String destination) {
        switch(destination){
        case "stdout": this.destination = System.out; break;
        case "stderr": this.destination = System.out; break;
        }
    }

}
