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

package com.googlesource.gerrit.plugins.manifest;

import com.google.gerrit.reviewdb.client.Branch;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.UUID;

public class TempRef implements AutoCloseable {
  public interface Factory {
    TempRef create(@Assisted Branch.NameKey branch);
  }

  private GitRepositoryManager repoManager;
  private Branch.NameKey tempRef;

  @Inject
  public TempRef(GitRepositoryManager repoManager,
                 @Assisted Branch.NameKey branch) throws IOException {
    this.repoManager = repoManager;

    Project.NameKey project = branch.getParentKey();
    tempRef = new Branch.NameKey(project, "refs/temp/" +
        UUID.randomUUID().toString());

    Repository repo = repoManager.openRepository(project);
    try {
      RefUpdate refUpdate = repo.updateRef(tempRef.get());
      refUpdate.setNewObjectId(repo.getRef(branch.get()).getObjectId());
      refUpdate.setForceUpdate(true);
      refUpdate.update();
    } finally {
      repo.close();
    }
  }

  public Branch.NameKey getBranch() {
    return tempRef;
  }

  public void close() throws IOException {
    Repository repo = repoManager.openRepository(tempRef.getParentKey());
    try {
      RefUpdate refUpdate = repo.updateRef(tempRef.get());
      refUpdate.setNewObjectId(ObjectId.zeroId());
      refUpdate.setForceUpdate(true);
      refUpdate.delete();
    } finally {
      tempRef = null;
      repo.close();
    }
  }
}
