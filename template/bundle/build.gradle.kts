plugins {
    id("nexfusion.bundle")
}

android {
    // Also the bundle id. Pick your own, it must be unique.
    namespace = "com.example.patches"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.patches"
        minSdk = 27
        versionCode = 1
        versionName = "1.0.0"
    }
}

nexfusionBundle {
    entry = "com.example.patches.ExamplePatches"
    author = "Your name"
    descriptionResource = "bundle_description"
    // Unique among the bundles users install together; NexFusion's own bundles use 0x4b and 0x4c.
    resourcePackageId = 0x60
}

dependencies {
    // Provided by NexFusion at runtime: never `implementation`.
    compileOnly("io.github.nexfusion:patcher:${providers.gradleProperty("patcherVersion").get()}")
}
