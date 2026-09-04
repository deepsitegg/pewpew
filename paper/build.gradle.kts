import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
}

val MAINTAINERS = listOf("ThebigTijn")

dependencies {
    implementation(project(":core"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation("com.jazzkuh.modulemanager:spigot:1.0-SNAPSHOT")
    implementation("com.jazzkuh.commandlib:spigot:1.0-SNAPSHOT")
    implementation("com.jazzkuh.inventorylib:spigot:1.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.2.1")

    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.spongepowered:configurate-core:4.1.2")
    compileOnly("commons-io:commons-io:2.15.1")

    compileOnly("com.github.SkriptLang:Skript:2.15.3")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13") { isTransitive = false }
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.13") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0") { isTransitive = false }

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.spongepowered:configurate-yaml:4.1.2")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("pewpew.jar")
    // published coordinate stays gg.deepsite:pewpew:<version> with no "-all" suffix
    archiveClassifier.set("")

    relocate("com.jazzkuh.modulemanager", "gg.deepsite.pewpew.libs.modulemanager")
    relocate("com.jazzkuh.commandlib", "gg.deepsite.pewpew.libs.commandlib")
    relocate("com.jazzkuh.inventorylib", "gg.deepsite.pewpew.libs.inventorylib")
    relocate("org.bstats", "gg.deepsite.pewpew.bstats")

    exclude("net/kyori/**")
    exclude("org/slf4j/**")
    exclude("javassist/**")
    exclude("javax/**")
    exclude("org/jetbrains/**")
    exclude("org/intellij/**")
    exclude("org/jspecify/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("module-info.class")

    manifest {
        attributes["Main-Class"] = "gg.deepsite.pewpew.PewpewPlugin"
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Build-Time"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        attributes["Implementation-Version"] = version
        attributes["Maintainers"] = MAINTAINERS.joinToString(", ")
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            artifactId = "pewpew"
            // publishes the shadowed jar with a correct POM
            from(components["shadow"])
        }
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Dcom.mojang.eula.agree=true", "-Dfile.encoding=UTF-8")
        downloadPlugins {
            modrinth("Skript", "2.15.3")
        }
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
