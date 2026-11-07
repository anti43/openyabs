#!/usr/bin/env bash
# Builds an installer with its own Java runtime (jpackage, Java 21) from the
# distribution zip (./gradlew distributionZip).
#
#   src/packaging/build-installer.sh <deb|rpm|msi> <version>
#
# version: numbers only, e.g. 1.3.2 (MSI: each part at most 255.255.65535).
# Needs: deb - dpkg-deb, fakeroot; rpm - rpmbuild; msi - WiX Toolset 3 on the PATH.
# Result: build/installer/out/
set -euo pipefail

type="${1:?type: deb, rpm or msi}"
version="${2:?version, e.g. 1.3.2}"
root="$(cd "$(dirname "$0")/../.." && pwd)"
work="$root/build/installer"

zip=$(ls "$root"/build/distributions/openyabs-*.zip | head -n 1)
rm -rf "$work"
mkdir -p "$work/unzipped" "$work/input" "$work/out"
(cd "$work/unzipped" && jar xf "$zip")
# the application: openyabs.jar and the libraries of its manifest (lib/*.jar, as
# with "java -jar"). jpackage puts every jar of the input on the class path, so
# the old jars in subfolders of lib/ (ooo/, sync/) must stay out: they would
# shadow newer versions (e.g. slf4j)
mkdir -p "$work/input/lib"
cp "$work/unzipped/openyabs/openyabs.jar" "$work/input/"
cp "$work/unzipped/openyabs/lib/"*.jar "$work/input/lib/"

common=(
  --type "$type"
  --name YaBS
  --app-version "$version"
  --vendor "openyabs"
  --description "YaBS - quotes, invoices, e-invoices, customers and products. All data stays on your computer."
  --copyright "GNU General Public License v3"
  --license-file "$root/src/main/resources/license/gpl-3"
  --input "$work/input"
  --main-jar openyabs.jar
  --main-class mpv5.Main
  # as "java -jar" with the Add-Opens of the manifest
  --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED"
  --dest "$work/out"
)

case "$type" in
  deb|rpm)
    extra=(
      --icon "$root/src/main/resources/images/icon2_128.png"
      --linux-package-name yabs
      --linux-shortcut
      --linux-menu-group "Office;Finance"
      --linux-app-category office
    )
    if [ "$type" = deb ]; then
      extra+=(--linux-deb-maintainer "openyabs@googlegroups.com")
    else
      extra+=(--linux-rpm-license-type "GPLv3")
    fi
    ;;
  msi)
    extra=(
      --icon "$root/src/main/resources/images/icon.ico"
      --win-menu
      --win-menu-group YaBS
      --win-shortcut
      --win-shortcut-prompt
      --win-dir-chooser
      # fixed: a newer MSI replaces an installed older version
      --win-upgrade-uuid "6f1d3a52-9c0e-4b7a-8f3e-2d5b9a7c4e11"
    )
    ;;
  *)
    echo "unknown type: $type (deb, rpm or msi)" >&2
    exit 1
    ;;
esac

jpackage "${common[@]}" "${extra[@]}"
ls -l "$work/out"
