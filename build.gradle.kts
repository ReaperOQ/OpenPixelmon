plugins {
    id("fabric-loom") version "0.9.+"
}

group = "me.marty"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()

    maven {
        name = "Geckolib Repository"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    }

    maven {
        name = "Devan-Kerman/Devan-Repo"
        url = uri("https://storage.googleapis.com/devan-maven/")
    }

    maven {
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }

    maven {
        url = uri("https://repo.repsy.io/mvn/fadookie/particleman")
    }

    maven {
        url = uri("https://maven.blamejared.com")
    }

    maven {
        name = "CurseMaven"
        url = uri("https://www.cursemaven.com/")
    }

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    minecraft("net.minecraft", "minecraft", "1.18_experimental-snapshot-7")
    mappings("net.fabricmc", "yarn", "1.18_experimental-snapshot-7+build.2", classifier = "v2")

    modImplementation("net.fabricmc", "fabric-loader", "0.11.6")
    modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.40.0+1.18_experimental")

    include(modImplementation("net.devtech", "arrp", "0.4.4"))
    include(modImplementation("software.bernie.geckolib", "geckolib-fabric-1.17", "3.0.10", classifier="dev"))

    include(modImplementation("io.github.onyxstudios.Cardinal-Components-API", "cardinal-components-base", "3.1.1"))
    include(modImplementation("io.github.onyxstudios.Cardinal-Components-API", "cardinal-components-entity", "3.1.1"))

    implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT")
}

base {
    archivesBaseName = "OpenPixelmon"
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

minecraft {
    accessWidener = file("src/main/resources/pixelmon.aw")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    options.release.set(16)
}

tasks.withType<AbstractArchiveTask> {
    from(file("LICENSE"))
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.remapJar {
    doLast {
        input.get().asFile.delete()
    }
}
