package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class TextStats extends DataClayObject implements Serializable {
	public HashMap<String, Integer> currentWordCount;
	public boolean debug;

	public TextStats() {
		currentWordCount = new HashMap<>();
		debug = false;
		// System.out.println("[ TextStats ] Call to empty constructor (for COMPSs, to
		// fill results later)");
	}

	public TextStats(final HashMap<String, Integer> newWordCount, final boolean doDebug) {
		currentWordCount = new HashMap<>();
		currentWordCount.putAll(newWordCount);
		debug = doDebug;
		// System.out.println("[ TextStats ] Call to constructor for wordcount");
	}

	public void setCurrentWordCount(final HashMap<String, Integer> newWordCount) {
		currentWordCount.putAll(newWordCount);
	}

	public HashMap<String, Integer> getCurrentWordCount() {
		return currentWordCount;
	}

	public int getSize() {
		return currentWordCount.size();
	}

	public boolean getDebug() {
		return debug;
	}

	public void setDebug() {
		debug = true;
	}

	public void mergeWordCounts(final TextStats newWordCount) {
		long start = 0, end;
		if (debug) {
			start = System.currentTimeMillis();
		}
		final HashMap<String, Integer> wordCountToMerge = newWordCount.getCurrentWordCount();
		for (final Entry<String, Integer> entry : wordCountToMerge.entrySet()) {
			final String word = entry.getKey();
			final Integer count = entry.getValue();
			final Integer curCount = currentWordCount.get(word);
			if (curCount == null) {
				currentWordCount.put(word, count);
			} else {
				currentWordCount.put(word, curCount + count);
			}
		}
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println("Merged result in " + (end - start) + " millis");
		}
	}

	public HashMap<String, Integer> getSummary(final int maxEntries) {
		int i = 0;
		final HashMap<String, Integer> result = new HashMap<>();
		for (final Entry<String, Integer> curEntry : currentWordCount.entrySet()) {
			result.put(curEntry.getKey(), curEntry.getValue());
			i++;
			if (i == maxEntries) {
				break;
			}
		}
		return result;
	}

	public void load() {

	}

}
