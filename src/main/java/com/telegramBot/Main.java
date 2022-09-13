package com.telegramBot;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends TelegramLongPollingBot {
    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallBackQuery(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    private String parserText() throws IOException {
        ArrayList<String> quoteList = new ArrayList<>();
        Document doc = Jsoup.connect("https://citatnica.ru/citaty/tsitaty-iz-knigi-zolotoj-telenok-300-tsitat").get();
        String className = "su-note-inner su-u-clearfix su-u-trim";
        Elements postElements = doc.getElementsByClass(className);
        postElements.forEach(quote -> quoteList.add(quote.text()));

        int randomValue = (int) (Math.random() * quoteList.size());
        return quoteList.get(randomValue);

    }

    private String parserImg() throws IOException {
        ArrayList<String> imgList = new ArrayList<>();
        Document doc = Jsoup.connect("https://zagony.ru/2008/09/02/samye-luchshie-fotografii-dikojj-prirody-40-foto.html").get();
        Elements postElements = doc.select(".highslide");
        postElements.forEach(img -> imgList.add(img.attr("href")));

        int randomValue = (int) (Math.random() * imgList.size());
        return imgList.get(randomValue);
    }

    @SneakyThrows
    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String data = callbackQuery.getData();
        if (data.equals("button1")) {
            SendMessage out = new SendMessage();
            out.setChatId(message.getChatId());
            out.setText(parserText());
            execute(out);
        }
        if (data.equals("button2")) {
            SendPhoto messagePhoto = new SendPhoto();
            messagePhoto.setChatId(message.getChatId().toString());
            messagePhoto.setPhoto(new InputFile(parserImg()));
            execute(messagePhoto);
        }
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        if (message.getText().equals("/start")) {
            execute(SendMessage
                    .builder()
                    .text("привет, меня зовут dreary. Пожалуйста, нажми на кнопку 'меню'")
                    .chatId(message.getChatId().toString())
                    .build());
        }
        // handle command
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/set_mood":
                        InlineKeyboardButton buttonYes = new InlineKeyboardButton();
                        buttonYes.setText("Цитаты");
                        buttonYes.setCallbackData("button1");

                        InlineKeyboardButton buttonNo = new InlineKeyboardButton();
                        buttonNo.setText("Фотографии");
                        buttonNo.setCallbackData("button2");

                        List<InlineKeyboardButton> button = new ArrayList<>();
                        button.add(buttonYes);
                        button.add(buttonNo);

                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        buttons.add(button);
                        execute(
                                SendMessage.builder()
                                        .text("Хочешь послушать интересные цитаты или посмотреть фотографии природы?")
                                        .chatId(message.getChatId().toString())
                                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                        .build());
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "DrearyautumnBot";
    }

    @Override
    public String getBotToken() {
        return "5450495691:AAHkCrGQK3u151CeQWuoZoHYUEOPzMc5ndY";
    }

    @SneakyThrows
    public static void main(String[] args) {
        Main bot = new Main();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
    }
}
