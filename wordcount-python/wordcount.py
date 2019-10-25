#!/usr/bin/env python
import sys

from dataclay import api, getRuntime

api.init()

from wordcount import TextCollection, TextStats


def count_words(text_collection):
    result = TextStats(dict())
    result.make_persistent()

    texts = text_collection.texts
    total_n_texts = len(texts)

    print("Ready to count words from %d different texts" % total_n_texts)

    for text in texts:
        partialResult = text.word_count()
        result.merge_word_counts(partialResult)

    return result


if __name__ == "__main__":
    if len(sys.argv) > 2:
        print ("""
    Usage:

        ./wordcount.py [text_collection_alias]
    """)
        exit(1)
    elif len(sys.argv) == 2:
        alias = sys.argv[1]
    else:
        alias = "Words"

    print(" ###############################")
    print(" # Start WordCount application #")
    print(" ###############################")

    words = TextCollection.get_by_alias(alias)

    res = count_words(words)

    print(" # WordCount finished")
    print(" # Top 10:\n %s" % res.top_words(10))
