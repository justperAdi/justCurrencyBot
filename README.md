# JustCurrencyBot

Telegram Bot for Currency Change History

## App.java
Starts the bot on the specified parameters "bot name" and "token"

## Class Bot.java
- Override basic methods of TelegramLongPollingBot.
- implement command botConnect. Register selected bot in TelegramAPI
- When receiving update only logged the event is not taking any action.

### Handlers and Command
- add special class for Command
- add Parser for Command
- add Handlers for Command

### Threads
- Sender Thread
- Receiver Thread
- Sample of the operate command in a separate thread. Notify command and class 