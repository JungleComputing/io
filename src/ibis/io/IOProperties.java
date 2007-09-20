/* $Id$ */

package ibis.io;

import ibis.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 * Collects all system properties used by the ibis.io package.
 */
class IOProperties implements Constants {
    static final TypedProperties properties;

    static final String PREFIX = "ibis.io.";

    /** Filename for the properties. */
    private static final String PROPERTIES_FILENAME = "ibis.properties";

    /** Property name of the property file. */
    public static final String PROPERTIES_FILE = PREFIX + "properties.file";
    
    static final Logger logger = Logger.getLogger("ibis.io");
     

    static final String s_stats_nonrewritten = PREFIX
            + "stats.nonrewritten";

    static final String s_stats_written = PREFIX + "stats.written";

    static final String s_classloader = PREFIX
            + "serialization.classloader";

    static final String s_timer = PREFIX + "serialization.timer";

    static final String s_no_array_buffers = PREFIX + "noarraybuffers";

    static final String s_conversion = PREFIX + "conversion";

    static final String s_buffer_size = PREFIX + "buffer.size";

    static final String s_array_buffer = PREFIX + "array.buffer";

    static final String s_dbg = PREFIX + "debug";

    static final String s_asserts = PREFIX + "assert";

    static final String s_small_array_bound = PREFIX
            + "smallarraybound";

    static final String s_hash_asserts = PREFIX + "hash.assert";

    static final String s_hash_stats = PREFIX + "hash.stats";

    static final String s_hash_timings = PREFIX + "hash.timings";

    static final String s_hash_resize = PREFIX + "hash.resize";
    
    static final String[] sysprops = {PROPERTIES_FILE,
            s_stats_nonrewritten,
            s_stats_written, s_classloader, s_timer, s_conversion, 
            s_dbg, s_asserts, s_small_array_bound,
            s_hash_asserts, s_hash_stats, s_hash_timings, s_hash_resize,
            s_buffer_size, s_array_buffer };

    static {
        properties = new TypedProperties(getDefaultProperties());
        properties.checkProperties(PREFIX, sysprops, null, true);
    }

    static final boolean DEBUG = properties.getBooleanProperty(s_dbg, false);

    public static final boolean ASSERTS = properties.getBooleanProperty(s_asserts, false);

    public static final int SMALL_ARRAY_BOUND
            = properties.getIntProperty(s_small_array_bound, 256); // byte

    public static final int BUFFER_SIZE = properties.getIntProperty(
            s_buffer_size, 4 * 1024);

    public static final int ARRAY_BUFFER_SIZE
            = properties.getIntProperty(s_array_buffer, 32);

    private static Properties getPropertyFile(String file) {

        InputStream in = null;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // ignored
        }

        if (in == null) {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            in = loader.getResourceAsStream(file);
            if (in == null) {
                return null;
            }
        }

        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            try {
                in.close();
            } catch (Exception x) {
                // ignore
            }
        }
        return null;
    }

    private static void addProperties(Properties props, Properties p) {
        for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = p.getProperty(key);
            props.setProperty(key, value);
        }
    }

    private static Properties getDefaultProperties() {

        Properties props = new Properties();
        
        // Get the properties from the commandline. 
        Properties system = System.getProperties();

        // Check what property file we should load.
        String file = system.getProperty(PROPERTIES_FILE,
                PROPERTIES_FILENAME);

        // If the file is not explicitly set to null, we try to load it.
        // First try the filename as is, if this fails try with the
        // user home directory prepended.
        if (file != null) {
            Properties fromFile = getPropertyFile(file);
            if (fromFile != null) {
                addProperties(props, fromFile);
            } else {
                String homeFn = System.getProperty("user.home")
                    + System.getProperty("file.separator") + file;
                fromFile = getPropertyFile(homeFn);
                
                if (fromFile == null) { 
                    if (! file.equals(PROPERTIES_FILENAME)) { 
                        // If we fail to load the user specified file,
                        // we give an error, since only the default file
                        // may fail silently.                     
                        System.err.println("User specified preferences \""
                                + file + "\" not found!");
                    }                                            
                } else {                  
                    // If we managed to load the file, we add the
                    // properties to the props, possibly
                    // overwriting defaults.
                    addProperties(props, fromFile);
                }
            }
        }

        // Finally, add the system properties (also from the command line)
        // to the result, possibly overriding entries from file or the 
        // defaults.            
        addProperties(props, system);

        return props;
    }
}
