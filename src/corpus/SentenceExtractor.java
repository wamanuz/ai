package corpus;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentenceExtractor extends ExtractionAlgorithm {

	@Override
	public String extract(Link l, String[] keywords) throws ExtractionException {
		URL url;
		try {
			// Create the URL
			url = new URL(l.getURL());
 
			// get the text
			String text = DefaultExtractor.INSTANCE.getText(url);
			
			text = separateSentences(text, keywords);
			return text;
			
		} catch (MalformedURLException | BoilerpipeProcessingException ex) {
			throw new ExtractionException(ex);
		}
    }
	
	private String separateSentences(String text, String[] keywords) {
		StringBuilder builder = new StringBuilder(text.length());
			
		// Build the keywords for the regex
		String keyword = "";
		for (int i = 0; i < keywords.length; i++) {
			if (i != keywords.length - 1)
				keyword += keywords[i] + '|';
			else
				keyword += keywords[i];
		}
		// Pattern with a regular expression for sentences containing at least one keyword
		Pattern p = Pattern.compile("[A-Z](?i)[^.?!]*?\\b(" + keyword + ")\\b[^.?!]*[.?!]");
	
		Matcher m = p.matcher(text);
		// Add each matched sentence on a separate line.
		while (m.find()) {
			String sentence = m.group();
			builder.append(sentence).append('\n');
		}

		return builder.toString().trim(); // Trim away that last newline
	}
}

