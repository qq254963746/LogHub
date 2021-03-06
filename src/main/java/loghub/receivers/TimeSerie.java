

package loghub.receivers;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import loghub.NamedArrayBlockingQueue;
import loghub.Receiver;

public class TimeSerie extends Receiver {

    private final static AtomicLong r = new AtomicLong(0);

    private int frequency = 1000;

    public TimeSerie(NamedArrayBlockingQueue outQueue) {
        super(outQueue);
    }

    @Override
    protected Iterator<byte[]> getIterator() {
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        return new Iterator<byte[]>() {
            @Override
            public boolean hasNext() {
                try {
                    Thread.sleep((int)(1.0/frequency * 1000));
                    return true;
                } catch (InterruptedException ex) {
                    return false;
                }
            }

            @Override
            public byte[] next() {
                try {
                    buffer.clear();
                    buffer.put(Long.toString(r.getAndIncrement()).getBytes());
                } catch (Exception e) {
                    throw new RuntimeException("can't push to buffer", e);
                }
                return buffer.array();
            }

        };
    }

    @Override
    public String getReceiverName() {
        return "TimeSerie";
    }

    /**
     * @return the interval
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the interval to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

}