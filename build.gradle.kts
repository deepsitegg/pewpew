
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.set

plugins {
    java
    id("io.freefair.lombok") version "8.13.1"
    id("com.gradleup.shadow") version "9.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "gg.deepsite"
version = "26.0.1"
val MAINTAINERS = listOf("ThebigTijn")

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

lombok {
    version = "1.18.46"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")

}

dependencies {
    /* Paper */
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    /* Modules */
    implementation("com.github.deepsitegg.modulemanager:spigot:main-SNAPSHOT")

    /* Command Library */
    implementation("com.github.deepsitegg.commandlibrary:spigot:adcdc9ba10")

    /* Inventory Library */
    implementation("com.github.deepsitegg.inventorylib:spigot:main-SNAPSHOT")

    /* Configuration */
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation("commons-io:commons-io:2.15.1")

    implementation("org.javassist:javassist:3.30.2-GA")

}

tasks.withType<ShadowJar> {
    archiveFileName.set("pewpew.jar")

    // Configurate is a multi-release jar; relocation moves its versioned classes to
    // META-INF/versions/<n>/gg/deepsite/... which hijacks classloader resource lookup
    // for our own package and breaks ModuleManager's Reflections scan. Base copies of
    // these classes exist in the main tree, so dropping the versioned tree is safe.
    exclude("META-INF/versions/**")

    relocate("com.jazzkuh.modulemanager", "gg.deepsite.pewpew.libs.modulemanager")
    relocate("com.jazzkuh.commandlib", "gg.deepsite.pewpew.libs.commandlib")
    relocate("com.jazzkuh.inventorylib", "gg.deepsite.pewpew.libs.inventorylib")
    relocate("org.spongepowered.configurate", "gg.deepsite.pewpew.libs.configurate")
    relocate("org.apache.commons.io", "gg.deepsite.pewpew.libs.commonsio")

    manifest {
        attributes["Main-Class"] = "gg.deepsite.pewpew.PewpewPlugin"
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Build-Time"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        attributes["Implementation-Version"] = version
        attributes["Maintainers"] = MAINTAINERS.joinToString(", ")
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Dcom.mojang.eula.agree=true", "-Dfile.encoding=UTF-8")
    }
}

val targetJavaVersion = 25
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
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
