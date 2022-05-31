package cj.jukebox.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * This class contains the configuration of some logging facilities.
 *
 * To recapitulate, logging levels are: GEN, DEBUG, TEST, DOWNLOAD, WARN.
 *
 * @author Khâtharsis
 *
 */
public final class Log {
    /**
     * states whether logging is enabled or not. Use this attribute when logging is
     * performed using not only <code>isXxxEnabled</code> methods but also more
     * complex instructions, e.g. loops.
     */
    public static final boolean ON = true;
    /**
     * name of logger for the general part (config, etc.).
     */
    public static final String LOGGER_NAME_GEN = "general";
    /**
     * logger object for the general part.
     */
    public static final Logger GEN = LogManager.getLogger(LOGGER_NAME_GEN);
    /**
     * name of logger for the debug.
     */
    public static final String LOGGER_NAME_DEBUG = "debug";
    /**
     * logger object for the debug.
     */
    public static final Logger DEBUG = LogManager.getLogger(LOGGER_NAME_DEBUG);
    /**
     * name of logger for the testing part (used in JUnit classes).
     */
    public static final String LOGGER_NAME_TEST = "test";
    /**
     * logger object for the testing part.
     */
    public static final Logger TEST = LogManager.getLogger(LOGGER_NAME_TEST);
    /**
     * name of logger for the testing part (used in JUnit classes).
     */
    public static final String LOGGER_NAME_WARN = "warning";
    /**
     * logger object for the testing part.
     */
    public static final Logger WARN = LogManager.getLogger(LOGGER_NAME_WARN);
    /**
     * name of logger for the testing part (used in JUnit classes).
     */
    public static final String LOGGER_NAME_DOWNLOAD = "download";
    /**
     * logger object for the testing part.
     */
    public static final Logger DL = LogManager.getLogger(LOGGER_NAME_DOWNLOAD);
    /*
     * static configuration, which can be changed by command line options.
     */
    static {
        setLevel(GEN, Level.INFO);
        setLevel(WARN, Level.WARN);
        setLevel(DEBUG, Level.INFO);
        setLevel(DL, Level.WARN);
        setLevel(TEST, Level.INFO);
    }

    /**
     * configures a logger to a level.
     *
     * @param logger the logger.
     * @param level  the level.
     */
    public static void setLevel(final Logger logger, final Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final var config = ctx.getConfiguration();
        var loggerConfig = config.getLoggerConfig(logger.getName());
        var specificConfig = loggerConfig;
        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        // having the original configuration as parent as well
        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }

    /**
     * private constructor to avoid instantiation.
     */
    private Log() {
    }
}
