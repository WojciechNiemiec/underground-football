## Micronaut 4.2.3 Documentation

- [User Guide](https://docs.micronaut.io/4.2.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.2.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.2.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

# Micronaut and Google Cloud Function

## Deploying the function

First build the function with:

```bash
$ ./mvnw clean package
```

Then `cd` into the `target` directory (deployment has to be done from the location where the JAR lives):

```bash
$ cd target
```

Now run:

```bash
$ gcloud functions deploy test-app --entry-point test.app.Function --runtime java17 --trigger-http
```

Choose unauthenticated access if you don't need auth.

To obtain the trigger URL do the following:

```bash
$ YOUR_HTTP_TRIGGER_URL=$(gcloud functions describe test-app --format='value(httpsTrigger.url)')
```

You can then use this variable to test the function invocation:

```bash
$ curl -i $YOUR_HTTP_TRIGGER_URL/test-app
```

- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)
## Feature google-cloud-function documentation

- [Micronaut Google Cloud Function documentation](https://micronaut-projects.github.io/micronaut-gcp/latest/guide/index.html#simpleFunctions)


## Feature maven-enforcer-plugin documentation

- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)
