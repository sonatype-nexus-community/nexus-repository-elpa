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
package org.sonatype.nexus.repository.elpa.internal

import spock.lang.Specification

/**
 * {@link ElpaParser} tests.
 */
public class ElpaParserTest
    extends Specification
{

  ElpaParser elpaParser = new ElpaParser();

  def "ingests attributes from an el source file"() {
    given:
      def source = this.getClass().getResource('/test-file.el').text

    when:
      def attributes = elpaParser.parseSingle(new ByteArrayInputStream(source.bytes))

    then:
      attributes.name == 'test-file.el'
      attributes.description == 'Test description here.'
      attributes.url == 'https://github.com/foo/bar'
      attributes.version == '1.2.3'
      attributes.keywords == ['kw1', 'kw2']
      attributes.requires.collect { [it.name, it.version] } == [['xyzzy', '3.2.1'], ['foobar', '0.0.0']]
  }

  def "ingests attributes from a tar file"() {
    given:
      def tarBytes = this.getClass().getResource('/test-file.tar').bytes

    when:
      def attributes = elpaParser.parseTar('test-file', new ByteArrayInputStream(tarBytes))

    then:
      attributes.name == 'test-file.el'
      attributes.description == 'Test description here.'
      attributes.url == 'https://github.com/foo/bar'
      attributes.version == '1.2.3'
      attributes.keywords == ['kw1', 'kw2']
      attributes.requires.collect { [it.name, it.version] } == [['xyzzy', '3.2.1'], ['foobar', '0.0.0']]
  }
}
