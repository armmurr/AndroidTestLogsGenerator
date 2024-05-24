# AndroidTestLogsGenerator

## Примеры запросов для фильтрации в logcat AndroidStudio
* **package:mine** - все пакеты, которые есть в проекте, который открыт на данный момент в AndroidStudio
* **package:com.alien.testlogsgenerator** - конкретный пакет по имени
* **tag:tag1** - сообщения с конкретным тэгом
* **message:Exception** - сообщения со словом **Exception**
* **process:system_server** - сообщения принадлежащие процессу с именем **system_server**
* **is:crash** - все краши
* **age:2m** - все сообщения, не старше **2m**
* **(tag:tag1 | tag:examplecustomtag2) & level:verbose**
* **-message:"Exception"** - поиск с отрицанием, все сообщения, где нет слова **Exception**
* **message~:Exception.\*fatal** - поиск с регулярным выражением, все сообщения, где после **Exception** через любое количество символов есть слово **fatal**
* **tag~:tag\d** - поиск с регулярным выражением, все сообщения, где после **tag** есть число
  
