/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.elpa.internal;

import java.util.List;

public class ElpaAttributes
{
  public static final String P_DESCRIPTION = "description";

  public static final String P_URL = "url";

  public static final String P_KEYWORDS = "keywords";

  private String name;

  private String version;

  private String description;

  private List<ElpaRequired> requires;

  private String url;

  private List<String> keywords;

  public String getName() {
    return name;
  }

  public ElpaAttributes setName(String name) {
    this.name = name;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public ElpaAttributes setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public ElpaAttributes setDescription(String description) {
    this.description = description;
    return this;
  }

  public List<ElpaRequired> getRequires() {
    return requires;
  }

  public ElpaAttributes setRequires(List<ElpaRequired> requires) {
    this.requires = requires;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public ElpaAttributes setUrl(String url) {
    this.url = url;
    return this;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public ElpaAttributes setKeywords(List<String> keywords) {
    this.keywords = keywords;
    return this;
  }

  public String toString() {
    return "ElpaAttributes(" +
        "name=" + name + "," +
        "version=" + version + "," +
        "description=" + description + "," +
        "requires=" + requires + "," +
        "url=" + url + "," +
        "keywords=" + keywords + ")";
  }
}
