#!/bin/sh
# Brings a Modrinth App instance up to date with the latest main before each launch.
# Set as the instance's pre-launch hook: /bin/sh /path/to/fallow-update.sh
# The Modrinth App runs hooks from the instance folder. See docs/dev-sync.md.

PACK_URL="${FALLOW_PACK_URL:-https://salvagedisc.github.io/fallow/pack.toml}"
HERE=$(cd "$(dirname "$0")" && pwd)
BOOTSTRAP="$HERE/packwiz-installer-bootstrap.jar"
INSTANCE="${INST_DIR:-$PWD}"

case "$INSTANCE" in
  */profiles/*|*/instances/*) ;;
  *) echo "fallow-update: $INSTANCE is not a launcher instance, nothing done" >&2; exit 0 ;;
esac

# Java: the one the launcher picked for this instance, else the newest Java the Modrinth App downloaded.
JAVA="${INST_JAVA:-}"
if [ -z "$JAVA" ] || [ ! -x "$JAVA" ]; then
  JAVA=$(find "$HOME/Library/Application Support/ModrinthApp/meta/java_versions" \
              "$HOME/Library/Application Support/com.modrinth.theseus/meta/java_versions" \
              "$HOME/.local/share/ModrinthApp/meta/java_versions" \
              -path '*/bin/java' -type f 2>/dev/null | sort | tail -n 1)
fi
if [ -z "$JAVA" ]; then
  echo "fallow-update: no Java found, launching without update" >&2
  exit 0
fi

if [ ! -f "$BOOTSTRAP" ]; then
  curl -fsSL -o "$BOOTSTRAP.part" \
    https://github.com/packwiz/packwiz-installer-bootstrap/releases/latest/download/packwiz-installer-bootstrap.jar \
    && mv "$BOOTSTRAP.part" "$BOOTSTRAP"
fi

cd "$INSTANCE" || exit 0
# A failed update (offline, server down) must not block the game: the installed version is kept.
"$JAVA" -jar "$BOOTSTRAP" -s client "$PACK_URL" \
  || echo "fallow-update: update failed, launching the installed version" >&2
exit 0
