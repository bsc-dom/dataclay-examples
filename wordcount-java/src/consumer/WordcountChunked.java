package consumer;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import dataclay.api.DataClay;
import dataclay.commonruntime.ClientManagementLib;
import dataclay.paraver.HostType;
import dataclay.paraver.Paraver;
import dataclay.paraver.ParaverEventType;
import dataclay.util.Configuration;
import es.bsc.compss.api.COMPSs;
import model.TextStats;
import model.chunked.ChunkedText;
import model.chunked.ChunkedTextCollectionIndex;
import storage.StorageItf;

public class WordcountChunked {
	/** Default session properties file. */
	public static String configPropertiesFile = null;
	/** Fragment collection alias. */
	public static String textColAlias;
	/** Default execution times (to see cache effects). */
	public static int execTimes = 3;
	/** Final result. */
	public static TextStats finalResult = null;
	/** Default num of times that every text must be counted. */
	public static int timesPerText = 1;
	/** Persist partial stats or not. */
	public static boolean persistStats = false;
	/** Whether to debug or not. */
	public static boolean doDebug = false;
	/** Whether to perform the reduce stage or not. */
	public static boolean doReduce = true;
	/** Whether to trace or not. */
	public static boolean dcTracing = false;
	/** Path for worker paraver traces. */
	public static String pathForWorkerTraces = "";

	public static void main(String args[]) throws Exception {
		initAttributes(args);

		if (configPropertiesFile != null) {
			StorageItf.init(configPropertiesFile);
		}

		// Init texts to parse
		ChunkedTextCollectionIndex tc = (ChunkedTextCollectionIndex) ChunkedTextCollectionIndex
				.getByAlias(textColAlias);
		System.out.println("[LOG] Obtained TextCollection: " + textColAlias + " @ " + tc.getLocation() + " with "
				+ tc.getTotalTexts() + " texts.");
		ChunkedText[] textsToCount = totalTexts(tc, timesPerText);
		if (textsToCount.length == 0) {
			System.out.println("[ERROR] No text found");
		}

		for (int t = 1; t <= execTimes; t++) {
			if (t == 3) {
				if (dcTracing) {
					activateTracing();
				}
			}
			System.out.println("[LOG] Computing result");
			long startTime = System.currentTimeMillis();
			finalResult = run(textsToCount);
			COMPSs.barrier();
			long endTime = System.currentTimeMillis();
			System.out.println("[TIMER] RUN exec " + t + ": " + (endTime - startTime) + " ms");
			printResult();
			endTime = System.currentTimeMillis();
			System.out.println("[TIMER] RUN + RESULT exec " + t + ": " + (endTime - startTime) + " ms");
			if (t == 3) {
				if (dcTracing) {
					createTraces();
				}
			}
		}

		if (configPropertiesFile != null) {
			StorageItf.finish();
		}
	}

	private static void printResult() {
		if (!doReduce || finalResult == null) {
			System.out.println("[LOG] WARNING. Null result (and doReduce is set to " + doReduce);
		} else {
			System.out.println("[LOG] Getting info of final result ... ");
			System.out.print("[LOG] Final result is located at:  ");
			System.out.println(finalResult.getLocation());
			System.out.print("[LOG] Final result contains: ");
			System.out.println(finalResult.getSize() + " unique words.");
			System.out.println("[LOG] Result summary: ");
			System.out.println(finalResult.getSummary(10));
		}
	}

	// private static void run() {
	public static TextStats run(final ChunkedText[] texts) throws Exception {
		TextStats[] partialResult = new TextStats[texts.length];

		// MAP-WORDCOUNT
		for (int i = 0; i < texts.length; i++) {
			partialResult[i] = wordCountNewStats(texts[i], persistStats);
		}

		if (doReduce) {
			// REDUCE-MERGE
			LinkedList<Integer> q = new LinkedList<Integer>();
			for (int i = 0; i < texts.length; i++) {
				q.add(i);
			}
			int x = 0;
			while (!q.isEmpty()) {
				x = q.poll();
				int y;
				if (!q.isEmpty()) {
					y = q.poll();
					partialResult[x] = reduceTask(partialResult[x], partialResult[y]);
					q.add(x);
				}
			}
			return partialResult[x];
		} else {
			return null;
		}
	}

	// wordcount that returns an internally created (and made persistent) stats
	// object
	public static TextStats wordCountNewStats(ChunkedText text, boolean persistStats) {
		if (dcTracing) {
			Paraver.emitEvent(ParaverEventType.ENTER_LOCAL_METHOD, "TASK_wordCountNewStats");
		}
		TextStats result = text.wordCount(persistStats);
		if (dcTracing) {
			Paraver.emitEvent(ParaverEventType.EXIT_LOCAL_METHOD, "TASK_wordCountNewStats");
		}
		return result;
	}

	// reducetask assuming the result is updated in the first parameter
	// and returns it
	public static TextStats reduceTask(TextStats m1, TextStats m2) {
		if (dcTracing) {
			Paraver.emitEvent(ParaverEventType.ENTER_LOCAL_METHOD, "TASK_reduceTask");
		}
		m1.mergeWordCounts(m2);
		if (dcTracing) {
			Paraver.emitEvent(ParaverEventType.EXIT_LOCAL_METHOD, "TASK_reduceTask");
		}
		return m1;
	}

	/**
	 * Init texts to be counted
	 */
	private static ChunkedText[] totalTexts(ChunkedTextCollectionIndex tc, int timesPerText) throws Exception {
		ArrayList<ChunkedText> texts = tc.getTexts();
		int actualTexts = texts.size();
		int totalTexts = actualTexts * timesPerText;
		ChunkedText[] result = new ChunkedText[totalTexts];
		int index = 0;
		for (int j = 0; j < timesPerText; j++) {
			for (ChunkedText text : texts) {
				result[index] = text;
				index++;
			}
		}
		return result;
	}

	/**
	 * Check app arguments and initialize properties properly
	 * 
	 * @param args
	 *            arguments of the application
	 * @return false if arguments are wrong. true otherwise.
	 */
	private static void initAttributes(String[] args) {
		if (args.length < 1) {
			System.err.println("[ERROR] Bad arguments. " + getUsage());
			System.exit(1);
		}

		int argIndex = 0;
		while (argIndex < args.length) {
			String arg = args[argIndex++];
			if (arg.equals("-c")) {
				configPropertiesFile = args[argIndex++];
				File f = new File(configPropertiesFile);
				if (!f.exists() || f.isDirectory()) {
					System.err.println("Bad argument. Config file: " + configPropertiesFile + " does not exist.");
					System.exit(1);
				}
			} else if (arg.equals("-tc")) {
				textColAlias = args[argIndex++];
			} else if (arg.equals("-h")) {
				System.out.println("[HELP] " + getUsage());
				System.exit(0);
			} else if (arg.equals("-t")) {
				timesPerText = new Integer(args[argIndex++]);
			} else if (arg.equals("-debug")) {
				doDebug = true;
			} else if (arg.equals("-noreduce")) {
				doReduce = false;
			} else if (arg.equals("-persiststats")) {
				persistStats = true;
			} else if (arg.equals("-exectimes")) {
				execTimes = new Integer(args[argIndex++]);
			} else if (arg.equals("-dctracing")) {
				dcTracing = true;
			} else if (arg.equals("-workertraces")) {
				pathForWorkerTraces = args[argIndex++];
			} else {
				System.err.println("[ERROR] Bad argument: " + arg + ". " + getUsage());
				System.exit(1);
			}
		}

		System.out.println("-----------------------------------");
		System.out.println("Wordcount with random generated points");
		System.out.println("-----------------------------------");
		System.out.println("- config properties: " + configPropertiesFile);
		System.out.println("- times per text   : " + timesPerText);
		System.out.println("- do debug         : " + doDebug);
		System.out.println("- do reduce        : " + doReduce);
		System.out.println("- persist stats    : " + persistStats);
		System.out.println("- exec times       : " + execTimes);
		System.out.println("- dataClay tracing : " + dcTracing);
		System.out.println("- Worker traces storing path: " + pathForWorkerTraces);
		System.out.println(
				"- Optimistic getlocations: " + Configuration.Flags.STORAGEITF_IGNORE_REPLICATION.getBooleanValue());
	}

	private static String getUsage() {
		return "Usage \n\n" + WordcountChunked.class.getName()
				+ " -tc <text_col_alias> [-t <times_per_text>] [-c <config_properties>]"
				+ " [-h (this help)] [-d (extra info)] [-noreduce] [-exectimes <n>] \n";
	}

	private static void activateTracing() throws Exception {
		final long time = System.currentTimeMillis();
		System.out.print("[PARAVER] Activating PARAVER tracing ...");
		long syncTime = ClientManagementLib.activateTracing();
		for (int i = 0; i < DataClay.getBackends().size(); i++) {
			activateTracesAtWorker(syncTime);
		}
		COMPSs.barrier();
		Thread.sleep(Configuration.Flags.PARAVER_TIME_SYNC.getIntValue() * 1000L * 2); // Tracing
																						// stabalization
		System.out.println("[PARAVER] activated in " + (System.currentTimeMillis() - time));
	}

	// TASK
	public static void activateTracesAtWorker(long syncTime) throws Exception {
		dcTracing = true;
		System.out.println("Activating traces at " + InetAddress.getLocalHost().getHostName());
		ClientManagementLib.activateTracingClient(syncTime);
	}

	private static void createTraces() throws Exception {
		final long time = System.currentTimeMillis();
		System.out.print("[PARAVER] Creating PARAVER traces ...");
		ClientManagementLib.deactivateTracing();
		//ClientManagementLib.createParaverTraces();
		for (int i = 0; i < DataClay.getBackends().size(); i++) {
			createTracesAtWorker(pathForWorkerTraces);
		}
		COMPSs.barrier();
		System.out.println("[PARAVER] created traces in " + (System.currentTimeMillis() - time));
	}

	// TASK
	public static void createTracesAtWorker(final String toPath) throws Exception {
		String hostname = InetAddress.getLocalHost().getHostName();
		System.out.println("Creating traces at " + hostname);
		ClientManagementLib.deactivateTracingClient();
		//ClientManagementLib.createClientParaverTraces();
		System.out.println("Traces created at " + hostname + ". Copying them to " + toPath);
		FileUtils.copyFile(new File(HostType.CL.name() + ".prv"),
				new File(toPath + File.separatorChar + "CL." + hostname + ".prv"));
	}
}
