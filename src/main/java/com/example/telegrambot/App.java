package com.example.telegrambot;

import com.example.telegrambot.bot.Bot;
import com.example.telegrambot.internal.ApiContextInitializer;
import com.example.telegrambot.service.MessageReciever;
import com.example.telegrambot.service.MessageSender;
import org.apache.log4j.Logger;
//import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.api.methods.send.SendMessage;

public class App {
//    private static final Logger log = Logger.getLogger(App.class);
    private static final int PRIORITY_FOR_SENDER = 1;
    private static final int PRIORITY_FOR_RECEIVER = 3;
    private static final String BOT_ADMIN = "321644283";

    public static void main(String[] args) {
        ApiContextInitializer.init();
        Bot just_currency_bot = new Bot("just_currency_bot", "2022272703:AAH5-4lQcM8N-BuE0LhujFAUrRGUb-GwoTM");

        MessageReciever messageReciever = new MessageReciever(just_currency_bot);
        MessageSender messageSender = new MessageSender(just_currency_bot);

        just_currency_bot.botConnect();



        Thread receiver = new Thread(messageReciever);
        receiver.setDaemon(true);
        receiver.setName("MsgReciever");
        receiver.setPriority(PRIORITY_FOR_RECEIVER);
        receiver.start();

        Thread sender = new Thread(messageSender);
        sender.setDaemon(true);
        sender.setName("MsgSender");
        sender.setPriority(PRIORITY_FOR_SENDER);
        sender.start();

        sendStartReport(just_currency_bot);
    }

    private static void sendStartReport(Bot bot) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(BOT_ADMIN);
        sendMessage.setText("Запустился");
        bot.sendQueue.add(sendMessage);
    }
}
