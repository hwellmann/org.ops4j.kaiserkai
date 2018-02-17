Kaiserkai: A Java Docker Registry with Better Garbage Collection
================================================================

## What is it?

Kaiserkai is a Java implementation of the [Docker Registry V2 API](https://docs.docker.com/registry/spec/api/).

Its filesystem storage layout is compatible with the one used by Docker's `registry:2`.

The project uses WildFly 11 and fabric8's `docker-maven-plugin` and `docker-client` library.

## Why bother?

Kaiserkai was created to solve the garbage collection problem when running a private Docker
registry.

The REST API is extended with a new `POST /_gc` method which performs garbage collection
without shutdown, only locking the registry for push operations.


## Build from Source

### Prerequisites

* Java 8 
* Maven 3.5.0
* Docker 17.06.0-ce

Older versions of Maven and Docker may work, these are the ones used for development and testing.

### Commands

    git clone git://github.com/hwellmann/org.ops4j.kaiserkai.git
    cd org.ops4j.kaiserkai
    mvn clean install

## Run

    $ docker run -d -v /path/to/registry:/opt/wildfly/kaiserkai -p 8080:8080 ops4j/kaiserkai:0.1.0-SNAPSHOT
    $ curl -u admin:admin http://localhost:8080/v2/_catalog    
    {"repositories":[]}
    