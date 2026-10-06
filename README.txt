Client Utils - Fabric mod for Minecraft Java 1.21.11
Features: kit saving, big items, command keys, no fog

BUILD (PC):
1. Java 21 + IntelliJ IDEA install karo.
2. IntelliJ mein File > Open > is folder (clientutils-mod) ko open karo. Gradle sync hone do (pehli baar thoda time lagta hai).
3. Gradle tool window > Tasks > build > build chalao
   (ya terminal mein: gradlew build  -- agar gradlew files hain)
4. Jar milega: build/libs/clientutils-1.0.0.jar  (NOT -sources.jar)
5. Jar ko .minecraft/mods mein daalo. Fabric Loader + Fabric API (1.21.11) chahiye.

SETTINGS: src/client/java/com/example/clientutils/ClientUtilsClient.java ke top par
  BIG_ITEM_SCALE, COMMANDS, KEYS

KITS: /kitsave naam, /kitload naam (creative only), /kitlist

BUILD WITHOUT INSTALLING ANYTHING (GitHub Actions):
1. github.com pe login karo, New repository banao.
2. Is folder ki saari files upload karo (.github folder samet).
3. Actions tab > "build" > Run workflow (ya upload ke baad khud chalta hai).
4. Green tick ke baad run kholo > neeche Artifacts > clientutils-mod download karo (zip ke andar jar hai).
