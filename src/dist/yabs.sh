#!/bin/sh
# Starts YaBS. Needs Java 21 or newer (java on the PATH or JAVA_HOME set).
cd "$(dirname "$0")" || exit 1
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi
if ! "$JAVA" -version >/dev/null 2>&1; then
    echo "Java not found. Please install Java 21 or newer (e.g. https://adoptium.net)." >&2
    exit 1
fi
VERSION=$("$JAVA" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n 1)
if [ "${VERSION:-0}" -lt 21 ]; then
    echo "YaBS needs Java 21 or newer, found Java ${VERSION:-unknown} (e.g. https://adoptium.net)." >&2
    exit 1
fi
exec "$JAVA" -jar openyabs.jar "$@"
