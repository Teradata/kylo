# Kylo

![kylo-logo-orange](https://cloud.githubusercontent.com/assets/5693584/22863033/4976d7d2-f0ee-11e6-95ec-3a30e2162a3c.png)


## Deployment Guide

To get started, please follow the [deployment guide](docs/latest/deployment-guide.adoc)

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
