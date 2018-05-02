<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2018-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
## ELPA Repositories

### Introduction

### Proxying ELPA Repositories

The Nexus Repository ELPA format allows the Emacs Lisp Package Archive to be
proxied and cached.  This includes compatible third-party repositories such as
http://melpa.org/ and https://marmalade-repo.org/.

To proxy an ELPA repository, you simply create a new 'elpa (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/display/NXRM3/Configuration#Configuration-RepositoryManagement) in
details. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. [http://elpa.gnu.org/packages/](http://elpa.gnu.org/packages/)
- Select a 'Blob store' for 'Storage'

### Configuring Emacs for use with Nexus Repository

To configure Emacs to use Nexus Repository as a Proxy for remote ELPA sites, add
it to `package-archives` variable in your .emacs file:

    (require 'package)
    (setq package-archives '(("nexus" . "http://localhost:8081/repository/elpa-proxy/")))
    (package-initialize)

After restarting emacs, running package commands such as `list-packages` should route through Nexus.

### Browsing ELPA Repository Packages

You can browse ELPA repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).
