package model.chunked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class WordIterator implements Iterator<String> {

	public ArrayList<Chunk> chunks;
	public int curChunk;
	public int curPositionInChunk;
	public int maxChunkSize;

	public WordIterator(final ArrayList<Chunk> chunksToIterate, final int theMaxChunkSize) {
		chunks = chunksToIterate;
		curChunk = 0;
		curPositionInChunk = 0;
		maxChunkSize = theMaxChunkSize;
	}

	@Override
	public boolean hasNext() {
		if (curChunk == chunks.size()) {
			return false;
		}
		if (curChunk == (chunks.size() - 1)) {
			final Chunk chunk = chunks.get(curChunk);
			return curPositionInChunk < (chunk.getSize());
		}
		return true;
	}

	@Override
	public String next() {
		if (curChunk == chunks.size()) {
			throw new NoSuchElementException();
		}
		final Chunk chunk = chunks.get(curChunk);
		if (curChunk == chunks.size() - 1) {
			if (curPositionInChunk == chunk.getSize()) {
				throw new NoSuchElementException();
			}
		}
		final String result = chunks.get(curChunk).getWords().get(curPositionInChunk);
		curPositionInChunk++;
		if (curPositionInChunk == maxChunkSize) {
			curChunk++;
			curPositionInChunk = 0;
		}
		return result;
	}

	@Override
	public void remove() {
		// NOT IMPLEMENTED

	}

	public void load() {

	}
}
