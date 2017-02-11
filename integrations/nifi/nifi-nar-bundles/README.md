Apache NiFi NAR bundles
==========

### Overview

This subtree provides extensions to NiFi.  Each significant subproject has its own readme.

Apache NiFi extensions are packaged in NARs (NiFi archives). A NAR allows several components and their dependencies to be
packaged together into a single package. The NAR package is then provided ClassLoader isolation from other NAR packages. 

### Organization

The organization structure for NAR files follows the Apache NiFi convention.  Each distinct technology area is defined
in its own "bundle" subproject.  The bundle contains a processors or services project and a corresponding nar project which
creates the NAR packaging.

### Deployment

Only the generated NAR files should be deployed to the NiFi /lib folder. 

### More information

[Apache NiFi Maven Projects for Extensions](https://cwiki.apache.org/confluence/display/NIFI/Maven+Projects+for+Extensions)

