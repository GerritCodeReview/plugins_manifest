// Copyright (C) 2016 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.manifest.extensions;

import java.util.Map;

/** A very simple Map based representation of a repo manifest.
 *  Various attributes default values are populated per repo docs.
 *  See below for details.
 */
public interface RepoManifest {
  String NODE_MANIFEST = "manifest";
  String NODE_DEFAULT = "default";
  String NODE_PROJECT = "project";
  String NODE_REMOTE = "remote";
  String NODE_INCLUDE = "include";
  String NODE_REMOVE_PROJECT = "remove-project";

  String ATTR_REMOTE_NAME = "name";
  String ATTR_PROJECT_DEST_BRANCH = "dest-branch";
  String ATTR_PROJECT_NAME = "name";
  String ATTR_PROJECT_PATH = "path";
  String ATTR_PROJECT_REMOTE = "remote";
  String ATTR_PROJECT_REVISION = "revision";

  // NOTE: These are quic-specific extensions to the repo manifest data model.
  String ATTR_PROJECT_XSHIP = "x-ship";
  String ATTR_PROJECT_XQUICDIST = "x-quic-dist";


  Map<String, String> getDefaults();

  Map<String, Map<String, String>> getRemotes();

  Map<String, Map<String, String>> getProjects();

  /**
   * The following items require special handling (per repo docs) that is not
   * implemented because we do not currently use these items.
   *
   * <!ATTLIST project dest-branch CDATA #IMPLIED>
   * <!ATTLIST project groups      CDATA #IMPLIED>
   * <!ATTLIST project upstream CDATA #IMPLIED>
   *
   * The following items do not require special handling (per repo docs)
   * and we do not currently use these items.
   *
   * <!ATTLIST project sync-c      CDATA #IMPLIED>
   * <!ATTLIST project sync-s      CDATA #IMPLIED>
   * <!ATTLIST project clone-depth CDATA #IMPLIED>
   * <!ATTLIST project force-path CDATA #IMPLIED>
   * <!ELEMENT notice (#PCDATA)>
   * <!ATTLIST remote alias        CDATA #IMPLIED>
   * <!ATTLIST default sync-j      CDATA #IMPLIED>
   * <!ATTLIST default sync-c      CDATA #IMPLIED>
   * <!ATTLIST default sync-s      CDATA #IMPLIED>
   * <!ELEMENT manifest-server (EMPTY)>
   * <!ELEMENT repo-hooks (EMPTY)>
   * <!ATTLIST repo-hooks in-project CDATA #REQUIRED>
   * <!ATTLIST repo-hooks enabled-list CDATA #REQUIRED>
   *
   */
}
