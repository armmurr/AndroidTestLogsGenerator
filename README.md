# AndroidTestLogsGenerator [En]

This application is designed to practice the skills of reading logs, searching for errors in them, and setting up filtering in Logcat. It also allows for real-time monitoring of the activity lifecycle.

## Features
* Generation of random logs with completely random parameters;
* Ability to create custom messages with user-defined text, adjustable frequency, and other parameters;
* Can emulate crashes, freezes, and other types of errors;
* Shows in real time which Activity lifecycle events are occurring, allowing you to easily model the expected behavior of your application in similar cases.

![image](https://github.com/user-attachments/assets/13ef3584-e9af-4649-8b87-2538cdfdce3f)

## Examples of filter queries in Android Studio's logcat
* **package:mine** - all packages in the project currently open in Android Studio
* **package:com.alien.testlogsgenerator** - a specific package by name
* **tag:tag1** - messages with a specific tag
* **message:Exception** - messages containing the word **Exception**
* **process:system_server** - messages belonging to the process named **system_server**
* **is:crash** - all crashes
* **age:2m** - all messages no older than **2m**
* **(tag:tag1 | tag:examplecustomtag2) & level:verbose** - searching with logical expressions, all messages where there is either **tag1** or **examplecustomtag2** with the **Debug** level
* **-message:"Exception"** - search with negation, all messages that do not contain the word **Exception**
* **message~:Exception.\*fatal** - search with a regular expression, all messages where after **Exception**, with any number of characters in between, there is the word **fatal**
* **tag~:tag\d** - search with a regular expression, all messages where after **tag** there is a number

# AndroidTestLogsGenerator [Ru]

Приложение предназначено для отработки навыков чтения логов, поиска в них ошибок и настройки фильтрации в Logcat. А также за мониторингом жизненного цикла activity в реальном времени.

## Возможности
* Генерация случайных логов, с послностью случайными параметрами;
* Возможность создания собственных сообщений, с пользовательским текстом, с настройкой переодичности и другими параметрами;
* Умеет эмулировать краши, зависания и другие типы ошибок;
* В реальном времени показывает какие события жизненного цикла Activity происходят, с помощью чего вы можете легко смоделировать ожидаемое поведение вашего приложения при аналогичных кейсах.

![image](https://github.com/user-attachments/assets/13ef3584-e9af-4649-8b87-2538cdfdce3f)

## Примеры запросов для фильтрации в logcat AndroidStudio
* **package:mine** - все пакеты, которые есть в проекте, который открыт на данный момент в AndroidStudio
* **package:com.alien.testlogsgenerator** - конкретный пакет по имени
* **tag:tag1** - сообщения с конкретным тэгом
* **message:Exception** - сообщения со словом **Exception**
* **process:system_server** - сообщения принадлежащие процессу с именем **system_server**
* **is:crash** - все краши
* **age:2m** - все сообщения, не старше **2m**
* **(tag:tag1 | tag:examplecustomtag2) & level:verbose** - поиск с логическими выражениями, все сообщения где есть или **tag1** или **examplecustomtag2** c уровнем **Debug**
* **-message:"Exception"** - поиск с отрицанием, все сообщения, где нет слова **Exception**
* **message~:Exception.\*fatal** - поиск с регулярным выражением, все сообщения, где после **Exception** через любое количество символов есть слово **fatal**
* **tag~:tag\d** - поиск с регулярным выражением, все сообщения, где после **tag** есть число
