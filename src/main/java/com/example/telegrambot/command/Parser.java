package com.example.telegrambot.command;

//import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private static final Logger log = Logger.getLogger(Parser.class);
    private final String PREFIX_FOR_COMMAND = "/";
    private final String DELIMITER_COMMAND_BOTNAME = "@";
    private String botName;

    public Parser(String botName) {
        this.botName = botName;
    }

    public ParsedCommand getParsedCommand(String text) {
        String trimText = "";
        if (text != null) trimText = text.trim();
        ParsedCommand result = new ParsedCommand(Command.NONE, trimText);

        if ("".equals(trimText)) return result;
        Map<String, String> commandAndText = getDelimitedCommandFromText(trimText);
        for (String key:commandAndText.keySet()){
            if (isCommand(key)) {
                if (isCommandForMe(key)) {
                    String commandForParse = cutCommandFromFullText(key);
                    Command commandFromText = getCommandFromText(commandForParse);
                    result.setText(commandAndText.get(key));
                    result.setCommand(commandFromText);
                } else {
                    result.setCommand(Command.NOTFORME);
                    result.setText(commandAndText.get(key));
                }

            }
        }

        return result;
    }

    private String cutCommandFromFullText(String text) {
        return text.contains(DELIMITER_COMMAND_BOTNAME) ?
                text.substring(1, text.indexOf(DELIMITER_COMMAND_BOTNAME)) :
                text.substring(1);
    }

    private Command getCommandFromText(String text) {
        String upperCaseText = text.toUpperCase().trim();
        Command command = Command.NONE;
        try {
            command = Command.valueOf(upperCaseText);
        } catch (IllegalArgumentException e) {
            log.debug("Can't parse command: " + text);
        }
        return command;
    }

    private Map<String, String> getDelimitedCommandFromText(String trimText) {
        Map<String, String> commandText;

        if (trimText.contains(" ")) {
            int indexOfSpace = trimText.indexOf(" ");
            Map<String, String> test = new HashMap<>();
            test.put(trimText.substring(0, indexOfSpace), trimText.substring(indexOfSpace + 1));
            commandText = test;
        } else {
            Map<String, String> test = new HashMap<>();
            test.put(trimText, "");
            commandText = test;
        }
        return commandText;
    }

    private boolean isCommandForMe(String command) {
        if (command.contains(DELIMITER_COMMAND_BOTNAME)) {
            String botNameForEqual = command.substring(command.indexOf(DELIMITER_COMMAND_BOTNAME) + 1);
            return botName.equals(botNameForEqual);
        }
        return true;
    }

    private boolean isCommand(String text) {
        return text.startsWith(PREFIX_FOR_COMMAND);
    }
}
