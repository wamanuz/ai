package text;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Based on Markov chains and attempts to find a sequence of n-grams with as
 * many keywords as possible without moving away too much from the most likely.
 */
public class MextGenerator extends TextGenerator {

	private static class SequenceState implements Comparable<SequenceState> {

		final SequenceState prev;
		final String token;
		final List<String> context;
		private final int score;
		private final int length;
		private final double prob; // indication of how probable this state is

		public SequenceState(SequenceState prev, String token, List<String> context, int length, int score, double prob) {
			this.prev = prev;
			this.token = token;
			this.context = context;
			this.length = length;
			this.score = score;
			this.prob = prob;
		}

		@Override
		public int compareTo(SequenceState o) {
			return o.score - score;
		}

		@Override
		public String toString() {
			List<String> tokens = new ArrayList<>();
			// Traverse previous states and store each token in a list
			SequenceState state = this;
			while (state != null) {
				tokens.add(state.token);
				state = state.prev;
			}
			StringBuilder builder = new StringBuilder();
			// Build the string in reverse order
			for (int i = tokens.size() - 1; i > 0; i--) {
				builder.append(tokens.get(i)).append(' ');
			}
			builder.append(token);
			return builder.toString();
		}
	}

	private final Map<List<String>, Map<String, Integer>> weightMatrix;
	private static final int N = 2;
	private static final int MAX_WORDS = 20;
	private static final int TIMELIMIT = 30000;

	public MextGenerator() {
		weightMatrix = new HashMap<>();
	}

	@Override
	public void addCorpus(String corpusfile) throws IOException {
		InputStream in = new FileInputStream(corpusfile);
		Iterator<String> tokenIter = createTokenIterator(in);

		boolean ready = true;
		LinkedList<String> ngram = new LinkedList<>();
		for (Iterator<String> it = tokenIter; it.hasNext();) {
			String token = it.next();
			if (token.isEmpty()) {
				continue;
			}
			if (ready && token.equals(String.valueOf(TextGenerator.first_char))) {
				ngram.clear();
				for (int i = 0; i < N; i++) {
					ngram.addLast(null);
				}
				ready = false;
			} else if (token.equals(String.valueOf(TextGenerator.last_char))) {
				ready = true;
			}

			//System.out.println(token);
			increaseWeight(new ArrayList<>(ngram), token);
			ngram.removeFirst();
			ngram.addLast(token);
		}
	}

	@Override
	public String generate(String[] keywords) {
		long timeStop = System.currentTimeMillis() + TIMELIMIT;

		LinkedList<String> context = new LinkedList<>();
		for (int i = 0; i < N; i++) {
			context.addLast(null);
		}

		PriorityQueue<SequenceState> looseEnds = new PriorityQueue<>();
		final Map<String, Integer> completed = new HashMap<>();

		SequenceState start = new SequenceState(null, String.valueOf(first_char), context, 0, 0, 1);

		looseEnds.add(start);

		int maxScore = 0;
		while (!looseEnds.isEmpty() && System.currentTimeMillis() < timeStop) {
			// Continue with the highest scored unfinished sequence
			SequenceState state = looseEnds.poll();

			LinkedList<String> newContext = new LinkedList<>(state.context);
			newContext.removeFirst();
			newContext.addLast(state.token);

			Map<String, Integer> table = weightMatrix.get(newContext);

			// Find largest weight
			//int sum = 0;
			double largestWeight = 0;
			for (Map.Entry<String, Integer> entry : table.entrySet()) {
				//sum += entry.getValue();
				largestWeight = Math.max(largestWeight, entry.getValue());
			}

			// Queue potential next words
			for (Map.Entry<String, Integer> entry : table.entrySet()) {
				String nextWord = entry.getKey();
				Integer weight = entry.getValue();

				double prob;// = weight / (double) sum;
				prob = weight / largestWeight; // weight relative to the largest
				int estimatedScore = evaluate(keywords, state, nextWord, prob);
				SequenceState nextState = new SequenceState(state, nextWord, newContext, state.length + 1, estimatedScore, state.prob * prob);
				if (!String.valueOf(last_char).equals(nextWord)) {
					// Queue if not too unlikely or too long
					if (nextState.prob > 0.1 && nextState.length < MAX_WORDS) {
						looseEnds.add(nextState);
					}
				} else {
					// debug stuff
					if (estimatedScore > maxScore) {
						System.out.println(estimatedScore + "> " + nextState);
						maxScore = estimatedScore;
					}
					//completed.put(estimatedScore + ": " + nextState.toString(), estimatedScore);
					completed.put(nextState.toString(), estimatedScore);
					// TODO: Cancel if a completed sentence with all keywords is found?
				}
			}
		}

		// Find completed sentence with the highest score
		String topSentence = null;
		int topScore = 0;
		for (Map.Entry<String, Integer> entry : completed.entrySet()) {
			String sentence = entry.getKey();
			Integer score = entry.getValue();
			if (score > topScore) {
				topScore = score;
				topSentence = sentence;
			}
		}

		if (topSentence == null) {
			return null; // nothing :(
		}
		// Filter sentence from start and stop tokens
		topSentence = topSentence.replaceAll("^\\$ *| *\\^$", "");
		return topSentence;
	}

	private static int evaluate(String[] keywords, SequenceState prev, String token, double prob) {
		int usedKeywords = 0;

		// TODO: Optimize or something
		for (String keyword : keywords) {
			if (token.equalsIgnoreCase(keyword)) {
				usedKeywords++;
				continue;
			}
			SequenceState state = prev;
			while (state != null) {
				if (state.token.equalsIgnoreCase(keyword)) {
					usedKeywords++;
					break;
				}
				state = state.prev;
			}
		}
		return (int) (10000 * (prev.prob * prob
				+ 2 * ((double) usedKeywords / keywords.length)));
	}

	private Iterator<String> createTokenIterator(InputStream in) {
		final Scanner scanner = new Scanner(in);
		Iterator<String> iter = new Iterator<String>() {

			boolean start = true;
			Scanner lineScanner;

			@Override
			public boolean hasNext() {
				// True if the corpus has more lines or a line is being parsed
				return scanner.hasNext() || !start;
			}

			@Override
			public String next() {
				if (start) { // Start of line, return first char token and read next line
					start = false;
					lineScanner = new Scanner(scanner.nextLine());
					lineScanner.useDelimiter("[ \\.]");
					return String.valueOf(TextGenerator.first_char);
				}
				if (lineScanner.hasNext()) { // return next word
					return lineScanner.next();
				}
				// End of line reached, return last char token and reset start
				start = true;
				return String.valueOf(TextGenerator.last_char);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		return iter;
	}

	private void increaseWeight(List<String> context, String token) {
		Map<String, Integer> nextTable = getTransitionTable(context);

		if (nextTable.containsKey(token)) {
			nextTable.put(token, nextTable.get(token) + 1);
		} else {
			nextTable.put(token, 1);
		}
	}

	private Map<String, Integer> getTransitionTable(List<String> context) {
		Map<String, Integer> nextTable;
		// Find or create table
		if (weightMatrix.containsKey(context)) {
			nextTable = weightMatrix.get(context);
		} else {
			nextTable = new HashMap<>();
			weightMatrix.put(context, nextTable);
		}
		return nextTable;
	}

	private <T> int calcTotalWeight(Map<T, Integer> table) {
		int total = 0;
		for (Integer weight : table.values()) {
			total += weight;
		}
		return total;
	}
}
