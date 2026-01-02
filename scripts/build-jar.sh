#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "Building fat JAR..."
./gradlew customFatJar

BUILD_JAR="$PROJECT_DIR/build/libs/ArmA3Sync-keko-1.0.jar"
OUTPUT_JAR="$PROJECT_DIR/ArmA3Sync-keko.jar"

if [ -f "$BUILD_JAR" ]; then
    cp "$BUILD_JAR" "$OUTPUT_JAR"
    echo ""
    echo "Build successful!"
    echo "JAR location: $OUTPUT_JAR"
    echo ""
    echo "Run with: java -jar $OUTPUT_JAR"
else
    echo "Build failed - JAR not found"
    exit 1
fi
