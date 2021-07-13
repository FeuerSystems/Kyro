
plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "dev.brys"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("net.dv8tion:JDA:4.3.0_294")
    implementation("io.github.cdimascio:java-dotenv:5.2.1")
    implementation("io.github.cdimascio:java-dotenv:5.2.1")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.mongodb:mongodb-driver-legacy:4.1.0-beta2")
    implementation("org.litote.kmongo:kmongo:4.2.3")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.google.apis:google-api-services-youtube:v3-rev212-1.25.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("io.ktor:ktor:1.5.0")
    implementation("io.ktor:ktor-server-netty:1.5.0")
    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("org.codehaus.jackson:jackson-mapper-asl:1.9.13")
    implementation("io.ktor:ktor-websockets:1.5.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
    implementation("org.json:json:20201115")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.codehaus.groovy:groovy-jsr223:3.0.7")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.5.0")
    implementation("com.jagrosh:jda-utilities:3.0.5")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.github.Kosert.FlowBus:FlowBus:1.1")
    implementation("io.javalin:javalin:3.13.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("net.explodingbush.KSoft4J:KSoft4J:1.0.6")

}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.suppressWarnings = true
    kotlinOptions.jvmTarget = JavaVersion.VERSION_14.toString()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(14))
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "brys.dev.kyro.BotKt"
    }
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    baseName = group
    classifier = "Kyro"
    version = version
}


