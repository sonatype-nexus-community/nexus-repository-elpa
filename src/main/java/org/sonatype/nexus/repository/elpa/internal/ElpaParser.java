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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.elpa.internal.sexp.Atom;
import org.sonatype.nexus.repository.elpa.internal.sexp.ListSexp;
import org.sonatype.nexus.repository.elpa.internal.sexp.Sexp;
import org.sonatype.nexus.repository.elpa.internal.sexp.SexpParser;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

/**
 * Parses an ELPA package into {@link ElpaAttributes}
 */
@Named
@Singleton
public class ElpaParser
    extends ComponentSupport
{
  // matches ;;; name --- description [-*- mode -*-]
  private static final Pattern EL_SOURCE_HEADER = Pattern.compile("^\\s*;;;\\s*([\\S-]*)\\s---*\\s*(.*?)\\s*(-\\*-.*)?$");
  // matches ;; name: value
  private static final Pattern EL_SOURCE_ATTRIBUTE = Pattern.compile("^\\s*;;\\s*([\\w-]*)\\s*:\\s*(.*)$");

  private static final String URL_KEY = "url";
  private static final String VERSION_KEY = "version";
  private static final String PACKAGE_REQUIRES_KEY = "package-requires";
  private static final String KEYWORDS_KEY = "keywords";

  private final SexpParser sexpParser = new SexpParser();

  public ElpaAttributes parseSingle(final InputStream in) throws IOException {
    ElpaAttributes attributes = new ElpaAttributes();
    try (InputStreamReader inputStreamReader = new InputStreamReader(in, UTF_8);
         BufferedReader reader = new BufferedReader(inputStreamReader)) {
      reader.lines().forEach(line -> ingestAttributesOnSourceLine(line, attributes));
    }
    return attributes;
  }

  public ElpaAttributes parseTar(final String name, final InputStream in) throws IOException {
    try (TarArchiveInputStream tar = new TarArchiveInputStream(in)) {
      TarArchiveEntry entry;
      while ((entry = tar.getNextTarEntry()) != null) {
        if (entry.getName().endsWith("/" + name + ".el")) {
          return parseSingle(tar);
        }
      }
    }
    return new ElpaAttributes();
  }

  /**
   * Ingests any attributes declared in the comments of a line of source code.
   */
  private void ingestAttributesOnSourceLine(final String line, final ElpaAttributes attributes) {
    Matcher headerMatcher = EL_SOURCE_HEADER.matcher(line);
    if (headerMatcher.matches()) {
      if (attributes.getName() == null) {
        attributes.setName(headerMatcher.group(1));
      }
      if (attributes.getDescription() == null) {
        attributes.setDescription(headerMatcher.group(2));
      }
    }
    Matcher attributeMatcher = EL_SOURCE_ATTRIBUTE.matcher(line);
    if (attributeMatcher.matches()) {
      if (URL_KEY.equalsIgnoreCase(attributeMatcher.group(1))) {
        attributes.setUrl(attributeMatcher.group(2));
      }
      else if (VERSION_KEY.equalsIgnoreCase(attributeMatcher.group(1))) {
        attributes.setVersion(attributeMatcher.group(2));
      }
      else if (PACKAGE_REQUIRES_KEY.equalsIgnoreCase(attributeMatcher.group(1))) {
        Sexp sexp = sexpParser.parse(new StringReader(attributeMatcher.group(2)));
        List<ElpaRequired> required = list(sexp).stream()
          .map(this::list)
          .map(req -> new ElpaRequired(symbol(req.get(0)), string(req.get(1))))
          .collect(toList());
        attributes.setRequires(required);
      }
      else if (KEYWORDS_KEY.equalsIgnoreCase(attributeMatcher.group(1))) {
        attributes.setKeywords(Arrays.asList(attributeMatcher.group(2).split(",\\s")));
      }
    }
  }

  private List<Sexp> list(final Sexp sexp) {
    return ((ListSexp)sexp).getList();
  }

  private String symbol(final Sexp sexp) {
    return ((Atom)sexp).symbol();
  }

  private String string(final Sexp sexp) {
    return ((Atom)sexp).string();
  }
}
