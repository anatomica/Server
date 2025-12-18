# PowerShell script to start server with UTF-8 encoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target\Server-1.0-SNAPSHOT-jar-with-dependencies.jar

