
# WordCount example in Java

The WordCount is a simple application that counts the number of appearances of each word in a text.

You will see two variations of this example: A simple one and the _chunked_ one. The chunked variant
will store the text in several _chunks_, which is more suitable for distributed environments.

## Model registration

Create a new account and datacontract, matching the values used in `cfgfiles/session.properties`:

    $ dataclaycmd NewAccount WordcountUser WordcountPass
    $ dataclaycmd NewDataContract WordcountUser WordcountPass WordcountDS WordcountUser

Build and register your model:

    $ mkdir model-bin
    $ javac -cp $CLASSPATH src/model/*.java -d ./model-bin
    $ javac -cp $CLASSPATH src/producer/*.java -d ./model-bin
    $ dataclaycmd NewModel WordcountUser WordcountPass WordcountNS ./model-bin java

Retrieve the stubs for the registered classes:

    $ dataclaycmd GetStubs WordcountUser WordcountPass WordcountNS ./stubs


## Building the application

Now that you have the stubs you will be able to build your application using those. It's just 
like a regular application but now it includes the stubs folder:

    $ mkdir bin
    $ javac -cp ./stubs:$CLASSPATH src/consumer/*.java -d bin/
    $ javac -cp ./stubs:$CLASSPATH src/producer/*.java -d bin/

## Running the application

### Generating the text

Just run the TextCollection generator:

    $ java -cp ./stubs:./bin:$CLASSPATH producer.TextCollectionGen

### Performing the word counting

The consumer's `Wordcount` is responsible for the counting:

    $ java -cp ./stubs:./bin:$CLASSPATH consumer.Wordcount

## Running the application in _chunked_ mode

You can try the chunked variant by using the appropriate counterparts, both in the
**producer** and **consumer** steps:

    $ java -cp ./stubs:./bin:$CLASSPATH producer.TextChunkedCollectionGen
    $ java -cp ./stubs:./bin:$CLASSPATH consumer.WordcountChunked
