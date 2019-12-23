rootProject.name = "drill-gradle-plugin"

buildCache {
    local {
        directory = rootDir.resolve("build-cache")
        removeUnusedEntriesAfterDays = 60
    }
}
