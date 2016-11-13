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

import org.jdom2.Document;
import org.jdom2.JDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;
import org.jdom2.input.sax.SAXHandler;
import org.jdom2.input.sax.SAXHandlerFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.UUID;


/** Some xml parsing routines tuned for our manifests */
public class ManifestXml {
  private Document doc;
  private ArrayList<DTDAttribute> dtdAttributes = new ArrayList<>();
  private String replacementText;

  public ManifestXml(String xml) throws IOException,
      ParserConfigurationException, SAXException, JDOMException {
    // Insert a unique identifier for entity definitions to prevent them from
    // getting expanded during the parse
    genReplacementText(xml);
    xml = xml.replaceAll("&([^;]*);", replacementText + "$1;");

    SAXBuilder builder = new SAXBuilder();
    builder.setSAXHandlerFactory(new SAXHandlerFactory() {
      @Override
      public SAXHandler createSAXHandler(JDOMFactory jdomFactory) {
        return new SAXHandler(){
          @Override
          public void attributeDecl(String eName, String aName, String type,
                                    String valueDefault, String value){
            dtdAttributes.add(new DTDAttribute(eName, aName, type, valueDefault,
                value));
            super.attributeDecl(eName, aName, type, valueDefault, value);
          }
        };
      }
    });
    builder.setExpandEntities(false);
    doc = builder.build(new InputSource(new StringReader(xml)));
  }

  public Document getDocument() {
    return doc;
  }

  public String getManifestText() throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    printDocument(stream);
    return stream.toString().replaceAll(replacementText, "&");
  }

  private void printDocument(OutputStream out)
      throws IOException{
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    outputter.setXMLOutputProcessor(new CustomOutputter(dtdAttributes));

    outputter.output(doc, out);
  }

  private void genReplacementText(String xml){
    String uuid = UUID.randomUUID().toString();
    while (xml.contains(uuid)) {
      uuid = UUID.randomUUID().toString();
    }
    replacementText = uuid;
  }
}


