# What this repository is

This repository contains all official SimpleCloud plugins:

1. Core: the shared foundation every plugin is built on, with one module per platform.
2. Proxy plugins: command, notify, proxy-essentials and server-connection.
3. Server plugins: npcs, placeholders, prefixes and signs.
4. Build logic: Gradle convention plugins for Kotlin, Spotless, Detekt and publishing.

Every plugin is split into a `-shared` module containing the platform independent logic and one module per platform
(`-velocity`, `-bungeecord`, `-paper`, ...) containing only the platform specific code. Most changes belong into the
`-shared` module. Some plugins also have an `-api` module, changes to it must stay binary compatible.

# Contributing

We love contributions! If you found a bug or have an idea for a feature, feel free to [open an issue][issues] or talk to
us on our [Discord][discord].

Small bug fixes can be submitted as a pull request right away. If you want to work on a new feature or a bigger change,
please discuss it with us first. This way we can agree on the approach early and you don't spend time on something we
can't merge.

If something you want to add is useful for more than one plugin, it probably belongs into `core`. Please talk to us
before changing it, since every plugin depends on it.

## Getting started

You need JDK 25 and an IDE with Kotlin support, we recommend [IntelliJ IDEA][intellij].

Fork the repository and clone your fork including its submodules:

```shell
git clone --recurse-submodules https://github.com/<your-username>/simplecloud-plugins.git
```

If you already cloned it without submodules, run `git submodule update --init --recursive`.

To build everything, run `./gradlew build`. To build a single plugin, run the `shadowJar` task of its module, for
example:

```shell
./gradlew :proxy:notify:notify-velocity:shadowJar
```

The plugin jars are collected in `build/plugins/<plugin>/` of the root project.

## Code style

Follow the [Kotlin coding conventions][kotlin-conventions] and stick to the style of the surrounding code.

We use [Spotless][spotless] to keep the formatting consistent. Always run it before committing:

```shell
./gradlew spotlessApply
```

We also use [Detekt][detekt] for static analysis. You don't have to run it on the whole project, but please run it on
the modules you changed and fix the findings your change introduced:

```shell
./gradlew :proxy:notify:notify-shared:detektMain
```

Our CI runs `./gradlew check` on every pull request, which includes both Spotless and Detekt. Running it locally before
pushing saves you a round trip.

## Submitting pull requests

Fork the project, create a branch for your change and open a pull request against `main`.

Keep your pull request focused on one thing. If you fixed a bug in notify and another one in signs, open two pull
requests instead of one. The same applies to unrelated refactorings or formatting changes, they belong into their own
pull request. Focused pull requests are easier to review, test and, if needed, revert.

In the description, explain what you changed and why, how you tested it and link the related issue if there is one.

Once your pull request is ready and CI passes, request a review from the maintainer team. Please address feedback by
pushing new commits, this makes it easier to see what changed since the last review.

## Checklist

Before submitting the pull request, make sure that you can say "yes" to each point in this checklist:

- Your pull request only contains changes related to one topic
- You can explain the changes made in the pull request
- You ran `./gradlew spotlessApply`
- You ran Detekt on the modules you changed and fixed new findings
- You built the plugin locally and tested your change on at least one supported platform
- You requested a review from the maintainer team

[issues]: https://github.com/simplecloudapp/simplecloud-plugins/issues
[discord]: https://discord.simplecloud.app
[intellij]: https://www.jetbrains.com/idea/
[spotless]: https://github.com/diffplug/spotless
[detekt]: https://detekt.dev
[kotlin-conventions]: https://kotlinlang.org/docs/coding-conventions.html
