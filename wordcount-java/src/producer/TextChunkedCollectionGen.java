package producer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dataclay.api.Backend;
import dataclay.api.BackendID;
import dataclay.api.DataClay;
import model.chunked.ChunkedText;
import model.chunked.ChunkedTextCollection;
import model.chunked.ChunkedTextCollectionIndex;
// import dataclay.collections.DataClayArrayList;
import storage.StorageItf;

public class TextChunkedCollectionGen {
	static int timesContentsPerBackend = 1;

	public static boolean prepareForDebug = false;

	public static final AtomicInteger NUM_TEXTS_CREATED = new AtomicInteger(0);

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			printErrorUsage();
			return;
		}

		if (!setOptionalArguments(args)) {
			printErrorUsage();
			return;
		}

		StorageItf.init(args[0]);
		for (Backend b : DataClay.getBackends().values()) {
			System.out.println("Backend " + b.getName() + " : " + b.getHostname() + ":" + b.getPort());
		}
		final String textColAlias = args[1];
		final String remotePath = args[2];
		final int maxChunkSize = new Integer(args[3]);

		ChunkedTextCollectionIndex textCollectionIndex;
		final ArrayList<ChunkedTextCollection> tcIndex = new ArrayList<ChunkedTextCollection>();
		int id = 1;

		// 1 collection per backend
		for (BackendID locID : DataClay.getJBackends().keySet()) {
			String prefixForChunkedTexts = textColAlias + id;
			ChunkedTextCollection tc = new ChunkedTextCollection(prefixForChunkedTexts, prepareForDebug);
			tc.makePersistent(prefixForChunkedTexts, locID);
			System.out.println("[LOG] Collection created at " + tc.getLocation());
			tcIndex.add(tc);
			id++;
		}
		textCollectionIndex = new ChunkedTextCollectionIndex(tcIndex);
		textCollectionIndex.makePersistent(textColAlias);
		System.out.println("[LOG] Created new collection index");
		System.out.println("[LOG] Collection index located at " + textCollectionIndex.getLocation());

		// 1 executor per backend (per collection)
		ExecutorService executor = Executors.newFixedThreadPool(tcIndex.size());

		for (final ChunkedTextCollection tc : tcIndex) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println("[LOG] Running thread " + Thread.currentThread().getId()
							+ " for objects in collection " + tc.getID());
					try {
						List<String> addTexts = tc.addTextsFromPath(remotePath, maxChunkSize);
						for (int i = 0; i < addTexts.size(); i++) {
							NUM_TEXTS_CREATED.incrementAndGet();
						}
						for (int i = 1; i < timesContentsPerBackend; i++) {
							for (String alias : addTexts) {
								ChunkedText textOrig = (ChunkedText) ChunkedText.getByAlias(alias);
								String textFromCopy = tc.addTextFromExisting(textOrig, maxChunkSize);
								ChunkedText textCopy = (ChunkedText) ChunkedText.getByAlias(ChunkedText.class.getName(),
										textFromCopy);
								System.out.println("[LOG] New text FROM COPY " + textFromCopy + "[id: "
										+ textCopy.getID() + "] @ [" + textCopy.getLocation() + "] with "
										+ textCopy.getNumChunks() + " chunks of at most " + maxChunkSize + " elements");
								NUM_TEXTS_CREATED.incrementAndGet();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			System.out.println("[LOG] Executor waits 5 seconds");
			Thread.sleep(5000);
		}

		System.out.println("[LOG] Updated collection. Now it has " + textCollectionIndex.getTotalTexts() + " texts.");
		System.out.println("[LOG] Total texts " + NUM_TEXTS_CREATED.get() + " texts.");

		StorageItf.finish();

	}

	private static boolean setOptionalArguments(String[] args) {
		for (int argIndex = 4; argIndex < args.length;) {
			String arg = args[argIndex++];
			if (arg.equals("-t")) {
				timesContentsPerBackend = new Integer(args[argIndex++]);
				if (timesContentsPerBackend <= 0) {
					System.err.println("Bad argument. TimesPerFile must be greater than zero");
					return false;
				}
			} else if (arg.equals("-debug")) {
				prepareForDebug = true;
				if (prepareForDebug) {
					System.out.println("[WARNING] This will cause debugging messages in Data Service nodes.");
				}
			} else {
				printErrorUsage();
				return false;
			}
		}
		return true;
	}

	private static void printErrorUsage() {
		System.err.println("Bad arguments. Usage: \n\n" + TextChunkedCollectionGen.class.getName()
				+ " <config_properties> <text_col_alias> <remote_path> <chunksize> "
				+ " [-n <nodes_per_level> -c <chunk_size>] [-t <times_file>] [-debug] [-persiststats] \n");
	}

}