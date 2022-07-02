package com.gaspar.logprocessor.constants;

public enum Setting {

    /**
     * String, path to the folder where logs are present
     */
    SOURCE_FOLDER,

    /**
     * Boolean, whether to delete source log files or not.
     */
    SOURCE_DELETE_SOURCES,

    /**
     * List of {@link LogExtension} constants, control which files count as logs.
     */
    SOURCE_LOG_EXTENSIONS,

    /**
     * Integer, minimum size of the logs that should be processed in megabytes. Set to
     * {@link com.gaspar.logprocessor.service.SettingsService#MIN_SIZE_DISABLED} to disable
     * any minimum size.
     */
    SOURCE_MIN_SIZE_MB,

    /**
     * One of {@link JsonGenerator} constants, which way is to be used to
     * generate JSON from log file.
     */
    ENGINE_JSON_GENERATOR,

    /**
     * Boolean, whether to save permalinks to logs (only if selected JSON
     * generator is {@link JsonGenerator#DPS_REPORT_API}).
     */
    ENGINE_DPS_REPORT_SAVE_PERMALINKS,

    /**
     * String, path to local ELite Insight exe.
     */
    ENGINE_ELITE_INSIGHT_PATH,

    /**
     * Boolean, A nem tisztított JSON fájlok megtartása.
     */
    ENGINE_ELITE_INSIGHT_KEEP_BIG_JSON,

    /**
     * String, path to elite insight configuration.
     */
    ENGINE_ELITE_INSIGHT_CONF_PATH,

    /**
     * String, path to the folder where results will be placed.
     */
    TARGET_FOLDER,

}
