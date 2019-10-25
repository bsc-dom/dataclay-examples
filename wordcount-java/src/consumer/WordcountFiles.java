package consumer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import es.bsc.compss.api.COMPSs;

public class WordcountFiles {
	private static String dataFolder;
	private static boolean doReduce = true;
	private static int execTimes = 2;
	private static int timesPerText = 1;
	private static String[] filePaths;
	private static HashMap<String, Integer>[] partialResult;
	private static HashMap<String, Integer> result;
	private static long iotime = 0;

	public static void main(String args[]) {
		// Get parameters
		checkArguments(args);

		dataFolder = args[0];

		System.out.println("[LOG] dataFolder parameter value = " + dataFolder);
		System.out.println("[LOG] timesPerText parameter value = " + timesPerText);

		for (int i = 1; i <= execTimes; i++) {
			long startTime = System.currentTimeMillis();
			run();
			COMPSs.barrier();
			long endTime = System.currentTimeMillis();
			System.out.println("[TIMER] RUN exec " + i + ": " + (endTime - startTime) + " ms. " + (iotime / 1000)
					+ " micros for IO.");
			printResult();
			endTime = System.currentTimeMillis();
			System.out.println("[TIMER] RUN + RESULT exec " + i + ": " + (endTime - startTime) + " ms. "
					+ (iotime / 1000) + " micros for IO.");
		}
	}

	private static void printResult() {
		if (doReduce && result != null) {
			System.out.println("[LOG] Main program finished.");
			System.out.println("[LOG] Result size = " + result.keySet().size());
			System.out.println("[LOG] Print some word counts (max 10): ");
			int i = 10;
			for (Entry<String, Integer> curWord : result.entrySet()) {
				System.out.println("[LOG] ---- [" + curWord.getKey() + " -> " + curWord.getValue() + "]");
				i--;
				if (i == 0) {
					break;
				}
			}
		} else {
			System.out.println("[WARNING] No result produced (doReduce flag is set to " + doReduce + ").");
		}

	}

	private static void run() {
		// Initialize file Names
		System.out.println("[LOG] Initializing filenames for each matrix");
		initializeVariables();

		// Compute result
		System.out.println("[LOG] Total memory: " + Runtime.getRuntime().totalMemory());
		System.out.println("[LOG] Max memory: " + Runtime.getRuntime().maxMemory());
		System.out.println("[LOG] Computing result");
		int l = filePaths.length;
		for (int i = 0; i < l; ++i) {
			String fp = filePaths[i];
			partialResult[i] = wordCount(fp);
		}

		if (doReduce) {
			// MERGE-REDUCE
			LinkedList<Integer> q = new LinkedList<Integer>();
			for (int i = 0; i < l; i++) {
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
			result = partialResult[x];
		} else {
			result = null;
		}
	}

	@SuppressWarnings("unchecked")
	private static void initializeVariables() {
		File[] fList = new File(dataFolder).listFiles();
		filePaths = new String[fList.length * timesPerText];
		int i = 0;
		for (int j = 0; j < timesPerText; j++) {
			for (File f : fList) {
				filePaths[i] = f.getAbsolutePath();
				i = i + 1;
			}
		}

		System.out.println("[LOG] Wordcounting following files: ");
		System.out.println(Arrays.toString(filePaths));

		partialResult = (HashMap<String, Integer>[]) new HashMap[filePaths.length];
		result = new HashMap<String, Integer>();
	}

	public static HashMap<String, Integer> reduceTask(HashMap<String, Integer> m1, HashMap<String, Integer> m2) {
		for (Entry<String, Integer> entry : m2.entrySet()) {
			Integer previousValue = m1.get(entry.getKey());
			if (previousValue == null) {
				m1.put(entry.getKey(), entry.getValue());
			} else {
				m1.put(entry.getKey(), entry.getValue() + previousValue);
			}
		}
		return m1;
	}

	public static HashMap<String, Integer> wordCount(String filePath) {
		File file = new File(filePath);
		HashMap<String, Integer> res = new HashMap<String, Integer>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] words = line.split(" ");
				for (String word : words) {
					if (res.containsKey(word)) {
						res.put(word, res.get(word) + 1);
					} else {
						res.put(word, 1);
					}
				}
				line = br.readLine();
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Cannot retrieve values from " + file.getName());
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					System.err.println("[ERROR] Cannot close buffered reader on file " + file.getName());
					e.printStackTrace();
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
					System.err.println("[ERROR] Cannot close file reader on file " + file.getName());
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	private static void checkArguments(String[] args) {
		if (args.length < 1) {
			System.err.println("[ERROR] Bad arguments. " + getUsage());
			System.exit(1);
		}
		if (args.length > 1) {
			for (int argIndex = 1; argIndex < args.length;) {
				String arg = args[argIndex++];
				if (arg.equals("-t")) {
					timesPerText = new Integer(args[argIndex++]);
				} else if (arg.equals("-h")) {
					System.out.println("[HELP] " + getUsage());
					System.exit(0);
				} else if (arg.equals("-debug")) {
				} else if (arg.equals("-noreduce")) {
					doReduce = false;
				} else if (arg.equals("-exectimes")) {
					execTimes = new Integer(args[argIndex++]);
				} else {
					System.err.println("[ERROR] Bad arguments. " + getUsage());
					System.exit(1);
				}
			}
		}
	}

	private static String getUsage() {
		return "[INFO] Usage \n\n" + WordcountFiles.class.getName()
				+ " <data_folder> [-t <times_per_text>] [-h (this manual)] [-d (extra info)] [-noreduce] \n";
	}
}
