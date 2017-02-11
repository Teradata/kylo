ui-common
===================

Purpose
------------
The **ui-common** module has common javascript and java code that pertains to the user interface projects.  
This allows for common javascript css to be coded in one place and then referenced across other projects.  Modules can include this project as a maven dependency and then reference the components.

Referencing Static Content
----------
1. Include this module as a maven dependency
```xml
  <dependency>
    <groupId>com.thinkbiganalytics.datalake</groupId>
    <artifactId>kylo-data-lake-ui-common</artifactId>
    <version>0.7.0-SNAPSHOT</version>
   </dependency>
```
2. Once included all the common static content is available at the path **/ui-common/**.  
	1. This path is configured in the **StaticCommonContentConfiguration.java** class.

Static External Code
-------------
If a webapp module that depends upon this common project wants to reference a common js file they would do so using the following syntax
```html
    <script src="/ui-common/js/vendor/jquery/jquery.js" type="text/javascript"></script>
    <script src="/ui-common/js/vendor/angular/angular.js" type="text/javascript"></script>
     <link rel="stylesheet" href="/ui-common/js/vendor/angular-material/angular-material.min.css"/>
```
 The above code will pull in the jquery.js , angular.js, and angular-materials.min css files from this project.

Static Common Angular Code
-----------
This project has custom common angular  services and directives that can be used across webapps.
For convenience all js, in the **/src/main/resources/static-common/js/**, except the **vendor** directory is merged together in a single file at **/src/main/resources/static-common/js/min/datalake-ui-common.js**
The angular components are stored in the angular module **datalakeui.common**.  To use these common angular components you need to include the **datalake-ui-common.js** file and add the dependency to your angular module like below:

**Add the dependency to the html file**
```html
<script src="/ui-common/js/min/datalake-ui-common.js"></script>
```
**Add the module to your application**
```javascript
angular.module("mymodule", ['datalakeui.common']);
```
### Static Template files
Static Template files are merged together and placed in the **/resources/static-common/templates.js**
This is done using **gulp** and the node module **gulp-angular-templatecache**. 

**NOTE:**If a static template file is modified then gulp will be needed to run to update this **/resources/static-common/templates.js** file.

#### Installing and running gulp to merge the template html files
1. install node  
2. install gulp  (follow steps 1 and 2 here: https://github.com/gulpjs/gulp/blob/master/docs/getting-started.md)
3. install the **gulp-angular-templatecache**
	1. npm install gulp-angular-templatecache --save-dev
4.  run the gulp command in a terminal at the data-lake-ui-common root
	1. > gulp
	1. This will run the **gulpfile.js** which will merge the template html files into the **templates.js** javascript file


