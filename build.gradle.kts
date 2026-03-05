plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jreleaser") version "1.22.0"
    id("signing")
}

group = "io.github.hahadu"
version = "1.0.1"

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

// No dependencies required; this module only defines annotations.

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.github.hahadu"
            artifactId = "ktor-annotations"
            version = project.version.toString()
            pom {
                name.set("ktor-annotations")
                description.set("Annotations for Ktor controllers and OpenAPI")
                url.set("https://github.com/hahadu/apidoc-generate")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("hahadu")
                        name.set("hahadu")
                        email.set("you@example.com")
                    }
                }
                scm {
                    url.set("https://github.com/hahadu/ktor-apidoc-plugin")
                    connection.set("scm:git:https://github.com/hahadu/apidoc-generate.git")
                    developerConnection.set("scm:git:ssh://github.com/hahadu/apidoc-generate.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
        maven {
            name = "staging"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

configure<org.gradle.plugins.signing.SigningExtension> {
    val signingKey = (findProperty("signingKey") ?: "").toString()
    val signingPassword = (findProperty("signingPassword") ?: "").toString()
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

jreleaser {
    val ossrhUser = (findProperty("ossrhUsername") ?: "").toString()
    val ossrhPass = (findProperty("ossrhPassword") ?: "").toString()
    environment {
        if (ossrhUser.isNotBlank()) {
            properties.put("JRELEASER_MAVENCENTRAL_APP_USERNAME", ossrhUser)
        }
        if (ossrhPass.isNotBlank()) {
            properties.put("JRELEASER_MAVENCENTRAL_APP_PASSWORD", ossrhPass)
        }
    }
    project {
        name.set("ktor-annotations")
        description.set("Annotations for Ktor controllers and OpenAPI")
        license.set("Apache-2.0")
        authors.add("hahadu")
        links {
            homepage.set("https://github.com/hahadu/apidoc-generate")
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("app") {
                    setActive("RELEASE")
                    setStage("FULL")
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    if (ossrhUser.isNotBlank()) {
                        username.set(ossrhUser)
                    }
                    if (ossrhPass.isNotBlank()) {
                        password.set(ossrhPass)
                    }
                    sign.set(false)
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get())
                }
            }
        }
    }
}

tasks.register("publishToCentral") {
    group = "publishing"
    description = "Publish to Central Portal via JReleaser without SCM release"
    // Runs: publishMavenJavaPublicationToStagingRepository -> jreleaserDeploy
    dependsOn("publishMavenJavaPublicationToStagingRepository", "jreleaserDeploy")
}
