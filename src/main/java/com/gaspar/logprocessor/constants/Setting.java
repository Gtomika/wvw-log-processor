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
     * String, path to the folder where results will be placed.
     */
    TARGET_FOLDER,

}