# keko-arma3sync

A fork of ArmA3Sync including a command line option for building all repositories at once.

Original source (offline): http://www.sonsofexiled.fr/wiki/index.php/ArmA3Sync_Wiki_English

## Build

Build an all-in-one executable JAR:

```shell
# Linux/macOS
./scripts/build-jar.sh

# Windows
scripts\build-jar.bat
```

The JAR is output to `keko-arma3sync.jar` in the project root.

## Run

```shell
keko-arma3sync
```

## Command Line Interface

### Repository Commands

```shell
# Build a single repository
keko-arma3sync -build "RepositoryName"

# Build all repositories
keko-arma3sync -buildall

# Check repository synchronization
keko-arma3sync -check "RepositoryName"

# Synchronize with repository
keko-arma3sync -sync "RepositoryName" "/path/to/destination" true
```

### Modset Management

Modsets (also called "Events" internally) are named collections of addons that can be loaded together. Use the `-modset` command to manage them via CLI.

```shell
# Show modset help
keko-arma3sync -modset help
```

#### Data Storage

When running `keko-arma3sync` from a directory (e.g., `/var/lib/keko-a3mm/arma3sync`), data is stored in two locations:

**Repository configuration** is stored relative to the current working directory:
```
./resources/ftp/<RepoName>.a3s.repository
```

**Modsets/Events** are stored inside the repository's configured path:
```
<repository.path>/.a3s/events
```

Example structure when running from `/var/lib/keko-a3mm/arma3sync` with repository path `/var/www/arma3/mods`:

```
/var/lib/keko-a3mm/arma3sync/          # Working directory
└── resources/
    └── ftp/
        └── myrepo.a3s.repository      # Repository config (encrypted)

/var/www/arma3/mods/                   # Repository path (where mods are hosted)
├── @ace/
├── @cba_a3/
└── .a3s/
    ├── sync                           # File tree with checksums
    ├── serverinfo                     # Server metadata
    └── events                         # Modset definitions
```

**Note:** Modset commands require the repository to have a local path configured. If you see the error "Repository has no local path configured", you need to set the repository's main folder path first (via the GUI or `-console` mode).

#### List modsets

```shell
keko-arma3sync -modset list "RepositoryName"
```

Output:
```
Modsets in repository "RepositoryName":
  1. MainEvent
  2. TrainingEvent
```

#### Show modset details

```shell
keko-arma3sync -modset show "RepositoryName" "ModsetName"
```

Output:
```
Modset: MainEvent
Description: Weekly main event modset
Repository: RepositoryName
Addons (3):
  - @ace (required)
  - @cba_a3 (required)
  - @tfar (optional)
```

#### Create a new modset

```shell
keko-arma3sync -modset create "RepositoryName" "NewModsetName"
```

#### Delete a modset

```shell
keko-arma3sync -modset delete "RepositoryName" "ModsetName"
```

#### Rename a modset

```shell
keko-arma3sync -modset rename "RepositoryName" "OldName" "NewName"
```

#### Set modset description

```shell
keko-arma3sync -modset set-description "RepositoryName" "ModsetName" "Description text"
```

#### Add addon to modset

```shell
# Add as required addon
keko-arma3sync -modset add-addon "RepositoryName" "ModsetName" "@addon_name"

# Add as optional addon
keko-arma3sync -modset add-addon "RepositoryName" "ModsetName" "@addon_name" optional
```

#### Remove addon from modset

```shell
keko-arma3sync -modset remove-addon "RepositoryName" "ModsetName" "@addon_name"
```

#### List available addons in repository

```shell
keko-arma3sync -modset list-addons "RepositoryName"
```

Output:
```
Available addons in repository "RepositoryName" (5):
  @ace
  @cba_a3
  @cup_terrains
  @cup_units
  @tfar
```

## Development

```shell
# Build only
./gradlew build

# Run from source
./gradlew run
```

## Testing

Run unit tests:

```shell
./gradlew test
```

View test report in browser:

```shell
open build/reports/tests/test/index.html   # macOS
xdg-open build/reports/tests/test/index.html  # Linux
start build/reports/tests/test/index.html  # Windows
```

Run tests with verbose output:

```shell
./gradlew test --info
```
