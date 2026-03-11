#!/usr/bin/env bash
#
# Tags and pushes a release based on the current SNAPSHOT version in gradle.properties.
#
# Usage: ./scripts/release.sh
#
# What it does:
#   1. Reads the version from gradle.properties (e.g. 0.3.0-SNAPSHOT)
#   2. Strips the -SNAPSHOT suffix to get the release version (0.3.0)
#   3. Checks out main, pulls latest
#   4. Creates a git tag (v0.3.0)
#   5. Pushes the tag to origin
#
# The tag push triggers the GitHub Actions release workflow, which handles
# publishing to Maven Central, creating the GitHub Release, deploying the
# web demo, and bumping to the next snapshot version.

set -euo pipefail

# Read version from gradle.properties
SNAPSHOT_VERSION=$(grep '^version=' gradle.properties | cut -d= -f2)

if [[ -z "$SNAPSHOT_VERSION" ]]; then
    echo "Error: Could not read version from gradle.properties" >&2
    exit 1
fi

if [[ "$SNAPSHOT_VERSION" != *-SNAPSHOT ]]; then
    echo "Error: Version '$SNAPSHOT_VERSION' is not a SNAPSHOT version." >&2
    echo "Expected something like '0.3.0-SNAPSHOT'." >&2
    exit 1
fi

RELEASE_VERSION="${SNAPSHOT_VERSION%-SNAPSHOT}"
TAG="v${RELEASE_VERSION}"

echo "Snapshot version: $SNAPSHOT_VERSION"
echo "Release version:  $RELEASE_VERSION"
echo "Tag:              $TAG"
echo ""

# Check for uncommitted changes
if ! git diff --quiet || ! git diff --staged --quiet; then
    echo "Error: Working tree has uncommitted changes. Commit or stash them first." >&2
    exit 1
fi

# Check that the tag doesn't already exist
if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Error: Tag '$TAG' already exists." >&2
    exit 1
fi

echo "Switching to main and pulling latest..."
git checkout main
git pull

echo ""
echo "Creating tag $TAG..."
git tag "$TAG"

echo "Pushing tag to origin..."
git push origin "$TAG"

echo ""
echo "Done! Tag $TAG pushed. The release workflow will take it from here."
echo "Monitor at: https://github.com/breischl/KENETh/actions/workflows/release.yml"
