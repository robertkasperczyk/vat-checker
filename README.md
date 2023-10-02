This project contains small spring-boot based web application, which is some kind of a wrapper API over the VIES vat
API.
Reasons to create this project is to solve two issues with original VIES API:

* provide comfortable access by using REST instead of SOAP (used by VIES)
* asynchronous calls to help with poor availability of VIES api

Currently, the project needs to be considered as in-progress.
There is plenty of stuff to be improved and added:

* introduction of docker
* deployment environments (currently there is single env)
* adding the static code analysis / formatting tools for maven
* considering caching of vat numbers
* securing the API with f.e. tokens
* ...

In order to run the app you are required to have MySQL v8 running on localhost and configured according to
ApplicationProperties file.