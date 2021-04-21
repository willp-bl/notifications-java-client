# Contributing

Pull requests are welcome.

## Unit tests

On a command line build the project with Maven to run the unit tests:

```shell
> mvn clean test
```


## Integration Tests for Notify dev team

You will need to source an environment.sh file. You can find a copy in notify-pass credentials/client-integration-tests. You will also need to include the following variable.

```shell
export NOTIFY_API_URL=https://api.notify.works
```

Use the following command to run the integration tests:
```shell
mvn clean verify
```
NOTE: you'll get a build failure `[ERROR] Failed to execute goal org.apache.maven.plugins:maven-gpg-plugin:1.5:sign (sign-artifacts) on project notifications-java-client: Exit code: 2 -> [Help 1]
` unless you import the gpg key, however, the you should see if the integration tests failed in the log message before the error. Import the key with this command:

```shell
gpg --import <(notify-pass show credentials/concourse/gpg-key)
```

## Update version
Increment the version in the `src/main/resources/application.properties` and `pom.xml` files.

## Deploying

Run the `release-java-client` concourse job after the `build-and-deploy-tech-docs-preview` is complete to publish the java client to maven central.