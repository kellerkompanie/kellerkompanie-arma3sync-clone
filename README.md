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
java -jar keko-arma3sync.jar
```

## Development

```shell
# Build only
./gradlew build

# Run from source
./gradlew run
```
