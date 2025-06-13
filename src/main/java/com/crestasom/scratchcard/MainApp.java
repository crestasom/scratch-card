
package com.crestasom.scratchcard;

import java.io.IOException;

import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.entity.CurrentMatrix;
import com.crestasom.scratchcard.util.ScratchCardUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainApp {

    public static void main(String[] args) throws IOException {

        log.debug("load game config");
        GameConfig gameConfig = ScratchCardUtils.loadConfig();
        log.debug("init matrix");
        CurrentMatrix matrix = ScratchCardUtils.initMatrix(gameConfig);
        log.debug("matrix {}", matrix);
        ScratchCardUtils.addBonusSymbol(gameConfig, matrix);
        log.debug("matrix after adding bonus {}", matrix);
    }



}
