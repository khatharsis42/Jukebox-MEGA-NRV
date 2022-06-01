package cj.jukebox.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.LoggerConfig

class Loggers {
    companion object {
        const val ON = true
        /**
         * name of logger for the general part (config, etc.).
         */
        const val LOGGER_NAME_GEN = "general"

        /**
         * logger object for the general part.
         */
        final val GEN = LogManager.getLogger(LOGGER_NAME_GEN)

        /**
         * name of logger for the debug.
         */
        const val LOGGER_NAME_DEBUG = "debug"

        /**
         * logger object for the debug.
         */
        final val DEBUG = LogManager.getLogger(LOGGER_NAME_DEBUG)

        /**
         * name of logger for the testing part (used in JUnit classes).
         */
        const val LOGGER_NAME_TEST = "test"

        /**
         * logger object for the testing part.
         */
        final val TEST = LogManager.getLogger(LOGGER_NAME_TEST)

        /**
         * name of logger for the download part.
         */
        const val LOGGER_NAME_DOWNLOAD = "download"

        /**
         * logger object for the download part.
         */
        final val DL = LogManager.getLogger(LOGGER_NAME_DOWNLOAD)

        /**
         * static configuration, which can be changed by command line options.
         */
        init {
            setLevel(GEN, Level.INFO);
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
        fun setLevel(logger: Logger, level: Level?) {
            val ctx = LogManager.getContext(false) as LoggerContext
            val config = ctx.configuration
            val loggerConfig = config.getLoggerConfig(logger.name)
            var specificConfig = loggerConfig
            // We need a specific configuration for this logger,
            // otherwise we would change the level of all other loggers
            // having the original configuration as parent as well
            if (loggerConfig.name != logger.name) {
                specificConfig = LoggerConfig(logger.name, level, true)
                specificConfig.parent = loggerConfig
                config.addLogger(logger.name, specificConfig)
            }
            specificConfig.level = level
            ctx.updateLoggers()
        }
    }
}