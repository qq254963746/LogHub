/**
 * 
 */
package loghub.processors;

import org.junit.Assert;
import org.junit.Test;

import loghub.Event;
import loghub.ProcessorException;

/**
 * @author fa4
 *
 */
public class TestFieldsProcessor {

    @Test
    public void test() throws ProcessorException {
        FieldsProcessor p = new FieldsProcessor() {

            @Override
            public void processMessage(Event event, String field,
                    String destination) throws ProcessorException {
                event.put(destination, event.get(field));
            }

            @Override
            public String getName() {
                return null;
            }

        };

        p.setDestination("${field}_done");
        p.setFields(new String[] {"a", "b"});
        Event e = new Event();
        e.put("a", 1);
        e.put("b", 2);
        p.process(e);
        Assert.assertEquals("destination field wrong", 1, e.get("a_done"));
        Assert.assertEquals("destination field wrong", 2, e.get("b_done"));
    }

}
