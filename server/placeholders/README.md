# Placeholders Plugin

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

🌟 Give us a star — your support means the world to us!
</div>
<br>

> All information about this project can be found in our detailed [documentation][docs-thisproject].

The placeholder plugin allows you to use all important parameters of the Cloud API as placeholders with the help of [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) or [MiniPlaceholders](https://github.com/MiniPlaceholders/MiniPlaceholders). This allows you to retrieve all information about a server or group as a placeholder.

The plugin supports Paper, Purpur, and Folia. Folia installations require PlaceholderAPI 2.11.7 or newer.

## Targeting other resources

The existing placeholders continue to return information about the current server, group, or persistent server:

```text
%simplecloud_server_player_count%
%simplecloud_group_player_count%
%simplecloud_persistent_server_player_count%
```

You can target another resource by inserting its selector before the property:

```text
%simplecloud_group_<group-name>_<property>%
%simplecloud_persistent_server_<persistent-server-name>_<property>%
%simplecloud_server_<group-name>:<numerical-id>_<property>%
%simplecloud_server_id:<server-id>_<property>%
```

For example:

```text
%simplecloud_group_Lobby_player_count%
%simplecloud_persistent_server_MainLobby_is_active%
%simplecloud_server_Lobby:1_state%
%simplecloud_server_id:550e8400-e29b-41d4-a716-446655440000_player_count%
```

Group and persistent-server names may contain underscores. If the selected resource does not exist or the selector is invalid, the placeholder cannot be resolved.

## Contributing
Contributions to SimpleCloud are welcome and highly appreciated. However, before you jump right into it, we would like you to read our [Contribution Guide][docs-contribute].

## License
This repository is licensed under [Apache 2.0][license].


<!-- LINK GROUP -->

<!-- ✅ PLEASE EDIT -->
[banner]: https://raw.githubusercontent.com/simplecloudapp/branding/refs/heads/main/readme/banner/plugin/placeholders.png
[issue-bug-report]: https://github.com/simplecloudapp/simplecloud-plugins/issues/new?labels=bug&title=placeholders%3A+%3Ctitle%3E
[issue-feature-request]: https://github.com/simplecloudapp/simplecloud-plugins/issues/new?labels=enhancement&title=placeholders%3A+%3Ctitle%3E
[docs-thisproject]: https://simplecloud.app/docs/en/manual/plugin/placeholders
[docs-contribute]: https://github.com/simplecloudapp/simplecloud-plugins/blob/main/.github/CONTRIBUTING.md

[modrinth]: https://modrinth.com/plugin/placeholder-plugin

<!-- ⛔ DON'T TOUCH -->
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
