# TaCZ: Leawind TPS Compat - NeoForge 1.21.1

This branch ports the original Forge 1.20.1 compatibility mod to NeoForge 1.21.1.

## Target

- Minecraft: `1.21.1`
- NeoForge: `21.1.145`
- Java: `21`
- TaCZ: `MUKSC/TACZ-1.21.1`, `1.1.7-hotfix-r5`
- Leawind's Third Person: `2.3.0`, NeoForge
- Architectury API: `13.0.8`
- modid: `levanilla_tacztps`

## Scope

This project is a client-side compatibility layer.

It does not modify TaCZ source code.
It does not modify Leawind source code.
It does not bundle TaCZ, Leawind's Third Person, or Architectury API inside the compat jar.

## Features

- Redirects TaCZ camera recoil to Leawind's third-person camera.
- Prevents TaCZ recoil from directly mutating the player rotation while Leawind third-person rendering is active.
- Allows TaCZ crosshair rendering to respect Leawind's third-person crosshair state.
- Supports config-driven first-person transition while aiming or scoping.
- Guards TaCZ draw logic against a null `Minecraft#hitResult`.

## Required runtime mods

Install these separately:

- TaCZ `1.1.7-hotfix-r5` for NeoForge 1.21.1
- Leawind's Third Person `2.3.0` for NeoForge 1.21.1
- Architectury API `13.0.8` for NeoForge 1.21.1

## Development local jars

Local development requires these repo-relative files:

```text
libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar
libs/architectury-api-13.0.8-neoforge.jar
```

These jars are ignored by Git and must not be committed.

## Config

Config file:

```text
config/tac-leawindtps.toml
```

Options:

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

Behavior:

- `switch_first_person_aiming=true` forces Leawind first-person transition while aiming any TaCZ gun.
- `switch_first_person_scoping=true` forces Leawind first-person transition while aiming a TaCZ gun with an external or built-in scope.
- `switch_first_person_aiming` takes precedence over `switch_first_person_scoping`.

## License

This project is a fork of `leva-nilla/TACZ-LeawindTPS-Compat`.

The original project is licensed under the GNU General Public License v3.0.
This fork remains licensed under `GPL-3.0-only`.

When distributing a compiled jar, the corresponding source code for that jar must also be available.
Do not add extra restrictions such as prohibiting modification, redistribution, or commercial use.
