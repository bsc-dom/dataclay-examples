
# HelloPeople example in Java

The HelloPeople is a simple application that is used for demonstration and as an
example of dataClay objects.

This example will give you a simple application with two classes and relationship 
between objects of them. It should be easy to increase the complexity and play around
it a little bit.

## Model registration

Create a new account and datacontract, matching the values used in `cfgfiles/session.properties`:

    $ dataclaycmd NewAccount HelloPeopleUser HelloPeoplePass
    $ dataclaycmd NewDataContract HelloPeopleUser HelloPeoplePass HelloPeopleDS HelloPeopleUser

Build and register your model:

    $ mkdir model-bin
    $ javac -cp $CLASSPATH src/model/*.java -d ./model-bin
    $ dataclaycmd NewModel HelloPeopleUser HelloPeoplePass HelloPeopleNS ./model-bin java

Retrieve the stubs for the registered classes:

    $ dataclaycmd GetStubs HelloPeopleUser HelloPeoplePass HelloPeopleNS ./stubs


## Building the application

Now that you have the stubs you will be able to build your application using those. It's just 
like a regular application but now it includes the stubs folder:

    $ mkdir bin
    $ javac -cp ./stubs:$CLASSPATH src/app/*.java -d bin/

## Running the application

The application can now be run:

    $ java -cp ./stubs:./bin:$CLASSPATH app.HelloPeople
