
package com.crestasom.scratchcard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

import com.crestasom.scratchcard.config.GameConfig;
import com.crestasom.scratchcard.config.StandardSymbolProbability;
import com.crestasom.scratchcard.entity.CurrentMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainApp {
    private static final int rowNum = 3;
    private static final int colNum = 3;
    private Random random = new Random();
    public static void main(String[] args) throws IOException {
        System.out.println("running");
        MainApp app = new MainApp();
        log.debug("load game config");
        GameConfig gameConfig = app.loadConfig();
        log.debug("init matrix");
        CurrentMatrix matrix = app.initMatrix(gameConfig);
        log.debug("matrix {}", matrix);
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

    public CurrentMatrix initMatrix(GameConfig gameConfig) {
        CurrentMatrix matrix = new CurrentMatrix(rowNum, colNum);
        gameConfig.getProbabilities().getStandardSymbols().stream()
                .forEach(g -> initCurrentSymbol(g, matrix, gameConfig));
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {

            }
        }
        return matrix;
    }

    public void initCurrentSymbol(StandardSymbolProbability symbolProbability, CurrentMatrix matrix,
            GameConfig gameConfig) {

        int row=symbolProbability.getRow();
        int column=symbolProbability.getColumn();
        log.debug("init current symbol for [{}][{}]", row + 1, column + 1);
        int totalWeight = symbolProbability.getSymbols().values().stream().mapToInt(Integer::intValue).sum();
        int rand = random.nextInt(totalWeight);
        log.debug("rand [{}]", rand);
        int cumulativeWt = 0;
        String symbol = null;
        for (Map.Entry<String, Integer> entry : symbolProbability.getSymbols().entrySet()) {
            cumulativeWt += entry.getValue();
            if (rand < cumulativeWt) {
                log.debug("symbol to be init [{}]", symbol);
                symbol = entry.getKey();
                break;
            }
        }

        if (symbol == null) {
            throw new RuntimeException(String.format("cannot init symbol for %d %d", symbolProbability.getRow() + 1,
                    symbolProbability.getColumn() + 1));
        }
        log.debug("setting [{}] in [{}][{}]", symbol, row + 1, column + 1);
        matrix.setValue(row, column, symbol);

    }
}
