package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "goldi_gpt_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7237305081:AAFceyjkPfWLaX2SI8U_ffUkEwqfaqtwxao";
    public static final String OPEN_AI_TOKEN = "sk-proj-F06209zzjcJv29AqJFKTT3BlbkFJvDQ75eWCoblwtakzM7GR";

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    @Override
    public void onUpdateEventReceived(Update update) {
        String massage = getMessageText();
        if(massage.equals("/start")){
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("Главное меню бота", "/start",
                    "Задать вопрос чату GPT \uD83E\uDDE0", "/gpt",
                    "Генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "Сообщение для знакомства \uD83E\uDD70", "/opener",
                    "Переписка от вашего имени \uD83D\uDE08", "/message",
                    "Переписка со звездами \uD83D\uDD25", "/date");
            return;
        }

        if(massage.equals("/gpt")){
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if(currentMode == DialogMode.GPT){
            String prompt = loadMessage("gpt");
            String answer = chatGPTService.sendMessage(prompt, massage);
            sendTextMessage(answer);
            return;
        }

        sendTextMessage("Приветсвутю, я GoldiGPT");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
