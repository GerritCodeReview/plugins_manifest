//Copyright (C) 2015 The Android Open Source Project
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.googlesource.gerrit.plugins.manifest;

import com.googlesource.gerrit.plugins.manifest.extensions.RepoManifest;

import com.google.gerrit.common.errors.PermissionDeniedException;
import com.google.gerrit.reviewdb.client.Branch;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.NoSuchRefException;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.sshd.BaseCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.inject.Inject;

import org.apache.sshd.server.Environment;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandMetaData(name = "update", description = "Create/update a manifest")
public class UpdateCommand extends BaseCommand {
  public static class RevisionOperation implements ManifestOperation {
    public String projectName;
    public String revision;

    public RevisionOperation(String projectName, String revision) {
      this.projectName = projectName;
      this.revision = revision;
    }

    public void transform(ManifestUpdater manifestUpdater) throws Exception {
      for (ManifestUpdater.Project project: manifestUpdater.getProjects(
          RepoManifest.ATTR_PROJECT_NAME, projectName)) {
        project.setAttribute(RepoManifest.ATTR_PROJECT_REVISION, revision);
      }
    }
  }

  public static class ProjectRevisionOptionHandler
      extends TwoArgumentOptionHandler<RevisionOperation> {

    public ProjectRevisionOptionHandler(CmdLineParser parser, OptionDef option,
        Setter<? super RevisionOperation> setter) {
      super(parser, option, setter);
    }

    @Override
    protected RevisionOperation parse(String project, String revision)
        throws NumberFormatException, CmdLineException {
      return new RevisionOperation(project, revision);
    }
  }

  @Option(name = "--src-project", aliases = "-sp",
      usage = "source project containing the manifest")
  private ProjectControl srcProjectControl;

  @Option(name = "--src-branch", aliases = "-sb", metaVar = "BRANCH",
      usage = "source branch containing the manifest")
  private String srcBranch;

  @Option(name = "--src-file", aliases = "-sf", metaVar = "FILE",
      usage = "source file containing the manifest")
  private String srcFile;

  @Option(name = "--dst-project", aliases = "-dp",
      usage = "destination project to place the manifest in")
  private ProjectControl dstProjectControl;

  @Option(name = "--dst-branch", aliases = "-db", metaVar = "BRANCH",
      usage = "destination branch to place the manifest on")
  private String dstBranch;

  @Option(name = "--dst-file", aliases = "-df", metaVar = "FILE",
      usage = "destination file to place the manifest in")
  private String dstFile;

  @Option(name = "--project-revision", aliases = "-pr", metaVar = "PROJECT REVISION",
      handler = ProjectRevisionOptionHandler.class,
      usage = "revision to update the project in the manifest to, Ex:" +
          "\n\tkernel/msm refs/heads/msm-3.14")
  public void parseOperation(RevisionOperation revisionOperation) {
    operations.add(revisionOperation);
  }
  public List<ManifestOperation> operations = new ArrayList<>();

  @Option(name = "--commit-message", aliases = "-m", metaVar = "COMMIT-MESSAGE",
      usage = "commit message to use when updating the manifest")
  private String commitMessage;

  @Inject
  private ProjectControl.Factory projectControlFactory;

  @Inject
  private ManifestUpdater manifestUpdater;

  public static final String DEFAULT_COMMIT_MESSAGE = "Update manifest\n";

  private Branch.NameKey srcRef;
  private Branch.NameKey dstRef;

  public void start(final Environment env) {
    startThread(new CommandRunnable() {
      public void run() throws IOException, UnloggedFailure {
        parseCommandLine();
        processArgs();
        try {
          updateManifest() ;
        } catch (NoSuchRefException e) {
          throw new UnloggedFailure(1, e.getMessage());
        } catch (NoSuchProjectException | RepositoryNotFoundException e) {
          throw new UnloggedFailure(2, e.getMessage());
        } catch (PermissionDeniedException e) {
          throw new UnloggedFailure(20, e.getMessage());
        } catch (Exception e) {
          throw new UnloggedFailure(50, "Error processing manifest: " + e);
        }
      }
    });
  }

  private void updateManifest() throws Exception {
    manifestUpdater.read(srcRef, srcFile);

    for (ManifestOperation operation: operations) {
      operation.transform(manifestUpdater);
    }

    RevCommit commit = manifestUpdater.write(dstRef, dstFile,
        getCommitMessage());
    out.write(("Commit: " + ObjectId.toString(commit.getId()) + "\n").getBytes(ENC));
    out.flush();
  }

  private String getCommitMessage() {
    if (commitMessage == null) {
      return DEFAULT_COMMIT_MESSAGE;
    } else {
      return commitMessage;
    }
  }

  private void processArgs() throws UnloggedFailure {
    if (srcProjectControl == null && dstProjectControl == null) {
      String project = "platform/manifest";
      try {
        srcProjectControl = projectControlFactory.controlFor(
            new Project.NameKey(project));
      } catch (NoSuchProjectException e) {
        throw new UnloggedFailure("missing " + project);
      }
      dstProjectControl = srcProjectControl;
    } else if (srcProjectControl == null) {
      srcProjectControl = dstProjectControl;
    } else if (dstProjectControl == null) {
      dstProjectControl = srcProjectControl;
    }

    if (srcBranch == null && dstBranch == null) {
      srcBranch = "master";
      dstBranch = srcBranch;
    } else if (srcBranch == null) {
      srcBranch = dstBranch;
    } else if (dstBranch == null) {
      dstBranch = srcBranch;
    }
    srcBranch = RefNames.fullName(srcBranch);
    dstBranch = RefNames.fullName(dstBranch);

    if (srcFile == null && dstFile == null) {
      srcFile = "default.xml";
      dstFile = srcFile;
    } else if (srcFile == null) {
      srcFile = dstFile;
    } else if (dstFile == null) {
      dstFile = srcFile;
    }

    srcRef = getBranch(srcProjectControl, srcBranch);
    dstRef = getBranch(dstProjectControl, dstBranch);
  }

  private Branch.NameKey getBranch(ProjectControl ctl, String branch) {
    return new Branch.NameKey(ctl.getProject().getNameKey(), branch);
  }
}
