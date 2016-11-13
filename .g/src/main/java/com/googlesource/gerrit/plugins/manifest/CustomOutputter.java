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

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class CustomOutputter extends AbstractXMLOutputProcessor {
  private ArrayList<DTDAttribute> dtdAttributes;

  public CustomOutputter(ArrayList<DTDAttribute> dtdAttributes) {
    this.dtdAttributes = dtdAttributes;
  }

  @Override
  public void process(Writer out, Format format, Document doc) throws IOException {
    format.setLineSeparator("\n");
    super.process(out, format, doc);
  }

  @Override
  protected void printAttribute(java.io.Writer out, FormatStack fstack,
      Attribute attribute) throws java.io.IOException {
    // Do not print attributes that use default values
    for (DTDAttribute dtdAttribute : dtdAttributes) {
      if (attribute.getName().equals(dtdAttribute.getAttributeName()) &&
          attribute.getParent().getName().equals(dtdAttribute.getElementName()) &&
          attribute.getAttributeType().toString().equals(dtdAttribute.getType()) &&
          attribute.getValue().equals(dtdAttribute.getValue())) {
        return;
      }
    }

    out.append(fstack.getLineSeparator());
    String indent = fstack.getIndent();
    out.append(fstack.getLevelIndent());
    out.append(indent);
    // super.printAttribute() indents with an extra space, this will offset that
    out.append(indent.substring(0, indent.length() - 1));
    super.printAttribute(out, fstack, attribute);
  }
}