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
import java.util.ListIterator;

import static org.sonatype.nexus.repository.elpa.internal.sexp.Tokenizer.Token;

/**
 * A simple sexp (s-expression) parser.
 */
public class SexpParser {

  public Sexp parse(final Reader in) {
    try {
      Tokenizer tokenizer = new Tokenizer();
      return parse(tokenizer.tokenize(in).listIterator());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Sexp parse(final ListIterator<Token> tokenStream) {
    Token token = tokenStream.next();
    if (token.isLeftParen()) {
      List<Sexp> list = parseList(tokenStream);
      if (list.isEmpty()) {
        return Sexp.NIL;
      }
      else {
        return new ListSexp(list);
      }
    }
    else {
      return new Atom(token.getValue());
    }
  }

  private List<Sexp> parseList(final ListIterator<Token> tokenStream) {
    List<Sexp> list = new ArrayList<>();
    Token token = tokenStream.next();
    while (!token.isRightParen()) {
      tokenStream.previous();
      list.add(parse(tokenStream));
      token = tokenStream.next();
    }
    return list;
  }
}
