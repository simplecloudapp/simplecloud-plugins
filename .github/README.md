![Banner][banner]

<div align="center">

[![Modrinth][badge-modrinth]][modrinth]
[![License][badge-license]][license]
<br>

[![Discord][badge-discord]][social-discord]
[![Follow @simplecloudapp][badge-x]][social-x]
[![Follow @simplecloudapp][badge-bluesky]][social-bluesky]
[![Follow @simplecloudapp][badge-youtube]][social-youtube]
<br>

[Report a Bug][issue-bug-report]
·
[Request a Feature][issue-feature-request]
<br>

Give us a star, your support means the world to us!
</div>
<br>

> All information about our plugins can be found in our detailed [documentation][docs-plugins].

This repository contains all official SimpleCloud plugins.

## Repository structure

```
core/         shared foundation every plugin is built on
proxy/        proxy plugins
server/       server plugins
build-logic/  Gradle convention plugins for Kotlin, Spotless, Detekt and publishing
```

## Building

You need JDK 25. Clone the repository including its submodules and build it:

```shell
git clone --recurse-submodules https://github.com/simplecloudapp/simplecloud-plugins.git
cd simplecloud-plugins
./gradlew build
```

The plugin jars are collected in `build/plugins/<plugin>/` of the root project.

## Contributing

Contributions to SimpleCloud are welcome and highly appreciated. However, before you jump right into it, we would like
you to read our [Contribution Guide](CONTRIBUTING.md).

## License

This repository is licensed under [Apache 2.0][license].


<!-- LINK GROUP -->

[banner]: https://github.com/simplecloudapp/branding/blob/main/readme/banner/plugins.png?raw=true
[issue-bug-report]: https://github.com/simplecloudapp/simplecloud-plugins/issues/new?labels=bug&projects=template=01_BUG-REPORT.yml&title=%5BBUG%5D+%3Ctitle%3E
[issue-feature-request]: https://github.com/simplecloudapp/simplecloud-plugins/discussions/new?category=ideas
[docs-plugins]: https://simplecloud.app/docs/en/manual/plugin/overview

[modrinth]: https://modrinth.com/organization/simplecloud

[license]: https://opensource.org/licenses/Apache-2.0

[social-x]: https://x.com/simplecloudapp
[social-bluesky]: https://bsky.app/profile/simplecloud.app
[social-youtube]: https://www.youtube.com/@thesimplecloud9075
[social-discord]: https://discord.simplecloud.app

[badge-modrinth]: https://img.shields.io/badge/modrinth-18181b.svg?style=flat-square&logo=modrinth
[badge-license]: https://img.shields.io/badge/apache%202.0-blue.svg?style=flat-square&label=license&labelColor=18181b&style=flat-square&color=e11d48
[badge-discord]: https://img.shields.io/badge/Community_Discord-d95652.svg?style=flat-square&logo=discord&color=27272a
[badge-x]: https://img.shields.io/badge/Follow_@simplecloudapp-d95652.svg?style=flat-square&logo=x&color=27272a
[badge-bluesky]: https://img.shields.io/badge/Follow_@simplecloud.app-d95652.svg?style=flat-square&logo=bluesky&color=27272a
[badge-youtube]: https://img.shields.io/badge/youtube-d95652.svg?style=flat-square&logo=youtube&color=27272a
