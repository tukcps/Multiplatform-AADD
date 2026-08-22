rootProject.name = "aadd"

val aaddDirectory = providers
    .gradleProperty("aaddDirectory")
    .orNull

if (aaddDirectory != null) {
    println("Found configured aadd project")
    includeBuild(aaddDirectory)
}