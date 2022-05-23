# Kalix Workshop - Loan application - Java

## Prerequisite
Java 11 or later<br>
Apache Maven 3.6 or higher<br>
[Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) <br>
Docker 20.10.8 or higher (client and daemon)<br>
Container registry with public access (like Docker Hub)<br>
Access to the `gcr.io/kalix-public` container registry<br>
cURL<br>
IDE / editor<br>

## Create kickstart maven project

```
mvn archetype:generate \
-DarchetypeGroupId=io.kalix \
-DarchetypeArtifactId=kalix-maven-archetype \
-DarchetypeVersion=LATEST
```
Define value for property 'groupId': `io.kx`<br>
Define value for property 'artifactId': `loan-application`<br>
Define value for property 'version' 1.0-SNAPSHOT: :<br>
Define value for property 'package' io.kx: : `io.kx.loanapp`<br>

## Import generated project in your IDE/editor
<i><b>Delete all proto files after done</b></i>

## Update main class
In `pom.xml`:
1. In `<mainClass>io.kx.loanapp.Main</mainClass>` replace `io.kx.loanapp.Main` with `io.kx.Main`
2. In `<dockerImage>my-docker-repo/${project.artifactId}</dockerImage>` replace `my-docker-repo` with the right `dockerId`


# Loan application service

## Define API data structure and endpoints (GRPC)
Create `io/kx/loanapp/api` folder in `src/main/proto` folder. <br>
Create `loan_app_api.proto` in `src/main/proto/io/kx/loanapp/api` folder. <br>
Create: <br>
- headers
- state
- commands
- service

<i><b>Tip</b></i>: Check content in `step-1` git branch

## Define persistence (domain) data structure  (GRPC)
Create `io/kx/loanapp/doman` folder in `src/main/proto` folder. <br>
Create `loan_app_domain.proto` in `src/main/proto/io/kx/loanapp/domain` folder. <br>
Create: <br>
- headers
- state
- events

<i><b>Tip</b></i>: Check content in `step-1` git branch
## Add codegen annotations in API data structure and endpoints (GRPC)
In `src/main/proto/io/kx/loanapp/api/loan_app_api.proto` add AkkaServerless codegen annotations to GRPC service
```
service LoanAppService {
```
```
option (kalix.codegen) = {
    event_sourced_entity: {
      name: "io.kx.loanapp.domain.LoanAppEntity"
      entity_type: "loanapp"
      state: "io.kx.loanapp.domain.LoanAppDomainState"
      events: [
        "io.kx.loanapp.domain.Submitted",
        "io.kx.loanapp.domain.Approved",
        "io.kx.loanapp.domain.Declined"
      ]
    }
  };
```
```
...
```
<i><b>Note</b></i>: `event_sourced_entity.name` has to be a unique name
## Compile maven project to trigger codegen
```
mvn compile
```
Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>
Business logic:<br>
`src/main/java/io/kx/Main`<br>
`src/main/java/io/kx/loanapp/domain/LoanAppEntity`<br>
<br>
Unit tests:<br>
`src/test/java/io/kx/loanapp/domain/LoanAppEntityTest`<br>
Integration tests:<br>
`src/it/java/io/kx/loanapp/api/LoanAppEntityIntegrationTest`<br>

<i><b>Tip</b></i>: If required reimport/re-sync project in your IDE
## Implement entity skeleton class
Implement `src/main/java/io/kx/loanapp/domain/LoanAppEntity` class <br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Implement unit test
Implement  `src/test/java/io/kx/loanapp/domain/LoanAppEntityTest` class<br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Run unit test
```
mvn test
```
## Implement integration test
Implement `src/it/java/io/kx/loanapp/api/LoanAppEntityIntegrationTest` class<br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Run integration test
```
mvn -Pit verify
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: If you do not require google pubsub emulator then comment it out in `docker-compose.yaml`
```
docker-compose up
```

Start the service:

```
mvn compile exec:exec
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT http://localhost:9000/loanapp/1/approve -H "Content-Type: application/json"
```

## Package

<i><b>Note</b></i>: Make sure you have updated `dockerImage` in your `pom.xml` and that your local docker is authenticated with your docker container registry

```
mvn package
```

<br><br>

Push docker image to docker repository:
```
mvn docker:push
```

## Register for Kalix account or Login with existing account
[Register](https://console.kalix.io/register)

## kalix CLI
Validate version:
```
kalix version
```
Login (need to be logged in the Kalix Console in web browser):
```
kalix auth login
```
Create new project:
```
kalix projects new loan-application --region <REGION>
```
<i><b>Note</b></i>: Replace `<REGION>` with desired region

List projects:
```
kalix projects list
```
Set project:
```
kalix config set project loan-application
```
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.0-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository

List services:
```
kalix services list
```
```
NAME               AGE    REPLICAS   STATUS   DESCRIPTION   
loan-application   102s   1          Ready  
```
## Expose service
```
kalix services expose loan-application
```
Result:
`
Service 'loan-application' was successfully exposed at: <some_host>.us-east1.kalix.app
`
## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Approve:
```
curl -XPUT https://<somehost>.kalix.app/loanapp/1/approve -H "Content-Type: application/json"
```