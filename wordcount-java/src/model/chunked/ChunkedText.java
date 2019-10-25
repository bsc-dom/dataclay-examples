package model.chunked;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import model.TextStats;
import dataclay.DataClayObject;

@SuppressWarnings("serial")
public class ChunkedText extends DataClayObject implements Iterable<String> {
	int maxChunkSize;
	ArrayList<Chunk> chunks;
	boolean debug;
	String title;

	public ChunkedText(String textTitle, int nodeSize, boolean doDebug) {
		this.title = textTitle;
		this.maxChunkSize = nodeSize;
		this.debug = doDebug;
		this.chunks = new ArrayList<>();
	}

	@Override
	public Iterator<String> iterator() {
		// FIXME WordIterator is not working properly
		throw new UnsupportedOperationException();
		// return new WordIterator(chunks, maxChunkSize);
	}

	public boolean addWord(String newWord) {
		Chunk wc = null;
		boolean createNewChunk = false;
		int curSize = chunks.size();
		if (curSize == 0) {
			createNewChunk = true;
		} else {
			wc = chunks.get(curSize - 1);
			if (wc.getSize() == maxChunkSize) {
				createNewChunk = true;
			}
		}
		if (createNewChunk) {
			wc = new Chunk();
			wc.addWord(newWord);
			// wc = (Chunk) wc.newProxy(true, this.getLocation());
			chunks.add(wc);
		} else {
			wc.addWord(newWord);
		}
		return false;
	}

	public void addWords(String filePath) throws IOException {
		File file = new File(filePath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		int addedWords = 0;
		long totalSize = file.length();
		System.out
				.println("[ Text ] Parsing file " + file.getName() + " of size " + totalSize / 1024 / 1024 + " MB ...");
		long init = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] wordsLine = line.split(" ");
			for (String word : wordsLine) {
				addWord(word);
				addedWords++;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("[ Text ] Added : " + addedWords + " words in " + (end - init) + " ms");

		br.close();
		fr.close();
	}

	public int getSize() {
		int curSize = chunks.size();
		if (curSize == 0) {
			return 0;
		}
		int totalSize = chunks.get(curSize - 1).getSize();
		if (curSize > 1) {
			totalSize += (maxChunkSize * (curSize - 1));
		}
		return totalSize;
	}

	public int getNumChunks() {
		return chunks.size();
	}

	public String getTitle() {
		return title;
	}

	public TextStats wordCount(boolean persistStats) {
		long start = 0, end = 0;
		if (debug) {
			start = System.currentTimeMillis();
		}

		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (Chunk chunk : this.chunks) {
			ArrayList<String> words = chunk.getWords();
			for (String word : words) {
				Integer curCount = result.get(word);
				if (curCount == null) {
					result.put(word, 1);
				} else {
					result.put(word, curCount + 1);
				}
			}
		}

		if (debug) {
			end = System.currentTimeMillis();
			System.out.println("[ Text ] Computed text " + getID() + " in " + (end - start) + " millis");
		}
		TextStats textStats = new TextStats(result, debug);
		if (persistStats) {
			textStats.makePersistent();
		}
		if (debug) {
			long persistedIn = System.currentTimeMillis() - end;
			System.out.println("[ Text ] TextStats " + textStats.getID() + " persisted in " + persistedIn + " millis");
		}
		return textStats;
	}

	public int wordCountNotComputing() {
		int i = 0;
		for (Chunk chunk : chunks) {
			ArrayList<String> words = chunk.getWords();
			Iterator<String> it = words.iterator();
			while (it.hasNext()) {
				it.next();
				i++;
			}
		}
		return i;
	}

	public ChunkedText cloneChunkedText(String textTitle) {
		ChunkedText newText = new ChunkedText(textTitle, maxChunkSize, debug);
		newText.makePersistent(textTitle);
		for (Chunk chunk : chunks) {
			newText.addChunk(chunk);
		}
		return newText;
	}

	public void addChunk(Chunk chunk) {
		Chunk newChunk = new Chunk();
		newChunk.addWords(chunk.getWords());
		chunks.add(newChunk);
	}

}
