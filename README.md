# AndroidTestLogsGenerator

## Примеры запросов для фильтрации
* package:mine
* package:com.alien.testlogsgenerator
* tag:tag1
* message:Exception
* process:system_server
* is:crash
* age:2m
* (tag:tag1 | tag:examplecustomtag2) & level:verbose
* -message:"Exception"
* message~:Exception.*fatal
  
