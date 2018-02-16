## CI/CD Demo - Openshift Jenkins Setup(OJS) - OpenShift Container Platform 3.7

![pipeline](https://user-images.githubusercontent.com/28925814/36300586-f302f4b4-1327-11e8-8df6-a1ecdafa5560.jpg)

This repository includes the pipeline definition for a Spring Boot Application to achieve continuous delivery using Jenkins, Nexus and SonarQube on OpenShift. On every pipeline execution, the code goes through the following steps:

1. Code is cloned from GitHub(this repo), built, tested using `JUnit` tests and analyzed by `SonarQube`.
2. The JAR artifact is pushed to `Nexus Repository manager`.
3. A container image (app:latest) is built based on the application JAR artifact deployed on `Open JDK`.
4. The container image is deployed in a fresh new container in **DEVEPLOMENT project**.
5. If integration tests are successful, the DEV image is tagged with the `Jenkins Build Number (app:xx)` in the PRODUCTION project.
6. The production image is deployed in a fresh new container in the **PRODUCTION project**.


## Prerequisites
* 8+ GB memory for OpenShift (10+ GB memory if using SonarQube & Nexus)
* Openjdk-s2i-imagestream imported to OpenShift (see Troubleshooting section for details)


## Setup on Local System
* Download/Install/Run Jenkins, Sonatype Nexus, Docker, SonarQube & Openshift OC CLI
* In SonarQube at first login copy the login auth code & change it in Jenkinsfile @ `Stage - Sonar Code Analysis`
* In Jenkins install `Openshift CLient`, `Openshift Login Plugin`, `Openshift Pipeline Jenkins Plugin`.
* Create a new Jenkins Pipeline Parameterized Build & create the parameters as per below image.
![jenkins](https://user-images.githubusercontent.com/28925814/36299790-b1437330-1324-11e8-86a7-4428cf0c797a.jpg)
* In Nexus create two repositories `release` & `snapshot`
![nexus](https://user-images.githubusercontent.com/28925814/36299834-d1b60434-1324-11e8-8d94-3ff39ec0cf38.jpg)


## Setup on OpenShift
Follow these [instructions](https://github.com/openshift/origin) in order to create a local OpenShift cluster. Otherwise using your current OpenShift cluster, create the following projects for components, Dev and Prod environments:
```
oc new-project dev --display-name="OJS - Development"
oc new-project prod --display-name="OJS - Production"

//Login as admin & create ImageStream in project openshift
oc create -f https://gist.githubusercontent.com/tqvarnst/3ca512b01b7b7c1a1da0532939350e23/raw/1973a8baf6e398f534613108e0ec5a774a76babe/openjdk-s2i-imagestream.json
```
## Guide
1. Start Jenkins Build & Enter the parameters.
2. Pipelines pauses at two STAGE's for approval. Click on this step on the pipeline and then Promote.
3. After pipeline completion, check the following:
  * Explore the snapshots repository in Nexus and verify JAR is pushed to the repository.
  * Explore SonarQube check the metrics, stats, code coverage, etc.
  * Explore App - Dev project in OpenShift console and verify the application is deployed in the DEV environment.
  * Explore App - Prod project in OpenShift console and verify the application is deployed in the PROD environment.
4. Try changing the unit tests to fail them & then pipeline will fail during unit tests due to the enabled changed condition.

## Issues
In Jenkinsfile @ `stage('Openshift Start Build')` I am using cURL to download a specific JAR from Nexus. Currently this step is **hardcoded**. Trying to find a Nexus REST API to download *latest/newest JAR* from Repo.
```
curl -O -X GET -u admin:admin123 http://localhost:8081/repository/snapshot/com/openshift/test/openshift-jenkins/0.0.1-SNAPSHOT/openshift-jenkins-0.0.1-20180214.210246-15.jar
```
