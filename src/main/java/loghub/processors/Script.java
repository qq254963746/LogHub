package loghub.processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import loghub.Event;
import loghub.Processor;
import loghub.ProcessorException;
import loghub.configuration.Beans;
import loghub.configuration.Properties;

@Beans("script")
public class Script extends Processor {

    private static final Logger logger = LogManager.getLogger();
    private static ScriptEngineManager factory = null;

    private String script;
    private Invocable inv;
    private Map<String, String> settings = Collections.emptyMap();

    @Override
    public void process(Event event) throws ProcessorException {
        try {
            inv.invokeFunction(settings.get("transform"), event);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new ProcessorException("unable to execute script " + script, e);
        }
    }

    @Override
    public String getName() {
        return "Script";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean configure(Properties properties) {
        if( factory == null) {
            factory = new ScriptEngineManager(properties.classloader);
        }
        try {
            Path scriptp = Paths.get(script);
            String mimeType = Files.probeContentType(scriptp);
            ScriptEngine engine;
            if(mimeType != null) {
                engine = factory.getEngineByMimeType(mimeType);
                logger.debug("script{} type is {}", script, mimeType);
            } else {
                int p = script.lastIndexOf(".");
                String extension = script.substring(p + 1);
                engine = factory.getEngineByExtension(extension);
            }
            if(engine == null) {
                logger.error("langage not found for script {}", script);
            }
            logger.debug("script language is {}", () -> engine);
            logger.debug("script language is {}", () -> engine.getFactory().getLanguageName());
            if( ! (engine instanceof Invocable)) {
                logger.error("engine for langage {} is not invocable", () -> engine.getFactory().getLanguageName());
                return false;
            }
            Reader r = getScriptReader();
            inv = (Invocable) engine;
            settings = (Map<String, String>) engine.eval(r);
            if(settings == null) {
                settings = (Map<String, String>) engine.get("settings");
            }
            if(settings.containsKey("configure")) {
                inv.invokeFunction(settings.get("configure"), properties );
            }
            return super.configure(properties);
        } catch (IOException e) {
            logger.error("Finding script {} failed: {}", script, e);
            return false;
        } catch (ScriptException e) {
            logger.error("execution of script {} failed: {}", script, e);
            logger.throwing(Level.DEBUG, e);
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ClassCastException e) {
            logger.error("script {} didn't return a configuration map");
            return false;
        }
    }

    private Reader getScriptReader() throws IOException {
        Path scriptp = Paths.get(script);
        Reader r;
        if(Files.isReadable(scriptp)) {
            r = Files.newBufferedReader(scriptp, Charset.defaultCharset());
        } else {
            InputStream is = getClass().getClassLoader().getResourceAsStream(script);
            r = new BufferedReader(new InputStreamReader(is));
        }
        return r;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

}
