#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

if [ -z "$1" ]; then
    echo "Usage: $0 <ssh-host>"
    echo "Example: $0 hostname"
    exit 1
fi

SSH_HOST="$1"

# Find the .deb file
DEB_FILE=$(find "$PROJECT_DIR/dist" -maxdepth 1 -name "keko-arma3sync_*.deb" -type f 2>/dev/null | head -1)

if [ -z "$DEB_FILE" ]; then
    echo "Error: No .deb file found in dist/"
    echo "Run ./scripts/build-deb.sh first"
    exit 1
fi

DEB_NAME=$(basename "$DEB_FILE")

echo "Deploying $DEB_NAME to $SSH_HOST..."

# Copy the .deb file to the target
echo "Copying package..."
scp "$DEB_FILE" "$SSH_HOST:/tmp/$DEB_NAME"

# Install the package
echo "Installing package..."
ssh "$SSH_HOST" "sudo dpkg -i /tmp/$DEB_NAME && rm /tmp/$DEB_NAME"

echo ""
echo "Deployment successful!"
echo "Run 'keko-arma3sync -buildall' on $SSH_HOST to build all repositories"
