Operational Metadata upgrade service
===
This module is desiged to detect and apply and programmatic updates to the Operational metadata store.
This module looks at the KYLO_VERSION database table to determine the database version and compares it to the auto generated "version.txt" file found in the /thinkbig-services/conf folder (generated via maven)
The current upgrade is to support new features in the 0.4.0 release that helps better align the Operational Feed Metadata with the JCR setup metadata.
