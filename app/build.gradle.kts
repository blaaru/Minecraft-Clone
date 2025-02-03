plugins {
    application
    java
}

repositories {
    mavenCentral() // Maven Central for LWJGL 3
}

dependencies {
    // LWJGL 3 dependencies
    implementation("org.lwjgl:lwjgl:3.3.6")
    implementation("org.lwjgl:lwjgl-glfw:3.3.6")
    implementation("org.lwjgl:lwjgl-opengl:3.3.6")
    implementation("org.lwjgl:lwjgl-stb:3.3.6")
    implementation("org.lwjgl:lwjgl-assimp:3.3.6")
    implementation("org.joml:joml:1.10.7") // JOML

    // Native bindings (Windows)
    runtimeOnly("org.lwjgl:lwjgl:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-stb:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-assimp:3.3.6:natives-windows")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("minecraft.clone.App")
}