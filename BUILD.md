# Сборка gym-mod

Нужно: JDK 25 и интернет (первый раз скачает Minecraft и зависимости).

Windows (PowerShell / cmd):   gradlew.bat build
Linux / macOS:                ./gradlew build

Готовый мод: build/libs/gymmod-1.0.0.jar  (файл *-sources.jar не нужен)
Положи его в папку mods вместе с Fabric API 0.161.0+26.2 и Fabric Loader 0.19.5+.

Если Gradle ругается на версию Loom/Gradle — актуальные версии на https://fabricmc.net/develop/
(правятся в gradle.properties и gradle/wrapper/gradle-wrapper.properties).

Без JDK: залей папку на GitHub — вкладка Actions → build соберёт jar (Artifacts).
