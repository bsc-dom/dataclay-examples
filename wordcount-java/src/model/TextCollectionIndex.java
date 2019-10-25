package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class TextCollectionIndex extends DataClayObject {
	ArrayList<TextCollection> textCollections;
	int nextCollection;

	public TextCollectionIndex(ArrayList<TextCollection> newTextCollections) {
		textCollections = new ArrayList<TextCollection>();
		textCollections.addAll(newTextCollections);
		nextCollection = 0;
	}

	public ArrayList<Text> getTexts() {
		ArrayList<Text> result = new ArrayList<Text>();
		for (TextCollection tc : textCollections) {
			result.addAll(tc.getTexts());
		}
		return result;
	}

	public int getSize() {
		int result = 0;
		for (final TextCollection tc : textCollections) {
			result += tc.getSize();
		}
		return result;
	}

	public List<String> addTextsFromPath(final String filePath) throws IOException {
		List<String> result;
		final File f = new File(filePath);
		if (f.isDirectory()) {
			result = addTextsFromDir(filePath);
		} else {
			result = new ArrayList<>();
			final String newTitle = addTextFromFile(filePath);
			result.add(newTitle);
		}
		return result;
	}

	public String addTextFromFile(final String filePath) throws IOException {
		if (nextCollection == textCollections.size()) {
			nextCollection = 0;
		}
		final TextCollection tc = textCollections.get(nextCollection);
		nextCollection++;
		final String textTitle = tc.addTextFromFile(filePath);
		return textTitle;
	}

	public List<String> addTextsFromDir(final String dirPath) throws IOException {
		final File dir = new File(dirPath);
		final List<String> result = new ArrayList<>();
		for (final File f : dir.listFiles()) {
			final String addedText = addTextFromFile(f.getAbsolutePath());
			result.add(addedText);
		}
		return result;
	}

	public void load() {

	}
}
