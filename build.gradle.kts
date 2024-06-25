plugins {
  java
  id("org.springframework.boot") version "3.2.7" // change to 3.3.1 to make it fail
  id("io.spring.dependency-management") version "1.1.5"
  id("io.freefair.lombok") version "8.6"
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("com.h2database:h2:2.2.224")
}

tasks.test {
  useJUnitPlatform()
}
