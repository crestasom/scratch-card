
package com.crestasom.scratchcard;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.crestasom.scratchcard.entity.GameState;
import com.crestasom.scratchcard.entity.config.GameConfig;
import com.crestasom.scratchcard.util.ScratchCardUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainApp {
    public static int betAmount;
    public static String configPath;

    public static void main(String[] args) throws IOException {
        parseCli(args);
        log.debug("load game config");
        GameConfig gameConfig = ScratchCardUtils.loadConfig(configPath);
        log.debug("init matrix");
        GameState matrix = ScratchCardUtils.initMatrix(gameConfig);
        matrix.setBetAmount(betAmount);
        log.debug("matrix {}", matrix);
        ScratchCardUtils.addBonusSymbol(gameConfig, matrix);
        log.debug("matrix after adding bonus {}", matrix);
        ScratchCardUtils.calculate(gameConfig, matrix);
        System.out.println(matrix);

    }

    public static void parseCli(String[] args) {
        Options options = new Options();

        Option config =
                Option.builder().longOpt("config").hasArg().desc("Path to the config JSON file").required().build();

        Option bettingAmount = Option.builder().longOpt("betting-amount").hasArg().desc("Betting amount as integer")
                .type(Number.class).required().build();

        options.addOption(config);
        options.addOption(bettingAmount);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            configPath = cmd.getOptionValue("config");
            betAmount = Integer.parseInt(cmd.getOptionValue("betting-amount"));
            log.debug("Config:{} ", configPath);
            log.debug("Betting Amount::{} ", betAmount);
        } catch (ParseException e) {
            log.error("Error:[{}} ", e.getMessage(), e);
            formatter.printHelp("java -jar yourapp.jar", options);
            System.exit(1);
        }
    }


}
