package loghub.processors;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loghub.Event;
import loghub.Helpers;
import loghub.configuration.Properties;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

public class Grok extends FieldsProcessor {

    private static final Logger logger = LogManager.getLogger();

    private final oi.thekraken.grok.api.Grok grok;
    private String pattern;

    public Grok() {
        grok = new oi.thekraken.grok.api.Grok();
    }

    @Override
    public void processMessage(Event event, String field, String destination) {
        String line = (String) event.get(field);
        Match gm = grok.match(line);
        gm.captures();
        if(! gm.isNull()) {
            //Bug in grok, rebuild the matching manually
            Map<String, String> groups = gm.getMatch().namedGroups();
            Map<String, String> names = grok.getNamedRegexCollection();
            for(Map.Entry<String, String> found: groups.entrySet()) {
                if(found.getValue() == null) {
                    continue;
                }
                String finalname = names.get(found.getKey());
                //Dirty hack to filter non capturing group
                if(finalname.equals(finalname.toUpperCase())) {
                    continue;
                }
                event.put(finalname, found.getValue());
            }
        }
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String getName() {
        return "grok";
    }

    @Override
    public boolean configure(Properties properties) {
        Helpers.ThrowingConsumer<InputStream> grokloader = is -> grok.addPatternFromReader(new InputStreamReader(new BufferedInputStream(is)));
        try {
            Helpers.readRessources(properties.classloader, "patterns", grokloader);
            grok.compile(pattern);
        } catch (IOException | URISyntaxException e) {
            logger.error("unable to load patterns: {}", e.getMessage());
            logger.catching(Level.DEBUG, e);
            return false;
        } catch (GrokException e) {
            logger.error("wrong pattern {}: {}", pattern, e.getMessage());
            logger.catching(Level.DEBUG, e);
            return false;
        }
        return super.configure(properties);
    }

}
