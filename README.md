[![alt Kylo](https://cloud.githubusercontent.com/assets/5693584/22863033/4976d7d2-f0ee-11e6-95ec-3a30e2162a3c.png)](http://kylo.io/)

Kylo is an enterprise-ready data lake management software platform for Hadoop and Spark integrating best practices around metadata management, governance, and security learned from over Think Big's 150+ successful big data projects.

Visit [http://kylo.io](http://kylo.io) and unlock the power of Kylo today!

## Quick Start

You can download a pre-configured sandbox and get started with Kylo in no time.
To get started visit the [Quick Start](http://kylo.io/quickstart.html) page.

## Documentation

Please visit [Kylo documentation](http://kylo.readthedocs.io/) to learn more.  

## Issues and Support

To raise issues with Kylo, please register and visit the [Jira instance](https://kylo-io.atlassian.net/projects/KYLO).

For support, questions can be asked on the [Google Groups group](https://groups.google.com/forum/#!forum/kylo-community).

## Code Structure

The layout of code in the following subtrees follows particular reasoning described below: 

| Subfolder        | Description           |
| ------------- |-------------|
| [commons](commons) |  Utility or common functionality
| [core](core) | API frameworks that can generally used by developers to extend the capabilities of Kylo
| [docs](docs) | Documentation that should be distributed as part of release
| [integrations](integrations) | Pure integration projects with 3rd party software such as NiFi and Spark. 
| [metadata](metadata) | The metadata server is a top-level project for providing a metadata repository
| [plugins](plugins) | Alternative and often optional implementations for operating with different distributions or technology branches
| [samples](samples) | Sample plugins, feeds, and templates,
| [security](security) | Support for application security for both authentication and authorization
| [services](services) | Provides REST endpoints and core server-side processing
| [ui](ui) | User Interface module for Kylo
