#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
IMAGE_NAME="keko-arma3sync-builder"

cd "$PROJECT_DIR"

echo "Building Docker image..."
docker build -t "$IMAGE_NAME" -f docker/Dockerfile .

echo ""
echo "Building Debian package..."
mkdir -p "$PROJECT_DIR/dist"
docker run --rm \
    -v "$PROJECT_DIR:/build" \
    -v "$PROJECT_DIR/dist:/dist" \
    -w /build \
    "$IMAGE_NAME" \
    sh -c "dpkg-buildpackage -us -uc -b && cp /*.deb /dist/ && dh clean"

DEB_FILE=$(find "$PROJECT_DIR/dist" -maxdepth 1 -name "keko-arma3sync_*.deb" -type f 2>/dev/null | head -1)

if [ -n "$DEB_FILE" ]; then
    echo ""
    echo "Build successful!"
    echo "Package: $DEB_FILE"
    echo ""
    echo "Install with: sudo dpkg -i $DEB_FILE"
else
    echo ""
    echo "Build failed - .deb file not found"
    exit 1
fi
