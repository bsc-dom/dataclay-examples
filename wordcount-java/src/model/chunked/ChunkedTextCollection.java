package model.chunked;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataclay.DataClayObject;
// import util.ids.BackendID;

@SuppressWarnings("serial")
public class ChunkedTextCollection extends DataClayObject {
	String textPrefix;
	ArrayList<ChunkedText> texts;
	boolean debug;

	public ChunkedTextCollection(String prefixForTextsInCollection, boolean doDebug) {
		this.texts = new ArrayList<>();
		this.textPrefix = prefixForTextsInCollection;
		this.debug = doDebug;
	}

	public void setDebug(boolean newDebug) {
		debug = newDebug;
	}

	public String getTextPrefix() {
		return textPrefix;
	}

	public ArrayList<ChunkedText> getTexts() {
		return texts;
	}

	public int getTotalTexts() {
		return texts.size();
	}

	public List<String> addTextsFromPath(final String path, final int chunkSize) throws IOException {
		List<String> result = new ArrayList<>();
		File dir = new File(path);
		if (dir.isFile()) {
			result.add(addTextFromFile(path, chunkSize));
		}
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					result.add(addTextFromFile(file.getAbsolutePath(), chunkSize));
				}
			}
		}
		return result;
	}

	public String addTextFromFile(final String filePath, final int chunkSize) throws IOException {
		String textTitle = textPrefix + ".file" + (texts.size() + 1);
		ChunkedText t = new ChunkedText(textTitle, chunkSize, debug);
		t.makePersistent(textTitle);
		texts.add(t);
		t.addWords(filePath);
		if (debug) {
			System.out.println("[LOG] Created text object with " + t.getSize() + " words, grouped in "
					+ t.getNumChunks() + " chunks of size " + chunkSize);
		}
		return textTitle;
	}

	public String addTextFromExisting(final ChunkedText existing, final int chunkSize) {
		String textTitle = textPrefix + ".file" + (texts.size() + 1);
		ChunkedText clonedText = existing.cloneChunkedText(textTitle);
		texts.add(clonedText);
		return textTitle;
	}
}
