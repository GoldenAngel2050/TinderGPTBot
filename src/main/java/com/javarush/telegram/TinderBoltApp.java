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
    public static final String TELEGRAM_BOT_TOKEN = "";
    public static final String OPEN_AI_TOKEN = "";

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> listMessage = new ArrayList<>();

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if(message.equals("/start")){
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

        if(message.equals("/gpt")){
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if(message.equals("/date")){
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде","date_grande",
                    "Марого Робби","date_robbie",
                    "Зендея","date_zendaya",
                    "Райн Гослинг","date_golsing",
                    "Том Харди","date_hardy");
            return;
        }

        if(currentMode == DialogMode.GPT){
            String prompt = loadMessage("gpt");
            Message msg = sendTextMessage("Подождите несколько секунд, чат GPT думает...");
            String answer = chatGPTService.sendMessage(prompt, message);
            updateTextMessage(msg, answer);

            return;
        }

        if(currentMode == DialogMode.DATE){
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("date_")){
                sendPhotoMessage(query);
                sendTextMessage("Прекрасно, а теперь пригласи партнёра на свидание за 5 сообщений.");
                String prompt = loadMessage(query);
                chatGPTService.setPrompt(prompt);
                return;
            }
            Message msg = sendTextMessage("Подождите несколько секунд, партнёр набирает сообщение...");
            String answer = chatGPTService.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        if(message.equals("/message")){
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующие сообщения", "message_next",
            "Пригласить на свидание", "message_date");
            return;
        }

        if(currentMode == DialogMode.MESSAGE){
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", listMessage);
                Message msg = sendTextMessage("Подождите несколько секунд, чат GPT думает...");
                String answer = chatGPTService.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            listMessage.add(message);
            return;
        }

        sendTextMessage("Приветсвутю, я GoldiGPT");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
