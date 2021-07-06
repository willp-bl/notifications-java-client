# Contributing

Pull requests are welcome.

## Setting Up

### Docker container

This app uses dependencies that are difficult to install locally. In order to make local development easy, we run app commands through a Docker container. Run the following to set this up:

```shell
make bootstrap-with-docker
```

## Tests

### Unit tests

To run the unit tests:

```shell
make test-with-docker
```

### Integration Tests

You will need to source an environment.sh file. You can find a copy in notify-pass credentials/client-integration-tests. You will also need to include the following variable.

```shell
export NOTIFY_API_URL=https://api.notify.works
```

To run the integration tests:

```shell
make integration-test-with-docker
```

NOTE: you'll get a build failure `[ERROR] Failed to execute goal org.apache.maven.plugins:maven-gpg-plugin:1.5:sign (sign-artifacts) on project notifications-java-client: Exit code: 2 -> [Help 1]
` however, you should see if the integration tests passed in the log message before the error.

## Update version

Increment the version in the `src/main/resources/application.properties` and `pom.xml` files.

## Deploying

Concourse will release and publish the java client to maven central.
