@echo off
REM Устанавливаем кодовую страницу UTF-8 для правильного отображения русского текста
chcp 65001 >nul 2>&1
REM Запускаем сервер
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target\Server-1.0-SNAPSHOT-jar-with-dependencies.jar
pause

