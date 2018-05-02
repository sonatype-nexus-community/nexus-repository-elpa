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

import static com.google.common.base.Preconditions.checkNotNull;

public class Atom implements Sexp {

  private final String value;

  public Atom(final String value) {
    this.value = checkNotNull(value);
  }

  @Override
  public boolean isNil() {
    return false;
  }

  public String symbol() {
    return value;
  }

  public String string() {
    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
      return value.substring(1, value.length() - 1);
    }
    else {
      throw new IllegalStateException("Atom is not a string");
    }
  }

  public String toString() {
    return "Atom(" + value + ")";
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    return value.equals(((Atom) other).value);
  }

}
