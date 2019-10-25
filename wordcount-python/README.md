
# Wordcount example in Python

The WordCount is a simple application that counts the number of appearances of each word in a text.

## Model registration

Create a new account and datacontract, matching the values used in `cfgfiles/session.properties`:

    $ dataclaycmd NewAccount WCuser WCpassword
    $ dataclaycmd NewDataContract WCuser WCpassword WCds WCuser

Register your model:

    $ dataclaycmd NewModel WCuser WCpassword wordcount ./classes python

Retrieve the stubs for the registered classes:

    $ dataclaycmd GetStubs WCuser WCpassword wordcount ./stubs


## Running the application

You can first generate the texts with:

    $ ./textcollectiongen.py

And then you can count the words with:

    $ ./wordcount.py
