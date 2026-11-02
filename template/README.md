# NexFusion patch bundle template

A standalone project that builds a [NexFusion](https://github.com/a092devs/NexFusion) patch bundle (`.nxb`).

1. Copy this directory into a new repository.
2. Change `namespace`/`applicationId` (the bundle id), `nexfusionBundle` and `res/values/strings.xml` in
   [bundle/](bundle).
3. Write patches, see the [development guide](https://github.com/a092devs/NexFusion/blob/main/DEVELOPMENT.md#writing-a-patch-bundle).
4. `./gradlew :bundle:assembleBundle` builds `bundle/build/outputs/bundle/bundle-<version>.nxb`. Install it in NexFusion
   with *Import bundle from file*.
5. Push a tag: the [release workflow](.github/workflows/release.yml) attaches the bundle to a GitHub release. Users add
   your repository (`owner/repo`) as a patch source in NexFusion.

`buildSrc/` holds a copy of the `nexfusion.bundle` plugin from NexFusion's `build-logic/`, and `gradle.properties` the
patcher version to compile against. Update both when targeting a newer NexFusion.
