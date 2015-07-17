@REM
@REM Copyright (C) 2015 Orange
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

REM This script is a sample of windows script that can be used to manually update an elpaaso database with liquibase changelog
REM Note: if this script is executed against an existing database its schema must already have been initialized or synchronized with liquibase
REM
REM Usage: this script must be launched in the directory containing the changelog files: cloud-paas-db/src/main/resources/liquibase

REM CAUTION: before using this script, review/update following variables

REM localisation of your maven local repository
set m2_repo=C:\x-data\maven-repository

REM liquibase and postgresql jdbc driver jars
set liquibase_jar=%m2_repo%\org\liquibase\liquibase-core\2.0.3\liquibase-core-2.0.3.jar
set postgresql_jar=%m2_repo%\postgresql\postgresql\9.0-801.jdbc4\postgresql-9.0-801.jdbc4.jar

REM credentials of elpaaso database to be synchronized
set db_url=jdbc:postgresql://<host>:<port>/<database>
set db_username=<user>
set db_password=<password>

java  -jar %liquibase_jar% --classpath="%postgresql_jar%" --changeLogFile="./paas-db-changelog-1.0-full.xml" --url="%db_url%" --username="%db_username%" --password="%db_password%"  update
java  -jar %liquibase_jar% --classpath="%postgresql_jar%" --changeLogFile="./paas-db-changelog-1.1-delta.xml" --url="%db_url%" --username="%db_username%" --password="%db_password%" update
java  -jar %liquibase_jar% --classpath="%postgresql_jar%" --changeLogFile="./paas-db-changelog-1.2-delta.xml" --url="%db_url%" --username="%db_username%" --password="%db_password%" update

