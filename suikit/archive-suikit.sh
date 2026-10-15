#!/bin/bash
#
# Archives SuiKit.xcframework for a SwiftPM binary target. SwiftPM downloads the whole archive, so the
# debug symbols go into a separate SuiKit.dSYMs.zip.
#
# Usage: archive-suikit.sh <SuiKit.xcframework> <output directory>

set -euo pipefail

xcframework="$1"
out="$(mkdir -p "$2" && cd "$2" && pwd)"
stage="$out/staging"

rm -rf "$stage" "$out/SuiKit.xcframework.zip" "$out/SuiKit.dSYMs.zip"
mkdir -p "$stage/dSYMs"
ditto "$xcframework" "$stage/SuiKit.xcframework"

for slice in "$stage"/SuiKit.xcframework/*/; do
  if [ -d "${slice}dSYMs" ]; then
    ditto "${slice}dSYMs" "$stage/dSYMs/$(basename "$slice")"
    rm -rf "${slice}dSYMs"
  fi
done

plist="$stage/SuiKit.xcframework/Info.plist"
index=0
while /usr/libexec/PlistBuddy -c "Print :AvailableLibraries:$index" "$plist" > /dev/null 2>&1; do
  /usr/libexec/PlistBuddy -c "Delete :AvailableLibraries:$index:DebugSymbolsPath" "$plist" 2> /dev/null || true
  index=$((index + 1))
done

(cd "$stage" && ditto -c -k --sequesterRsrc --keepParent SuiKit.xcframework "$out/SuiKit.xcframework.zip")
(cd "$stage" && ditto -c -k --sequesterRsrc --keepParent dSYMs "$out/SuiKit.dSYMs.zip")
rm -rf "$stage"
