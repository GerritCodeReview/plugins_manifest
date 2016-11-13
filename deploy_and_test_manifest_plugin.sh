#!/usr/bin/env bash

source "$(dirname "$0")/test/help_deploy.sh"

plugin=manifest
jar_glob="$(dirname "$0")/../../plugins/$plugin/target/$plugin*.jar"
matches_one_file "$jar_glob" || exit 1
jar=$(echo $jar_glob)
plugin="$plugin"__"$USER"

host=review-android-dev.quicinc.com
gerrit_dir=$(get_quic_gerrit_dir)

# Main
deploy_plugin "$plugin" "$jar" "$host" "$gerrit_dir" &&
    "$(dirname "$0")/test/test_manifest_plugin.sh" \
        "$plugin" "$host" "$gerrit_dir" || exit $?

echo "All test cases passed."
exit 0
