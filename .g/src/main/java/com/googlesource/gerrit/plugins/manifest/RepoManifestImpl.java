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

import com.googlesource.gerrit.plugins.manifest.extensions.RepoManifest;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RepoManifestImpl implements RepoManifest {
  private Map<String, String> defaults = new
      HashMap<String, String>();
  private Map<String, Map<String, String>> remotes =
      new HashMap<String, Map<String, String>>();
  private Map<String, Map<String, String>> projects =
      new HashMap<String, Map<String, String>>();

  public RepoManifestImpl(String xml) throws IOException,
      ParserConfigurationException, SAXException {
    Document doc = newBuilder().parse(new InputSource(new StringReader(xml)));
    NodeList tops = doc.getChildNodes();
    for (int i = 0 ; i < tops.getLength() ; i++) {
      Node manifest = tops.item(i);
      if (NODE_MANIFEST.equals(manifest.getNodeName())) {
        NodeList subs = manifest.getChildNodes();
        for (int j = 0 ; j < subs.getLength() ; j++) {
          Node node = subs.item(j);
          String name = node.getNodeName();
          if (NODE_INCLUDE.equals(name) || NODE_REMOVE_PROJECT.equals(name)) {
            throw new SAXException("unsupported element: " + name);
          }
          Map<String, String> atts = attributesToMap(node);
          if (NODE_DEFAULT.equals(name)) {
            defaults.putAll(atts);
          } else if (NODE_REMOTE.equals(name)) {
            remotes.put(atts.get(ATTR_REMOTE_NAME), atts);
          } else if (NODE_PROJECT.equals(name)) {
            if (atts.get(ATTR_PROJECT_PATH) == null) {
              atts.put(ATTR_PROJECT_PATH, atts.get(ATTR_PROJECT_NAME));
            }
            projects.put(atts.get(ATTR_PROJECT_PATH), atts);
          }
        }

        for (Map<String, String> project : projects.values()) {
          populateDefault(project, ATTR_PROJECT_REMOTE);
          populateDefault(project, ATTR_PROJECT_REVISION);
          populateDefault(project, ATTR_PROJECT_DEST_BRANCH);
        }
        return;
      }
    }
    throw new SAXException("Cannot Parse Manifest File");
  }

  @Override
  public Map<String, String> getDefaults() {
    return defaults;
  }

  @Override
  public Map<String, Map<String, String>> getRemotes() {
    return remotes;
  }

  @Override
  public Map<String, Map<String, String>> getProjects() {
    return projects;
  }

  public void populateDefault(Map<String, String> atts, String att) {
    if (!atts.containsKey(att)) {
      atts.put(att, defaults.get(att));
    }
  }

  public static Map<String, String> attributesToMap(Node node) {
    Map<String, String> atts = new HashMap<String, String>();
    NamedNodeMap nmmap = node.getAttributes();
    if (nmmap != null) {
      for (int i = 0 ; i < nmmap.getLength() ; i++) {
        Attr att = (Attr) nmmap.item(i);
        atts.put(att.getName(), att.getValue());
      }
    }
    return atts;
  }

  private static DocumentBuilder newBuilder()
      throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setExpandEntityReferences(false);
    factory.setIgnoringComments(true);
    factory.setCoalescing(true);
    return factory.newDocumentBuilder();
  }
}
