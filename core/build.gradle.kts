base {
    archivesName = "pewpew-core"
}

val adventureVersion = "4.17.0"
val configurateVersion = "4.1.2"

dependencies {
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")

    compileOnly("org.spongepowered:configurate-core:$configurateVersion")
    compileOnly("org.spongepowered:configurate-yaml:$configurateVersion")
    compileOnly("commons-io:commons-io:2.15.1")

    testImplementation("net.kyori:adventure-api:$adventureVersion")
    testImplementation("net.kyori:adventure-text-minimessage:$adventureVersion")
    testImplementation("org.spongepowered:configurate-core:$configurateVersion")
    testImplementation("org.spongepowered:configurate-yaml:$configurateVersion")
    testImplementation("commons-io:commons-io:2.15.1")
}

publishing {
    publications {
        create<MavenPublication>("core") {
            artifactId = "pewpew-core"
            from(components["java"])
        }
    }
}
