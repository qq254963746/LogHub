package loghub.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import loghub.Event;
import loghub.configuration.Beans;

/**
 * This transformer parse a field using a regex that extract name and value.
 * The regex must contains two named group name and value. The field is parsed until exhaustion. The unmatched content will stay in the field
 * unless everything match, in this case, the field is removed.
 * <p>
 * The default parser is "(?&lt;name&gt;\p{Alnum}+)\p{Space}?[=:]\p{Space}?(?&lt;value&gt;[^;,:]+)[;,:]?" and should match most common case
 * @author Fabrice Bacchella
 *
 */
@Beans({"parser"})
public class VarExtractor extends FieldsProcessor {

    private Pattern parser = Pattern.compile("(?<name>\\p{Alnum}+)\\p{Space}?[=:]\\p{Space}?(?<value>[^;,:]+)[;,:]?");

    @Override
    public void processMessage(Event event, String field, String destination ) {
        String message = event.get(field).toString();
        String after = message;
        Matcher m = parser.matcher(message);
        while(m.find()) {
            String key = m.group("name");
            String value = m.group("value");
            if(key != null && ! key.isEmpty()) {
                if(value != null && ! value.isEmpty()) {
                    event.put(key, value);
                }
            }
            after = message.substring(m.end());
        }
        if(after != null && ! after.isEmpty()) {
            event.put(destination, after);
        } else {
            event.remove(field);
        }
    }

    @Override
    public String getName() {
        return "VarExtractor";
    }

    /**
     * @return the parser
     */
    public String getParser() {
        return parser.pattern();
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(String parser) {
        this.parser = Pattern.compile(parser);
    }

}
