data-lake-ui-common
===================

Purpose
------------
The **data-lake-ui-common** module has common javascript and java code that pertains to the **data-lake-ui** project.  
This allows for common javascript css to be coded in 1 place and then referenced across other projects.  Modules can include this project as a maven dependency and then reference the components.

Referencing Static Content
----------
In a sub module all static content is available at the path **/ui-common/**.  This is done via the **StaticCommonContentConfiguration** class.

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
```html
angular.module("mymodule", ['datalakeui.common']);
```
### Static Template files
Static Template files are merged together and placed in the **/resources/static-common/templates.js**
This is done using **gulp** and the node module **gulp-angular-templatecache**. 
If a static template file is modified then gulp will be needed to run to update this **/resources/static-common/templates.js** file.

#### Installing and running gulp to merge the template html files
1. install node  
2. install gulp  (follow steps 1 and 2 here: https://github.com/gulpjs/gulp/blob/master/docs/getting-started.md)
3. install the **gulp-angular-templatecache**
	1. npm install gulp-angular-templatecache --save-dev
4.  run the gulp command in a terminal at the data-lake-ui-common root
	1. > gulp
	1. This will run the **gulpfile.js** which will merge the template html files into the **templates.js** javascript file


