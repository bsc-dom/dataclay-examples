package model.chunked;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class ChunkedTextCollectionIndex extends DataClayObject {
	ArrayList<ChunkedTextCollection> textCollections;
	int nextCollection;

	public ChunkedTextCollectionIndex(ArrayList<ChunkedTextCollection> newTextCollections) {
		textCollections = new ArrayList<ChunkedTextCollection>();
		textCollections.addAll(newTextCollections);
		nextCollection = 0;
	}

	public ArrayList<ChunkedText> getTexts() {
		ArrayList<ChunkedText> result = new ArrayList<>();
		for (ChunkedTextCollection tc : textCollections) {
			result.addAll(tc.getTexts());
		}
		return result;
	}

	public ArrayList<ChunkedTextCollection> getTextCollections() {
		return textCollections;
	}

	public int getTotalTexts() {
		int result = 0;
		for (ChunkedTextCollection tc : textCollections) {
			result += tc.getTotalTexts();
		}
		return result;
	}

	public List<String> addTextsFromPath(final String filePath, int nodeSize) throws IOException {
		List<String> result;
		File f = new File(filePath);
		if (f.isDirectory()) {
			result = addTextsFromDir(filePath, nodeSize);
		} else {
			result = new ArrayList<String>();
			String newTitle = addTextFromFile(filePath, nodeSize);
			result.add(newTitle);
		}
		return result;
	}

	public String addTextFromFile(final String filePath, int nodeSize) throws IOException {
		if (nextCollection == textCollections.size()) {
			nextCollection = 0;
		}
		ChunkedTextCollection tc = textCollections.get(nextCollection);
		nextCollection++;
		String textTitle = tc.addTextFromFile(filePath, nodeSize);
		return textTitle;
	}

	public List<String> addTextsFromDir(final String dirPath, int nodeSize) throws IOException {
		File dir = new File(dirPath);
		List<String> result = new ArrayList<String>();
		for (File f : dir.listFiles()) {
			String addedText = addTextFromFile(f.getAbsolutePath(), nodeSize);
			result.add(addedText);
		}
		return result;
	}
}
