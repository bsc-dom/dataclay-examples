package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class Text extends DataClayObject {
	String title;
	List<String> words1;
	boolean debug;

	// Constructor required for COMPSs
	public Text() {
		System.out.println("[ Text ] Call to empty constructor (for COMPSs).");
	}

	// Constructor for TextCollection to build new text objects
	public Text(String newTitle, boolean doDebug) {
		this.title = newTitle;
		this.words1 = new ArrayList<>();
		this.debug = doDebug;
		if (debug) {
			System.out.println(
					"[ Text ] Call to real constructor to create a text object with " + words1.getClass().getName());
		}
	}

	public String getTitle() {
		return title;
	}

	public void addWords(final String filePath) throws IOException {
		final File file = new File(filePath);
		final FileReader fr = new FileReader(file);
		final BufferedReader br = new BufferedReader(fr);
		String line;
		int addedWords = 0;
		final long totalSize = file.length();
		System.out
				.println("[ Text ] Parsing file " + file.getName() + " of size " + totalSize / 1024 / 1024 + " MB ...");
		final long init = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			final String[] wordsLine = line.split(" ");
			for (final String word : wordsLine) {
				words1.add(word);
				addedWords++;
			}
		}
		final long end = System.currentTimeMillis();
		System.out.println("[ Text ] Added : " + addedWords + " words in " + (end - init) + " ms");

		br.close();
		fr.close();
	}

	public TextStats wordCount(final boolean persistStats) {
		long start = 0, end = 0;
		if (debug) {
			start = System.currentTimeMillis();
		}
		final HashMap<String, Integer> result = new HashMap<>();
		final Iterator<String> it = words1.iterator();
		while (it.hasNext()) {
			final String word = it.next();
			final Integer curCount = result.get(word);
			if (curCount == null) {
				result.put(word, 1);
			} else {
				result.put(word, curCount + 1);
			}
		}
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println("[ Text ] Computed text " + getID() + " in " + (end - start) + " millis");
		}
		final TextStats textStats = new TextStats(result, debug);
		if (persistStats) {
			textStats.makePersistent(true, null);
		}
		if (debug) {
			final long persistedIn = System.currentTimeMillis() - end;
			System.out.println("[ Text ] TextStats " + textStats.getID() + " persisted in " + persistedIn + " millin");
		}
		return textStats;
	}

	public void wordCountMakingPersistentReturningIntAlias(final int statsAlias) {
		long start = 0, end = 0;
		if (debug) {
			start = System.currentTimeMillis();
		}
		final HashMap<String, Integer> result = new HashMap<>();
		final Iterator<String> it = words1.iterator();
		while (it.hasNext()) {
			final String word = it.next();
			final Integer curCount = result.get(word);
			if (curCount == null) {
				result.put(word, 1);
			} else {
				result.put(word, curCount + 1);
			}
		}
		if (debug) {
			end = System.currentTimeMillis();
			System.out.println("[ Text ] Computed text " + getID() + " in " + (end - start) + " millis");
		}
		final TextStats textStats = new TextStats(result, debug);
		textStats.makePersistent("" + statsAlias);
		if (debug) {
			final long persistedIn = System.currentTimeMillis() - end;
			System.out.println("[ Text ] TextStats " + textStats.getID() + " persisted in " + persistedIn + " millin");
		}
	}

	public int wordCountNotComputing() {
		int i = 0;
		final Iterator<String> it = words1.iterator();
		while (it.hasNext()) {
			it.next();
			i++;
		}
		return i;
	}

	public void load() {

	}
}
