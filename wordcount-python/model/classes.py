from dataclay import DataClayObject, dclayMethod
from dataclay.contrib.dummy_pycompss import task


class TextCollection(DataClayObject):
    """Collection of Text objects.

    @ClassField texts list<WordCount.classes.Text>
    """
    @dclayMethod()
    def __init__(self):
        self.texts = list()

    @dclayMethod(text_file="str")
    def add_text_from_file(self, text_file):
        """Load a text from a file into a Text object."""
        text = Text()
        with open(text_file, "r") as f:
                for line in f.readlines():
                    text.add_words(line.strip().lower().translate(None, ".,\"'").split())

        text.make_persistent()
        self.texts.append(text)

    @dclayMethod(text="str")
    def add_text(self, text):
        """Load a text string into a Text object."""
        text_obj = Text()
        text_obj.add_words(text.strip().lower().translate(None, ".,\"'").split())
        text_obj.make_persistent()
        self.texts.append(text_obj)


class TextStats(DataClayObject):
    """Store WordCount-ing stats.

    Dictionary-like storage of word counting (also used for partial results).

    @ClassField current_word_count anything
    """
    @dclayMethod(init_dict="anything")
    def __init__(self, init_dict):
        self.current_word_count = dict(init_dict)

    @task(isModifier=False)
    @dclayMethod(target="WordCount.classes.TextStats")
    def merge_word_counts(self, target):
        dic1 = self.current_word_count
        dic2 = target.current_word_count

        # Basic merge (COMPSs verbatim)
        for k in dic2:
            if k in dic1:
                dic1[k] += dic2[k]
            else:
                dic1[k] = dic2[k]

        # self.current_word_count has been updated

    @dclayMethod(num_words=int, return_="anything")
    def get_summary(self, num_words):
        sorted_values = sorted((count, word) for word, count in self.current_word_count.iteritems())
        return dict((word, count) for count, word in sorted_values[-num_words:])

    @dclayMethod(return_=int)
    def get_total(self):
        return sum(self.current_word_count.values())


class Text(DataClayObject):
    """A "Text" (list of words).

    This object contains `words` (= list of words).

    @ClassField words anything
    """

    @dclayMethod()
    def __init__(self):
        # This will be initialized in persistent storage with populate_from_file
        self.words = list()

    @dclayMethod(words="anything")
    def add_words(self, words):
        """Add a list of words into this text's word list."""
        self.words.extend(words)

    @task(isModifier=False, returns=object)
    @dclayMethod(return_="WordCount.classes.TextStats")
    def word_count(self):
        partialResult = dict()
        for entry in self.words:
            if entry in partialResult:
                partialResult[entry] += 1
            else:
                partialResult[entry] = 1

        ret = TextStats(partialResult)
        return ret
