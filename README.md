
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
