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
package org.sonatype.nexus.repository.elpa.internal.sexp

import spock.lang.Specification
import spock.lang.Unroll

/**
 * {@link SexpParser} tests.
 */
public class SexpParserTest
    extends Specification
{

  SexpParser sexpParser = new SexpParser()

  @Unroll
  def 'parse \'#input\''() {
    when:
      def sexp = sexpParser.parse(new StringReader(input))

    then:
      sexp == expectedSexp

    where:
      input                              || expectedSexp
      '()'                               || Sexp.NIL
      '(())'                             || new ListSexp([Sexp.NIL])
      'test'                             || new Atom('test')
      '(symbol "string")'                || new ListSexp([new Atom('symbol'), new Atom('"string"')])
      '"string with spaces"'             || new Atom('"string with spaces"')
  }
}
