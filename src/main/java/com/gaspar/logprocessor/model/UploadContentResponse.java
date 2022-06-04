package com.gaspar.logprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dps.report APIs response when calling /uploadContent endpoint.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadContentResponse {

    /**
     * Identifier of the generated dps report.
     */
    private String id;

    /**
     * Permalink to this upload.
     */
    private String permalink;

    /**
     * Optional error field populated by dps.report if there was an issue.
     */
    private String error;

}
