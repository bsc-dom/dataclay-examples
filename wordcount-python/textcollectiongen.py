#!/usr/bin/env python
import os
import sys
from itertools import cycle

from dataclay import api, getRuntime

api.init()

from wordcount import TextCollection


def generate_text(path, text_collection_alias="Words"):
    text_collection = TextCollection()
    text_collection.make_persistent(text_collection_alias)

    text_collection.add_text("Should do a Lorem Ipsum here, but instead I will repeat here some do and do nots and so on.")


if __name__ == "__main__":
    if len(sys.argv) < 2 or len(sys.argv > 3):
        print("""
    Usage:

        ./textcollectiongen.py [text_collection_alias]
    """)
        exit(1)

    print(" ###################################")
    print(" # Start TextCollection generation #")
    print(" ###################################")

    path = sys.argv[1]

    if len(sys.argv) == 3:
        generate_text(path, sys.argv[2])
    else:
        generate_text(path)

    print()
    print(" # TextCollection generated")
