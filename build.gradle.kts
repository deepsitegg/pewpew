import java.util.concurrent.TimeUnit

plugins {
    java
    `maven-publish`
    id("io.freefair.lombok") version "8.13.1" apply false
    id("com.gradleup.shadow") version "9.0.2" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false
}

val targetJavaVersion = 21

allprojects {
    group = "gg.deepsite"
    version = "26.1.2-dev"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "io.freefair.lombok")

    configure<io.freefair.gradle.plugins.lombok.LombokExtension> {
        version = "1.18.46"
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.deepsite.gg/releases")
        maven("https://maven.deepsite.gg/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.skriptlang.org/releases")
        maven("https://maven.enginehub.org/repo/")
        maven("https://jitpack.io")
    }

    // Our own SNAPSHOTs change often — don't let Gradle cache them for 24h
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(5, TimeUnit.MINUTES)
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

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
        options.release.set(targetJavaVersion)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    publishing {
        repositories {
            maven {
                name = "deepsite"
                url = uri(
                    if (project.version.toString().let { it.endsWith("SNAPSHOT") || it.endsWith("-dev") })
                        "https://maven.deepsite.gg/snapshots"
                    else
                        "https://maven.deepsite.gg/releases"
                )
                credentials {
                    username = System.getenv("DEEPSITE_MAVEN_NAME")
                        ?: project.findProperty("deepsiteUsername") as String?
                    password = System.getenv("DEEPSITE_MAVEN_SECRET")
                        ?: project.findProperty("deepsitePassword") as String?
                }
                // Reposilite expects preemptive basic auth
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
