plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())

    pom {
        name.set("Compose Drag and Drop")
        description.set("A library that allows you to easily add drag and drop functionality to your Jetpack Compose or Compose Multiplatform projects.")
        url.set("https://github.com/MohamedRejeb/compose-dnd")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        developers {
            developer {
                id.set("MohamedRejeb")
                name.set("Mohamed Rejeb")
                email.set("mohamedrejeb445@gmail.com")
            }
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/MohamedRejeb/compose-dnd/issues")
        }
        scm {
            connection.set("https://github.com/MohamedRejeb/compose-dnd.git")
            url.set("https://github.com/MohamedRejeb/compose-dnd")
        }
    }
}
