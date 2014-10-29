package text;

import java.io.*;
import java.util.*;

public class HeadlineTest {

	public static void main(String[] args) throws IOException {
		String[] keywords = {
			"Alien Mars",
			"Banana Vehicle",
			"War Afghanistan",
			"Mayor New York",
			"Submarine Stockholm",
			"Brazil Election",
			"Fifa Bribe Soccer",
			"Mayor Svensson Seattle",
			"Scientology",
			"Kleenex",
			"Xerox",
			"Microsoft",
			"Apple",
			"Interstellar"
		};

		Map<String, Map<String, String>> result = new HashMap<>();
		for (String keywordsStr : keywords) {
			Map<String, TextGenerator> tgs = createTextGenerators();
			Map<String, String> headlines = getHeadlines(keywordsStr, tgs, "sentence");
			result.put(keywordsStr, headlines);
		}

		System.out.println();
		printResult(result);
	}

	private static Map<String, String> getHeadlines(String keywordsStr, Map<String, TextGenerator> tgs, String prefix) throws IOException {
		Map<String, String> headlines = new HashMap<>();
		String[] keywords = keywordsStr.split(" ");
		String corpus = prefix + "_" + keywordsStr.replace(' ', '_') + ".corpus";

		for (Map.Entry<String, TextGenerator> entry : tgs.entrySet()) {
			String name = entry.getKey();
			TextGenerator tg = entry.getValue();

			tg.addCorpus(corpus);

			String headline = tg.generate(keywords);
			headlines.put(name, headline);
			System.out.println(name + ": " + headline);
		}
		return headlines;
	}

	private static Map<String, TextGenerator> createTextGenerators() {
		HashMap<String, TextGenerator> textGenerators = new HashMap<>();
		textGenerators.put("mext", new MextGenerator());
		textGenerators.put("most-likely", new MostLikelyGenerator());
		return textGenerators;
	}

	private static void printResult(Map<String, Map<String, String>> result) {
		for (Map.Entry<String, Map<String, String>> entry : result.entrySet()) {
			String keywords = entry.getKey();
			Map<String, String> headlines = entry.getValue();

			System.out.println(keywords);
			for (Map.Entry<String, String> entry1 : headlines.entrySet()) {
				String name = entry1.getKey();
				String headline = entry1.getValue();

				System.out.println(name + ": " + headline);
			}
			System.out.println();
		}
	}
}
