# Contributing

Pull requests are welcome.

## Unit tests

On a command line build the project with Maven to run the unit tests and integration tests:

```shell
> mvn clean test
```


## Integration Tests for Notify dev team

You will need to source an environment.sh file. The contents of that file are explained below. All secrets are found in the jenkins vault, stored in ansible in the notifications-aws repo.

```
export NOTIFY_API_URL=https://api.notify.works
export API_KEY= # vault_jenkins_notify_client.api_key
export FUNCTIONAL_TEST_EMAIL=notify-tests-preview+client_funct_tests@digital.cabinet-office.gov.uk
export FUNCTIONAL_TEST_NUMBER=+447537417379 # Twilio number
export EMAIL_TEMPLATE_ID=f0bb62f7-5ddb-4bf8-aac7-c1e6aefd1524
export SMS_TEMPLATE_ID=31046c06-418a-49bf-86de-706b68415b47
export LETTER_TEMPLATE_ID=de1252c4-d8c3-4435-92fb-02f136778b2b
export EMAIL_REPLY_TO_ID=db8d1a9d-41ef-43cd-a04a-ed7d95214d95
export SMS_SENDER_ID=e9355456-c52f-4648-abb6-5fc192b29195
export INBOUND_SMS_QUERY_KEY= # vault_jenkins_notify_client.inbound_sms_query_key
export API_SENDING_KEY= # vault_jenkins_notify_client.api_sending_key
```


## Update version
Increment the version in the `src/main/resources/application.properties` and `pom.xml` files.


## Deploying

[For internal notify use only]
You'll need to make sure you have the `notify-gpg-key` private key available locally.

```shell
gpg --import <(notify-pass show credentials/concourse/gpg-key)
```

Then, from the notifications-java-client directory, run

```shell
export MAVEN_CENTRAL_PASSWORD=$(notify-pass show credentials/maven-central/password)
./deploy.sh
```
