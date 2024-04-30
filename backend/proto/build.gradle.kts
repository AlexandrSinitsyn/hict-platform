import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.google.protobuf") version "0.9.4"
}

group = "ru.itmo.hict.proto"
version = "1.0.0"

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.build {
    dependsOn("generateProto")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-stub:1.63.0")
    implementation("io.grpc:grpc-protobuf:1.63.0")
    api("com.google.protobuf:protobuf-kotlin:3.25.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}
