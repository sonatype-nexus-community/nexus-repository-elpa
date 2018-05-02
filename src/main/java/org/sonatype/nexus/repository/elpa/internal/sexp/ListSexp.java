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

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListSexp implements Sexp {

  private final List<Sexp> list;

  ListSexp(final List<Sexp> list) {
    this.list = checkNotNull(list);
  }

  @Override
  public boolean isNil() {
    return false;
  }

  public List<Sexp> getList() {
    return list;
  }

  @Override
  public String toString() {
    return "List(" + list + ")";
  }

  @Override
  public int hashCode() {
    return list.hashCode();
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
    return list.equals(((ListSexp) other).list);
  }
}
