plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.secureoverlay"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.bouncycastle:bcprov-jdk18on:1.86")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.86")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
