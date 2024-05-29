package com.javarush.telegram;

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

    private UserInfo me;
    private UserInfo she;
    private int questionCount;

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

        if(currentMode == DialogMode.GPT && !isMessageCommand()){
            String prompt = loadMessage("gpt");
            Message msg = sendTextMessage("Подождите несколько секунд, чат GPT думает...");
            String answer = chatGPTService.sendMessage(prompt, message);
            updateTextMessage(msg, answer);

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

        if(currentMode == DialogMode.DATE && !isMessageCommand()){
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

        if(currentMode == DialogMode.MESSAGE && !isMessageCommand()){
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

        if(message.equals("/profile")){
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько вам лет?");

        }

        if(currentMode == DialogMode.PROFILE && !isMessageCommand()){
            switch (questionCount){
                case 1 -> {
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("Кем вы работаете?");
                }
                case 2 -> {
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("Есть ли у вас хобби?");
                }
                case 3 -> {
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Что вам не нравится в людях?");
                }
                case 4 -> {
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                }
                case 5 -> {
                    me.goals = message;
                    questionCount = 6;

                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите несколько секунд, чат GPT думает...");
                    String answer = chatGPTService.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                }
            }
            return;
        }

        if(message.equals("/opener")){
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки?");
            return;
        }

        if(currentMode == DialogMode.OPENER && !isMessageCommand()){
            switch (questionCount) {
                case 1 -> {
                    she.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                }
                case 2 -> {
                    she.age = message;
                    questionCount = 3;
                    sendTextMessage("Есть ли у неё хобби и какие?");
                }
                case 3 -> {
                    she.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Кем она работает?");
                }
                case 4 -> {
                    she.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Цели знакомства?");
                }
                case 5 -> {
                    she.goals = message;
                    String aboutFriend = she.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Подождите несколько секунд, чат GPT думает...");
                    String answer = chatGPTService.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                }
            }
            return;

        }

        sendTextMessage("Приветсвутю, я GoldiGPT");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
