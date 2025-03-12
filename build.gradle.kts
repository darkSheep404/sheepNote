import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.7.0"
}

group = "com.darksheep"
version = "2025-03-12"

repositories {
    /*上述代码将阿里云Maven仓库设置为优先级最高，
    从而加快Gradle的构建速度。请注意，通常只需将 "public" 仓库添加到 repositories 中，
    因为这个仓库会根据请求自动代理其他仓库（例如 jcenter、google 等）。
    但是，为了确保更好的兼容性和分级，您也可以用上述配置添加所有阿里云Maven仓库*/
    mavenLocal()
    mavenCentral()
    maven {
        setUrl("https://maven.aliyun.com/repository/public")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/jcenter")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/google")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/spring")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/spring-plugin")
    }
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.21.0.1")
   /* implementation("com.intellij:psi:2021.3.0")
    implementation("com.intellij:java-psi:2021.3.0")*/
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3")
    type.set("IC") // Target IDE Platform
    //这里应用了java插件 java语言类型才不会报错
    plugins.set(listOf("java"))
    //com.intellij.java
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    systemProperty("file.encoding", "UTF-8")
}
