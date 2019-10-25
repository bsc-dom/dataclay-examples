
# HelloPeople example in Python

The HelloPeople is a simple application that is used for demonstration and as an
example of dataClay objects.

This example will give you a simple application with two classes and relationship 
between objects of them. It should be easy to increase the complexity and play around
it a little bit.

## Model registration

Create a new account and datacontract, matching the values used in `cfgfiles/session.properties`:

    $ dataclaycmd NewAccount user s3cr3tp4ssw0rd
    $ dataclaycmd NewDataContract user s3cr3tp4ssw0rd forthepeople user

Register your model:

    $ dataclaycmd NewModel user s3cr3tp4ssw0rd model_hp ./model_hp python

Retrieve the stubs for the registered classes:

    $ dataclaycmd GetStubs user s3cr3tp4ssw0rd model_hp ./stubs


## Running the application

The application can now be run:

    $ ./hellopeople.py
