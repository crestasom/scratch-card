
package com.crestasom.scratchcard;

import java.io.IOException;
import java.io.InputStream;

import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.entity.CurrentMatrix;
import com.crestasom.scratchcard.util.ScratchCardUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainApp {

    public static void main(String[] args) throws IOException {
        System.out.println("running");
        MainApp app = new MainApp();
        log.debug("load game config");
        GameConfig gameConfig = app.loadConfig();
        log.debug("init matrix");
        CurrentMatrix matrix = ScratchCardUtils.initMatrix(gameConfig);
        log.debug("matrix {}", matrix);
        ScratchCardUtils.addBonusSymbol(gameConfig, matrix);
        log.debug("matrix after adding bonus {}", matrix);
    }

    public GameConfig loadConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/config.json")) {
            if (is == null) {
                throw new IOException("Config file not found: config.json");
            }
            return mapper.readValue(is, GameConfig.class);
        }
    }

}
