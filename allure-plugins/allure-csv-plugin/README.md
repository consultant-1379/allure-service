# Important Notice

The plugin `allure-csv-plugin` will work correctly only when built by Maven,
in order to make sure that `yuicompressor-maven-plugin` goal `compress` is executed,
and all `allure-csv-plugin` 3PP Library dependencies are included in the final artifact. 

Otherwise, if you will launch the `allure-service` by executing `AllureServiceApplication` 
directly using your IDE, the IDE will build the project, but skip Maven plugins execution, 
which will lead to the incorrectly built `allure-csv-plugin` JAR file, which in turn will 
result in `allure-service` generating corrupted Allure Reports.

`allure-csv-plugin` relies on the AngularJS module `ngSanitize`, which is not included
in the core AngularJS distribution file `angular.js`, but instead is bundled as a separate
`angular-sanitize.js` file. Allure Reports do not include this file, and therefore it is
up to the `allure-csv-plugin` to make sure that `ngSanitize` is present in the report,
because otherwise generated Allure Reports will fail at AngularJS startup time due to
the unsatisfied `ngSanitize` module dependency.