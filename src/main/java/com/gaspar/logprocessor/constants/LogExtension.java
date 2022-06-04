package com.gaspar.logprocessor.constants;

import lombok.Getter;

@Getter
public enum LogExtension {

    EVTC(".evtc"),

    ZEVTC(".zevtc");

    private String extension;

    LogExtension(String extension) {
        this.extension = extension;
    }

}
