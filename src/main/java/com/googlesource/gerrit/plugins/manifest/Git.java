// Copyright (C) 2015 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.manifest;

import com.google.gerrit.reviewdb.client.Branch;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;

public class Git {
  public static ObjectId getCommit(GitRepositoryManager repos,
      Branch.NameKey branch) throws IOException {
    Repository repo = repos.openRepository(branch.getParentKey());
    try {
      return repo.getRef(branch.get()).getObjectId();
    } finally {
      repo.close();
    }
  }

  public static boolean mergedInto(GitRepositoryManager repos, String project,
      ObjectId haystack, ObjectId needle) throws IOException {
    Repository repo = repos.openRepository(Project.NameKey.parse(project));
    RevWalk walk = new RevWalk(repo);
    try {
      return walk.isMergedInto(walk.parseCommit(needle),
          walk.parseCommit(haystack));
    } finally {
      repo.close();
    }
  }
}
