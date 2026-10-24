# Contributing guidelines

This repository contains all official SimpleCloud plugins:

- Core: common code used by all plugins.
- Proxy plugins: command, notify, proxy-essentials and server-connection.
- Server plugins: npcs, placeholders, prefixes and signs.
- Build logic: Gradle convention plugins for Kotlin, Spotless, Detekt and publishing.

We love contributions! If you have a question or want to discuss an idea first, feel free to talk to us on our
[Discord][discord].

## Submitting issues

If you found a bug or have an idea for a feature, [open an issue][issues].

- Keep every issue about one thing. If you found a bug in notify and another one in signs, open two issues.
- Mention the plugin your issue is about in the title, for example `notify`.
- For bugs, describe what you expected, what happened instead and how to reproduce it. Include the plugin version, the
  platform you are running and any relevant logs.

## Pull requests

Small bug fixes can be submitted as a pull request right away. If you want to work on a new feature or a bigger change,
please discuss it with us in an issue first. This way we can agree on the approach early and you don't spend time on
something we can't merge.

### Getting started

You need JDK 25 and an IDE with Kotlin support. We recommend [IntelliJ IDEA][intellij].

Fork the repository and clone your fork including its submodules:

```shell
git clone --recurse-submodules https://github.com/<your-username>/simplecloud-plugins.git
```

Run `./gradlew build` to build everything. The plugin jars are collected in `build/plugins/<plugin>/`.

### Code style

Follow the [Kotlin coding conventions][kotlin-conventions] and stick to the style of the surrounding code.

We use [Spotless][spotless] to keep the formatting consistent. Always run it before committing:

```shell
./gradlew spotlessApply
```

We also use [Detekt][detekt] for static analysis. Run it on the modules you changed and fix any new issues your change
introduces, for example:

```shell
./gradlew :proxy:notify:notify-shared:detektMain
```

Our CI runs `./gradlew check` on every pull request, which includes both Spotless and Detekt.

### Submitting

Create a branch for your change and open a pull request against `main`.

Keep your pull request focused on one thing. Unrelated fixes, refactorings or formatting changes belong in their own
pull request. Focused pull requests are easier to review, test and, if needed, revert.

In the description, explain what you changed and why, how you tested it and link the related issue if there is one.

Please address feedback by pushing new commits, as this makes it easier to see what changed since the last review.

### Checklist

Before submitting the pull request, make sure that you can say "yes" to each point:

- Your pull request only contains changes related to one topic
- You can explain the changes made in the pull request
- You ran `./gradlew spotlessApply`
- You ran Detekt on the modules you changed and fixed new findings
- You built the plugin locally and tested your change on at least one supported platform

## AI usage

Using AI tools is completely fine, as long as you understand the code you submit. Please write your issues and
pull request descriptions yourself.

[issues]: https://github.com/simplecloudapp/simplecloud-plugins/issues
[discord]: https://discord.simplecloud.app
[intellij]: https://www.jetbrains.com/idea/
[spotless]: https://github.com/diffplug/spotless
[detekt]: https://detekt.dev
[kotlin-conventions]: https://kotlinlang.org/docs/coding-conventions.html
