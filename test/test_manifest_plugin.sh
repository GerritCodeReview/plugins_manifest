#!/usr/bin/env bash

source "$(dirname $0)/help_deploy.sh"

plugin=$1; shift
host=$1; shift
gerrit_dir=$1; shift
port=$(get_ssh_port "$host" "$gerrit_dir")

manifest_list_empty='[]'
manifest_list_LNX_LE_5_3=' [ { "kind": "gerritcodereview#remotemanifest", "cgsn": "ReviewAndroid", "project": "mdm/manifest", "branch": "LNX.LE.5.3", "file": "default.xml" } ]'
manifest_list_TIZEN_0_3=' [ { "kind": "gerritcodereview#remotemanifest", "cgsn": "ReviewAndroid", "project": "tizen/manifest", "branch": "tizen_0.3", "file": "default.xml" } ]'

test_usage() {
    local children=
    for command in ls-manifest ls-si update; do
        ssh -p $port $host $plugin --help 2>&1 |
            grep --quiet "$command" &
        children+=" $!"
    done
    wait_on $children
}

test_ls_manifests() {
    local children=
    test_ls_manifests__performance &
    children+=" $!"
    test_ls_manifests__branch &
    children+=" $!"
    test_ls_manifests__hasproject &
    children+=" $!"
    test_ls_manifests__hascomponentrevision &
    children+=" $!"
    test_ls_manifests__hascomponentrevisionhistory &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__performance() {
    echo "first query time (above): $(\
        time ssh -p $port $host $plugin ls-manifests branch:DONT_MATCH)" &&
        echo "second query time (above): $(\
            time ssh -p $port $host $plugin ls-manifests branch:DONT_MATCH)"
}

test_ls_manifests__branch() {
    local a=$(mktemp)
    local b=$(mktemp)
    local x=$(mktemp)
    local y=$(mktemp)
    ssh -p $port $host $plugin ls-manifests branch:LNX.LE.5.3 |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b" &&
      ssh -p $port $host $plugin ls-manifests branch:DNS |
            sortize_json > "$x" &&
      echo "$manifest_list_empty" |
            sortize_json > "$y" && diff "$x" "$y"
    rval=$?
    rm "$a" "$b" "$x" "$y"
    return $rval
}

test_ls_manifests__hasproject() {
    local children=
    test_ls_manifests__hasproject__name_and_rev &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_revisionequals &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_revisionhistory &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_xship_and_xquicdist &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__hasproject__name_and_rev() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:kernel/msm-3.10 revision:tizen_0.3\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_TIZEN_0_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hasproject__name_and_revisionequals() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:platform/vendor/qcom-proprietary/wtf/1 revisionequals:9d66b4e9e5cd82174f3e51134048867063c7a35a\" AND hasproject:\"name:platform/vendor/qcom-proprietary/wtf/2 revisionequals:7b51568eff18afa4d0cda64072a2ee52f1405183\" AND hasproject:\"name:platform/vendor/qcom-proprietary/wtf/3 revisionequals:aec0ede05238b3204ded16f4b4d7057c0e138978\" AND hasproject:\"name:platform/vendor/qcom-proprietary/wtf/4 revisionequals:9eb53b7e477f07a59cd703078e687b6abacee7cb\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hasproject__name_and_revisionhistory() {
    local children=
    test_ls_manifests__hasproject__name_and_revisionhistory__equals &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_revisionhistory__in_history &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_revisionhistory__not_in_history &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__hasproject__name_and_xship_and_xquicdist() {
    local children=
    test_ls_manifests__hasproject__name_and_xship_and_xquicdist_match &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_xship_and_xquicdist_nomatch &
    children+=" $!"
    test_ls_manifests__hasproject__name_and_xship_and_xquicdist_match_many &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__hasproject__name_and_xship_and_xquicdist_match() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:kernel/msm-3.10 revision:tizen_0.3 xship:oss xquicdist:la\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_TIZEN_0_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hasproject__name_and_xship_and_xquicdist_nomatch() {
    local x=$(mktemp)
    local y=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:kernel/msm-3.10 revision:tizen_0.3 xship:oss xquicdist:le\"' |
            sortize_json > "$x" &&
      echo "$manifest_list_empty" |
            sortize_json > "$y" && diff "$x" "$y"
    rval=$?
    rm "$x" "$y"
    return $rval
}

test_ls_manifests__hasproject__name_and_xship_and_xquicdist_match_many() {
    local x=$(mktemp)
    local y=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:kernel/msm-3.10 xship:oss xquicdist:la\"' |
            sortize_json > "$x" &&
      echo "$manifest_list_empty" |
            sortize_json > "$y" && diff "$x" "$y" > /dev/null
    if [ "$?" -eq "0" ]; then
        rval=1
    else
        rval=0
    fi
    rm "$x" "$y"
    return $rval
}

test_ls_manifests__hasproject__name_and_revisionhistory__equals() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:platform/vendor/qcom-proprietary/wtf/1 revisionhistory:9d66b4e9e5cd82174f3e51134048867063c7a35a\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hasproject__name_and_revisionhistory__in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:platform/vendor/qcom-proprietary/wtf/1 revisionhistory:8b5b95f987c94139692a88623f0d35d11ca78e77\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hasproject__name_and_revisionhistory__not_in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        'hasproject:\"name:platform/vendor/qcom-proprietary/wtf/1 revisionhistory:e89f63325b41793e6f1012accdcd6c7aea2e4d9a\"' |
            sortize_json > "$a" &&
      echo "$manifest_list_empty" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevision() {
    local children=
    test_ls_manifests__hascomponentrevision__equals &
    children+=" $!"
    test_ls_manifests__hascomponentrevision__in_history &
    children+=" $!"
    test_ls_manifests__hascomponentrevision__not_in_history &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__hascomponentrevision__equals() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevision:pcm.gerrit.2.0-383 |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevision__in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevision:pcm.gerrit.2.0-374 |
            sortize_json > "$a" &&
      echo "$manifest_list_empty" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevision__not_in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevision:pcm.gerrit.2.0-375 |
            sortize_json > "$a" &&
      echo "$manifest_list_empty" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevisionhistory() {
    local children=
    test_ls_manifests__hascomponentrevisionhistory__equals &
    children+=" $!"
    test_ls_manifests__hascomponentrevisionhistory__in_history &
    children+=" $!"
    test_ls_manifests__hascomponentrevisionhistory__not_in_history &
    children+=" $!"
    wait_on $children
}

test_ls_manifests__hascomponentrevisionhistory__equals() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevisionhistory:pcm.gerrit.2.0-383 |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevisionhistory__in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevisionhistory:pcm.gerrit.2.0-374 |
            sortize_json > "$a" &&
      echo "$manifest_list_LNX_LE_5_3" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

test_ls_manifests__hascomponentrevisionhistory__not_in_history() {
    local a=$(mktemp)
    local b=$(mktemp)
    ssh -p $port $host $plugin ls-manifests \
        branch:LNX.LE.5.3 AND hascomponentrevisionhistory:pcm.gerrit.2.0-375 |
            sortize_json > "$a" &&
      echo "$manifest_list_empty" |
            sortize_json > "$b" && diff "$a" "$b"
    rval=$?
    rm "$a" "$b"
    return $rval
}

sortize_json() {
    json_pp | sed -e 's|,$||' | sort
}

# NOTE: test_ls_manifests may need to be updated
#       depending on the component revision data in the env.
children=
test_usage &
children+=" $!"
test_ls_manifests &
children+=" $!"
wait_on $children
