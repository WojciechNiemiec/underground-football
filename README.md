# Underground Football

Backend application to enable organisation of amateur football matches and leagues.
The goal is to provide simple and easy to use platform for organisers and players supporting:
- managing costs of match infrastructure
- managing player settlements

## Build and run

First build the project with:

```bash
mvn clean package
```

The application expects mongo on port 27017.
```bash
docker run -d -p 27017:27017 --name mongodb mongo
```

Running the application locally involves starting function emulator
```bash
mvn function:run
```

## API Demonstration

The API can be explored using Bruno collections located in `bruno` directory.
Follow instructions in `bruno/README.md` to set up Bruno and import collections.

## Cloud deployment

To be defined...

## General guidelines

The application is written using Java Jigsaw modules.
Each module is represented by different maven module.
Cyclic dependencies between modules are not allowed.
Domain modules like Game and Wallet should be designed in framework-agnostic way.
The stack should be preserved small and simple.

## Micronaut 4.10.4 Documentation

- [User Guide](https://docs.micronaut.io/4.10.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
