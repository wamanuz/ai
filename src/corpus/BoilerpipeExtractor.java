package corpus;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoilerpipeExtractor extends ExtractionAlgorithm {

	@Override
	public String extract(Link l, String[] keywords) throws ExtractionException {
		try {
			URL url = new URL(l.getURL());
			String text = DefaultExtractor.INSTANCE.getText(url);
			text = separateSentences(text);
			return text;
		} catch (MalformedURLException | BoilerpipeProcessingException ex) {
			throw new ExtractionException(ex);
		}
	}

	private String separateSentences(String text) {
		StringBuilder builder = new StringBuilder(text.length());

		Pattern pattern;
		// Match sentences starting with capital letter or digit and ending with '.', '!' or '?'. Can contain citations.
		pattern = Pattern.compile("[A-Z\\d](?:[^.!?\\n\"](?:\\\"[^\"\\n]+\\\")?)*[.!?](?=\\s)");
		//pattern = Pattern.compile("(?![.!?\\s ])[^.!?\\n]+[.!?]"); // Variant starting with anything except whitespace and no citations.
		//pattern = Pattern.compile("[A-Z\\d][^.!?\\n]*[.!?]"); // Variant without citations.
		Matcher matcher = pattern.matcher(text);

		// Add each matched sentence on a separate line.
		while (matcher.find()) {
			String sentence = matcher.group();
			builder.append(sentence).append('\n');
		}

		return builder.toString().trim(); // Trim away that last newline
	}
}
