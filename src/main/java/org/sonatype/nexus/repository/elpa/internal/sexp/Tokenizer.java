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
package org.sonatype.nexus.repository.elpa.internal.sexp;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Tokenizer {

  public static class Token {

    private final String value;

    public Token(final String value) {
      this.value = checkNotNull(value);
    }

    public boolean isLeftParen() {
      return "(".equals(value);
    }

    public boolean isRightParen() {
      return ")".equals(value);
    }

    public String getValue() {
      return value;
    }

    public String toString() {
      return value;
    }
  }

  public List<Token> tokenize(Reader in) throws IOException {
    List<Token> tokens = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    boolean inString = false;
    int ch = in.read();
    while (ch != -1) {
      if (ch == '(' ||
          ch == ')' ||
          ch == '\'') {
        maybeFinishToken(tokens, current);
        tokens.add(new Token("" + (char) ch));
      }
      else if (ch == '"') {
        inString = current.length() == 0;
        current.append((char) ch);
      }
      else if (ch == ' ') {
        if (inString) {
          current.append((char) ch);
        }
        else {
          maybeFinishToken(tokens, current);
        }
      }
      else {
        current.append((char) ch);
      }
      ch = in.read();
    }
    maybeFinishToken(tokens, current);
    return tokens;
  }

  private void maybeFinishToken(List<Token> tokens, StringBuilder current) {
    if (current.length() > 0) {
      tokens.add(new Token(current.toString()));
    }
    current.setLength(0);
  }
}
