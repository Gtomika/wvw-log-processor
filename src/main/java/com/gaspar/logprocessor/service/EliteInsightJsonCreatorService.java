package com.gaspar.logprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
@Slf4j
public class EliteInsightJsonCreatorService {

    public void getLogJsonFromEliteInsight(Path logFile, BiConsumer<String, Path> onSuccess, Consumer<Path> onFail) {

    }

}
