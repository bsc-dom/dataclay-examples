package model;

import java.io.IOException;
import java.util.ArrayList;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class TextCollection extends DataClayObject {
	String textPrefix;
	ArrayList<Text> texts;
	boolean debug;

	public TextCollection(String prefixForTextsInCollection, boolean doDebug) {
		this.texts = new ArrayList<>();
		this.textPrefix = prefixForTextsInCollection;
		this.debug = doDebug;
	}

	public String getTextPrefix() {
		return textPrefix;
	}

	public ArrayList<Text> getTexts() {
		return texts;
	}

	public int getSize() {
		return texts.size();
	}

	public String addTextFromFile(final String filePath) throws IOException {
		String textTitle = textPrefix + ".file" + (texts.size() + 1);
		Text t = new Text(textTitle, debug);
		t.makePersistent(textTitle);
		texts.add(t);
		t.addWords(filePath);
		return textTitle;
	}
}
