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

public class DTDAttribute {
  private String elementName;
  private String attributeName;
  private String type;
  private String valueDefault;
  private String value;

  public DTDAttribute(String eName, String aName, String type,
                      String valueDefault, String value) {
    this.elementName = eName;
    this.attributeName = aName;
    this.type = type;
    this.valueDefault = valueDefault;
    this.value = value;
  }

  public String getElementName(){
    return this.elementName;
  }

  public String getAttributeName(){
    return this.attributeName;
  }

  public String getType(){
    return this.type;
  }

  public String getValueDefault(){
    return this.valueDefault;
  }

  public String getValue(){
    return this.value;
  }
}
