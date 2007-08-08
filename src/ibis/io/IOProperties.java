/* $Id$ */

package ibis.io;

import ibis.util.TypedProperties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 * Collects all system properties used by the ibis.io package.
 */
class IOProperties implements Constants {
    static final TypedProperties properties;
    
    static {
        Logger ibisLogger = Logger.getLogger("ibis");
        Logger rootLogger = Logger.getRootLogger();
        if (! rootLogger.getAllAppenders().hasMoreElements()
             && !ibisLogger.getAllAppenders().hasMoreElements()) {
            // No appenders defined, print to standard err by default
            PatternLayout layout = new PatternLayout("%d{HH:mm:ss} %-5p %m%n");
            WriterAppender appender = new WriterAppender(layout, System.err);
            ibisLogger.addAppender(appender);
            ibisLogger.setLevel(Level.WARN);
        }
    }

    static final Logger logger = Logger.getLogger("ibis.io");
     
    static {
        properties = new TypedProperties();
        properties.loadDefaultConfigProperties();
    }

    static final String PROPERTY_PREFIX = "ibis.io.";

    static final String s_stats_nonrewritten = PROPERTY_PREFIX
            + "stats.nonrewritten";

    static final String s_stats_written = PROPERTY_PREFIX + "stats.written";

    static final String s_classloader = PROPERTY_PREFIX
            + "serialization.classloader";

    static final String s_timer = PROPERTY_PREFIX + "serialization.timer";

    static final String s_no_array_buffers = PROPERTY_PREFIX + "noarraybuffers";

    static final String s_conversion = PROPERTY_PREFIX + "conversion";

    static final String s_buffer_size = PROPERTY_PREFIX + "buffer.size";

    static final String s_array_buffer = PROPERTY_PREFIX + "array.buffer";

    static final String s_dbg = PROPERTY_PREFIX + "debug";

    static final String s_asserts = PROPERTY_PREFIX + "assert";

    static final String s_small_array_bound = PROPERTY_PREFIX
            + "smallarraybound";

    static final String s_hash_asserts = PROPERTY_PREFIX + "hash.assert";

    static final String s_hash_stats = PROPERTY_PREFIX + "hash.stats";

    static final String s_hash_timings = PROPERTY_PREFIX + "hash.timings";

    static final String s_hash_resize = PROPERTY_PREFIX + "hash.resize";
    
    static final boolean DEBUG = properties.getBooleanProperty(s_dbg, false);

    public static final boolean ASSERTS = properties.getBooleanProperty(s_asserts, false);

    public static final int SMALL_ARRAY_BOUND
            = properties.getIntProperty(s_small_array_bound, 256); // byte

    public static final int BUFFER_SIZE = properties.getIntProperty(
            s_buffer_size, 4 * 1024);

    public static final int ARRAY_BUFFER_SIZE
            = properties.getIntProperty(s_array_buffer, 1024);

    static final String[] sysprops = { s_stats_nonrewritten,
            s_stats_written, s_classloader, s_timer, s_conversion, 
            s_dbg, s_asserts, s_small_array_bound,
            s_hash_asserts, s_hash_stats, s_hash_timings, s_hash_resize,
            s_buffer_size, s_array_buffer };

    static {
        properties.checkProperties(PROPERTY_PREFIX, sysprops, null, true);
    }
}
