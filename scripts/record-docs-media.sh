#!/usr/bin/env bash
# Records the demo clips of the README and the docs from the sample app.
#
# The clips are scripted in sample/common/src/desktopTest/kotlin/recordings and
# rendered off screen, so the result is the same on every run. Needs ffmpeg.
#
#   scripts/record-docs-media.sh            # every clip
#   scripts/record-docs-media.sh reorder    # only the named clips (comma separated)
set -euo pipefail

cd "$(dirname "$0")/.."

only="${1:-}"
raw="$(mktemp -d)"
trap 'rm -rf "$raw"' EXIT

gradle_args=(":sample:common:desktopTest" --tests "recordings.*" "-PrecordDir=$raw")
if [[ -n "$only" ]]; then gradle_args+=("-PrecordOnly=$only"); fi
./gradlew "${gradle_args[@]}"

# Width of the GIF of a clip: the wide Kanban clips lead the README
gif_width() {
    case "$1" in
        kanban | kanban-light) echo 880 ;;
        *) echo 280 ;;
    esac
}

mkdir -p docs/videos docs/images/demos
for clip in "$raw"/*.mp4; do
    name="$(basename "$clip" .mp4)"
    cp "$clip" "docs/videos/$name.mp4"
    ffmpeg -y -loglevel error -i "$clip" \
        -vf "fps=15,scale=$(gif_width "$name"):-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128:stats_mode=diff[p];[b][p]paletteuse=dither=bayer:bayer_scale=5:diff_mode=rectangle" \
        "docs/images/demos/$name.gif"
    echo "$name: $(du -h "docs/videos/$name.mp4" | cut -f1) mp4, $(du -h "docs/images/demos/$name.gif" | cut -f1) gif"
done

# Window-sized stills: the home screen and the board with a card in hand
for still in "$raw"/*.png; do
    [[ -e "$still" ]] || continue
    cp "$still" "docs/images/$(basename "$still")"
    echo "$(basename "$still"): $(du -h "docs/images/$(basename "$still")" | cut -f1)"
done
