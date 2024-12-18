val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
}

group = "com.snowykte0426"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor 서버 핵심 의존성
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    // Oshi 라이브러리
    implementation("com.github.oshi:oshi-core:6.6.5")
    // Logback 로깅
    implementation("ch.qos.logback:logback-classic:$logback_version")
    // Yaml 설정 파일 읽기
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    // HTML 빌더
    implementation("io.ktor:ktor-server-html-builder-jvm")
    // Test 의존성
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}