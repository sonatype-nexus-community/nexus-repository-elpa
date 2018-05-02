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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility Methods for working ELPA routes and paths
 */
@Named
@Singleton
public class ElpaPathUtils
{
  public TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  public String nameVersion(final TokenMatcher.State state) {
    return match(state, "nameVersion");
  }

  public String name(final TokenMatcher.State state) {
    String nameVersion = nameVersion(state);
    int versionStart = nameVersion.lastIndexOf('-');
    return nameVersion.substring(0, versionStart);
  }

  public String version(final TokenMatcher.State state) {
    String nameVersion = nameVersion(state);
    int versionStart = nameVersion.lastIndexOf('-');
    return nameVersion.substring(versionStart + 1);
  }

  public String path(final TokenMatcher.State state) {
    return nameVersion(state) + '.' + match(state, "extension");
  }

  public String sigPath(final TokenMatcher.State state) {
    return path(state) + ".sig";
  }

  public String contentsPath() {
    return "/archive-contents";
  }

  private String match(TokenMatcher.State state, String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }
}
