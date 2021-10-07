package com.example.telegrambot.service;

import com.example.telegrambot.bot.Bot;
import com.example.telegrambot.command.Command;
import com.example.telegrambot.command.ParsedCommand;
import com.example.telegrambot.command.Parser;
import com.example.telegrambot.handler.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.stickers.Sticker;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MessageReciever implements Runnable {
    private static final Logger log = Logger.getLogger(MessageReciever.class);
    private final int WAIT_FOR_NEW_MESSAGE_DELAY = 1000;
    private final String END_LINE = "\n";
    private Bot bot;
    private Parser parser;

    public MessageReciever(Bot bot) {
        this.bot = bot;
        parser = new Parser(bot.getBotName());
    }

    @Override
    public void run() {
        log.info("[STARTED] MsgReciever.  Bot class: " + bot);
        while (true) {
            for (Object object = bot.receiveQueue.poll(); object != null; object = bot.receiveQueue.poll()) {
                log.debug("New object for analyze in queue " + object.toString());
                analyze(object);
            }
            try {
                Thread.sleep(WAIT_FOR_NEW_MESSAGE_DELAY);
            } catch (InterruptedException e) {
                log.error("Catch interrupt. Exit", e);
                return;
            }
        }
    }

    private void analyze(Object object) {
        if (object instanceof Update) {
            Update update = (Update) object;
            log.debug("Update recieved: " + update.toString());
            if (update.hasMessage() && update.getMessage().hasText()) {
                analyzeForUpdateType(update);
            } else if (update.hasCallbackQuery()) {
                // Set variables
                String call_data = update.getCallbackQuery().getData();
                Integer message_id = update.getCallbackQuery().getMessage().getMessageId();
                long chat_id = update.getCallbackQuery().getMessage().getChatId();

                if (call_data.equals("eur") || call_data.equals("usd") ||  call_data.equals("rub")) {
                    String answer = "Updated message text";

                    SendMessage new_message = new SendMessage()
                            .setChatId(chat_id)
                            .setText(getCurrency().toString())
                            .setReplyMarkup(SystemHandler.setButtons());
                    try {
                        bot.execute(new_message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

            }
        } else log.warn("Cant operate type of object: " + object.toString());
    }

    private void analyzeForUpdateType(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();

        ParsedCommand parsedCommand = new ParsedCommand(Command.NONE, "");

        if (message.hasText()) {
            parsedCommand = parser.getParsedCommand(message.getText());
        } else {
            Sticker sticker = message.getSticker();
            if (sticker != null) {
                parsedCommand = new ParsedCommand(Command.STICKER, sticker.getFileId());
            }
        }

        AbstractHandler handlerForCommand = getHandlerForCommand(parsedCommand.getCommand());
        String operationResult = handlerForCommand.operate(chatId.toString(), parsedCommand, update);

        if (!"".equals(operationResult)) {
            SendMessage messageOut = new SendMessage();
            messageOut.setChatId(chatId);
            messageOut.setText(operationResult);
            bot.sendQueue.add(messageOut);
        }
    }

    private AbstractHandler getHandlerForCommand(Command command) {
        if (command == null) {
            log.warn("Null command accepted. This is not good scenario.");
            return new DefaultHandler(bot);
        }
        switch (command) {
            case START:
            case HELP:
            case ID:
                SystemHandler systemHandler = new SystemHandler(bot);
                log.info("Handler for command[" + command.toString() + "] is: " + systemHandler);
                return systemHandler;
            case NOTIFY:
                NotifyHandler notifyHandler = new NotifyHandler(bot);
                log.info("Handler for command[" + command.toString() + "] is: " + notifyHandler);
                return notifyHandler;
            default:
                log.info("Handler for command[" + command.toString() + "] not Set. Return DefaultHandler");
                return new DefaultHandler(bot);
        }
    }

    private static HttpURLConnection connection;
    private static HttpURLConnection connection2;

    public StringBuilder getCurrency(){
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterFrom = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat day = new SimpleDateFormat("dd");
        Date date = new Date();
        int days = Integer.parseInt(day.format(date));
        System.out.println(formatter.format(date) + " " + day.format(date));

        StringBuilder text = new StringBuilder();
        text.append("USD:              EUR          RUB").append(END_LINE);

//        int itr = 10;
//        while(itr != 0){
            try{

//                String urlS = "http://api.currencylayer.com/historical?access_key=5ad0f86d860fdf0390898bb5f551d58c&date="
//                        + formatter.format(date) + "-" + String.valueOf(days);
////                System.out.println(urlS);
//
                String urlS = "http://localhost:8080/currency/last/dozen/" + formatterFrom.format(date)
                        + (Integer.parseInt(day.format(date)) - 10) + "/"
                        + formatter.format(date);
                System.out.println(urlS);
                URL url = new URL(urlS);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();
//                System.out.println(status);
                if(status > 299){
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
//            System.out.println(responseContent.toString());
//                System.out.println(responseContent.toString());



                text.append(parser(responseContent.toString()));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
//            days--;
//            itr--;

//            responseContent.delete(0, responseContent.length());

//        }

        return text;

    }

    public StringBuilder parser(String responseBody){
        StringBuilder test = new StringBuilder();
        JSONArray jsonArray = new JSONArray (responseBody);
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);

//            if(!jsonObject.getBoolean("success")){
//                System.out.println(jsonObject.getJSONObject("error").getString("info"));
//                return jsonObject.getJSONObject("error").getString("info");
//            }
//            String source = jsonObject.getString("source");
            String source = "USD";
            String date = jsonObject.getString("date");
            BigDecimal eur = jsonObject.getBigDecimal("eur");
            BigDecimal rub = jsonObject.getBigDecimal("rub");
            System.out.println(date + " " + eur + " " + rub);

            DecimalFormat df = new DecimalFormat("###.#####");

                test.append(date.substring(0, date.indexOf("T")) + " ");
                test.append(df.format(eur) + " ");
                test.append(df.format(rub));

            test.append(END_LINE);
        }

//        return date + ": " + eur + " " + rub;
        return test;
    }
}
