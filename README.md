
# dataClay examples

In this repository you will find different simple examples, which should be able to
showcase the basic features of dataClay and give you some starting point for your
application.

## Before we begin

You need to have a way to start dataClay --a `docker-compose` is the fastest
and most reproducible way to do it.

The examples assume that dataClay will be reachable through 11034 port.

You should have the dataClay tool available, which will be used in the registration step
of all the examples.

### Java

Examples will assume that `$CLASSPATH` is populated with all the necessary paths for dataClay
library to work.

### Python

We recommend to use a virtual environment under which the dataClay package is installed. The 
`dataclaycmd` (dataClay tool) will use it, as well as the main application.

### `dataclaycmd` usage

The easiest approach to using the dataClay tool (aka `dataclaycmd`) is using the image [`bscdataclay/client`](https://hub.docker.com/r/bscdataclay/client). This examples try to be general and don't go into deployment details, but in most scenarios you will need to prepare the following alias for `dataclaycmd` to work:

    alias dataclaycmd="docker run \
                           -v \$PWD/cfgfiles/:/usr/src/dataclay/client/cfgfiles/:ro \
                           -v \$PWD/model-bin/:/usr/src/dataclay/client/model-bin/ \
                           -v \$PWD/stubs/:/usr/src/dataclay/client/stubs/ \
                           bscdataclay/client:2.0"

**IMPORTANT**: Don't forget to escape the `$PWD` --the backslash is mandatory. If you have some path error or some anomalous behavior in your shell, put the explicit absolute full path for the scenario you are running.

Note that you may need to also add the `--network=<docker network>` parameter in order for the dataClay tool to be able to reach the dataClay services. For example, if you have started dataClay through a `docker-compose` command, you can check the name of the networks with the command `docker network ls` and you will see a network named `<folder name>_default` which you can use. You should update the `cfgfiles/client.properties` according to your specific topology.

## Following the examples

Each example folder has a README which explains the steps and the basic characteristics
of each application.

You will see that there is a common basic structure which comprehends:

  1. Model registration
  2. Building the application (for Java examples)
  3. Running the application


## Cleanup

You may want to cleanup your example folder if you change things or want to try again. The following
folders should be removed before starting again:

  - `model-bin` which is the path where Java examples compile their model classes.
  - `bin` which is the path where Java examples compile its main application.
  - `stubs` which is the folder where all the example retrieve the stubs.
