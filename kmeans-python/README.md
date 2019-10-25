
# k-Means example in Python

This application is the **Standard algorithm** (aka _naive k-means_) implementation 
of the k-Means clustering method.

It is a numerical application, with heavy use of `numpy` library, which shows how a typical
HPC application can be used. Note that the whole dataset is split into `Fragment` objects
which make this code suitable for distributed environments.

## Model registration

Create a new account and datacontract, matching the values used in `cfgfiles/session.properties`:

    $ dataclaycmd NewAccount KmeansUser KmeansPassword
    $ dataclaycmd NewDataContract KmeansUser KmeansPassword KmeansDS KmeansUser

Register your model:

    $ dataclaycmd NewModel KmeansUser KmeansPassword classes ./classes python

Retrieve the stubs for the registered classes:

    $ dataclaycmd GetStubs KmeansUser KmeansPassword classes ./stubs


## Running the application

The application can now be run:

    $ ./kmeans.py
