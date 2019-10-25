#!/usr/bin/env python2
import sys

from dataclay.api import init, finish

# Init dataClay session
init()

from model_hp.classes import Person, People


class Attributes(object):
    pass


def usage():
    print("Usage: hellopeople.py <colname> <personName> <personAge>")


def init_attributes(attributes):
    if len(sys.argv) != 4:
        print("ERROR: Missing parameters")
        usage()
        exit(2)
    attributes.collection = sys.argv[1]
    attributes.p_name = sys.argv[2]
    attributes.p_age = int(sys.argv[3])


if __name__ == "__main__":
    attributes = Attributes()
    init_attributes(attributes)

    # Create people object if it does not exist
    try:
        # Trying to retrieve it using alias
        people = People.get_by_alias(attributes.collection)
    except Exception:
        print("[LOG] Creating people's object with alias %s" \
            % attributes.collection)
        people = People()
        people.make_persistent(alias=attributes.collection)

    # Add new person to people
    person = Person(attributes.p_name, attributes.p_age)
    person.make_persistent()
    people.add(person)

    print("[LOG] People object contains:")
    print(people)

    # Close session
    finish()
