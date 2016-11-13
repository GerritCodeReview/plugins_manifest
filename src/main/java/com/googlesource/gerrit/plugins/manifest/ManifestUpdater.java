//Copyright (C) 2016 The Android Open Source Project
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

import com.google.gerrit.common.errors.PermissionDeniedException;
import com.google.gerrit.reviewdb.client.Branch;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.NoSuchRefException;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.manifest.extensions.RepoManifest;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

public class ManifestUpdater {
  public class Project {
    private Element element;

    public Project(Element element) {
      this.element = element;
    }

    public String getAttribute(String attname) {
      switch (attname) {
        case RepoManifest.ATTR_PROJECT_NAME:
          String name = element.getAttributeValue(RepoManifest.ATTR_PROJECT_NAME);
          if (name == null) {
            name = element.getAttributeValue(RepoManifest.ATTR_PROJECT_PATH);
          }
          return name;
        case RepoManifest.ATTR_PROJECT_REVISION:
          String revision = element.getAttributeValue(RepoManifest.ATTR_PROJECT_REVISION);
          if (revision == null) {
            revision = getDefault(RepoManifest.ATTR_PROJECT_REVISION);
          }
          return revision;
        default:
          return element.getAttributeValue(attname);
      }
    }

    public void setAttribute(String attname, String value) {
      element.setAttribute(attname, value);
    }

    private String getDefault(String attname) {
      Document doc = manifestXml.getDocument();
      XPathFactory xFactory = XPathFactory.instance();
      String xPathStr = "/manifest/default/@" + attname;

      XPathExpression<Attribute> attribXPathExpr = xFactory.compile(xPathStr,
          Filters.attribute());
      try {
        return attribXPathExpr.evaluate(doc).get(0).getValue();
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }
  }

//   protected CreateChange.Factory ccFactory;
  protected ManifestXml manifestXml;
  protected GitFile manifestFile;
  protected GitFile.Factory gitFileFactory;
  protected ProjectControl.Factory pctlFactory;
  protected GitRepositoryManager repos;
  protected TempRef.Factory tmpRefFactory;
  protected CurrentUser user;

  @Inject
  public ManifestUpdater(CurrentUser user, ProjectControl.Factory pctlFactory,
      GitFile.Factory gitFileFactory, GitRepositoryManager repos,
      //CreateChange.Factory ccFactory,
      TempRef.Factory tmpRefFactory) {
//    this.ccFactory = ccFactory;
    this.gitFileFactory = gitFileFactory;
    this.pctlFactory = pctlFactory;
    this.repos = repos;
    this.tmpRefFactory = tmpRefFactory;
    this.user = user;
  }

  public void read(Branch.NameKey branch, String srcFile)
      throws ConfigInvalidException, IOException, JDOMException,
      NoSuchProjectException, NoSuchRefException, ParserConfigurationException,
      PermissionDeniedException, SAXException {
    ProjectControl pctl = pctlFactory.controlFor(branch.getParentKey());
    if (!pctl.controlForRef(branch).isVisible()) {
      throw new NoSuchRefException(branch.getShortName());
    }
    manifestFile = gitFileFactory.create(branch, srcFile);
    manifestXml = new ManifestXml(manifestFile.read());
  }

  public List<Project> getProjects() {
    Document doc = manifestXml.getDocument();
    XPathFactory xFactory = XPathFactory.instance();
    String xPathStr = "/manifest/project";
    XPathExpression<Element> prjXPathExpr = xFactory.compile(xPathStr,
        Filters.element());

    List<Project> projects = new ArrayList<>();
    for (Element element: prjXPathExpr.evaluate(doc)) {
      projects.add(new Project(element));
    }
    return projects;
  }

  public List<Project> getProjects(String attname, String value) {
   return match(getProjects(), attname, value);
  }

  public RevCommit write(Branch.NameKey branch, String file,
      String commitMessage) throws ConfigInvalidException, IOException,
      NoSuchProjectException, PermissionDeniedException {
    ProjectControl pctl = pctlFactory.controlFor(branch.getParentKey());
    if (!pctl.controlForRef(branch).canUpdate()) {
      throw new PermissionDeniedException("Cannot write to " + branch);
    }
    manifestFile.setBranch(branch);
    manifestFile.setFileName(file);
    return manifestFile.write(manifestXml.getManifestText(), commitMessage);
  }

  public static List<Project> match(Iterable<Project> projects, String attname,
      String value) {
    List<Project> retProjects = new ArrayList<>();
    for (Project project: projects) {
      if (project.getAttribute(attname).equals(value)) {
        retProjects.add(project);
      }
    }
    return retProjects;
  }
}
