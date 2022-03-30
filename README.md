# Basically Wordle. - Backend

[![Deploy Project](https://github.com/sopra-fs22-group-09/basically-wordle-backend/actions/workflows/deploy.yml/badge.svg)](https://github.com/sopra-fs22-group-09/basically-wordle-backend/actions/workflows/deploy.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-09_basically-wordle-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-09_basically-wordle-backend)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-09_basically-wordle-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-09_basically-wordle-backend)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-09_basically-wordle-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-09_basically-wordle-backend)

Wordle PvP Backend implementation with Spring Boot and GraphQL.

## Development
### Pre-requisites
- [Docker](https://docs.docker.com/get-docker/) with [Compose](https://docs.docker.com/compose/install/)
  - For Windows, go with [Docker Desktop](https://docs.docker.com/desktop/windows/install/)
### Development
Run the following command to start the PostgreSQL and Redis database:
```shell
$ docker-compose up -d
```

Set the `local` profile in the run configurations dialog:  
![img.png](screenshots/img_2.png)

You can now use IntelliJ run configurations to launch the server as usual.

To shut down the compose stack, run:
```shell
$ docker-compose down
```

### Auto-reload backend
Enable "Build project automatically":  
![Settings1](screenshots/img_1.png)
Enable "Allow auto-make to start even if developed application is currently running":  
![Settings2](screenshots/img.png)