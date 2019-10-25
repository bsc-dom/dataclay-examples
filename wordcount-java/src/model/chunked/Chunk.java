package model.chunked;

import java.util.ArrayList;

import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class Chunk extends DataClayObject {
	public ArrayList<String> wordsInChunk;

	public Chunk() {
		wordsInChunk = new ArrayList<>();
	}

	public int getSize() {
		return wordsInChunk.size();
	}

	public void addWord(final String newWord) {
		wordsInChunk.add(newWord);
	}

	public void addWords(final ArrayList<String> newWords) {
		wordsInChunk.addAll(newWords);
	}

	public ArrayList<String> getWords() {
		return wordsInChunk;
	}

	public void load() {

	}
}
