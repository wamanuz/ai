package corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SentenceExtractor extends ExtractionAlgorithm {

	@Override
	public String extract(Link l, String[] keywords) throws ExtractionException {
		URL url;
		String extractedSentences = "";
		try {
			// get URL content
			url = new URL(l.url);
			URLConnection conn = url.openConnection();
 
			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
 
			String inputLine;
			// Build the keywords for the regex
			String keyword = "";
			for (int i = 0; i < keywords.length; i++) {
				if (i != keywords.length - 1)
					keyword += keywords[i] + '|';
				else
					keyword += keywords[i];
			}
			Pattern p = Pattern.compile("[A-Z](?i)[^.?!]*?\\b(" + keyword + ")\\b[^.?!]*[.?!]");
				
			while ((inputLine = br.readLine()) != null) {
				
				Matcher m = p.matcher(inputLine);

				while (m.find()) {
					extractedSentences += m.group();
				}
			}
			
			br.close();
		}
        return extractedSentences; 
    }
}
