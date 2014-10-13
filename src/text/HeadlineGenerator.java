import java.lang.StringBuilder; 

import java.util.Arrays; 
import java.util.HashMap; 


/** 
 *  java HeadlineGenerator [-a text_generation_algorithm] corpus_file keywords 
 *
 *  Generates a corpus from given keywords and prints it to stdout. 
 *
 *  -a text_generator_algorithm 
 *                      Specifies the algorithm that determines how the 
 *                      headline is generated.  
 *
 *   corpus_file        Specifies the corpus to be used.  
 *
 *   keywords           The keywords that are going to be used. 
 *
 */
public class HeadlineGenerator {
    public static void main(String[] args) {
        String runoptions = "Usage:HeadlineGenerator [-a text_generation_algorithm] corpus_file keywords";

        // The default algorithm 
        TextGenerator tg = new UchimotoGenerator(); 


        // Hashmaps to save algorithms that are implemented 
        HashMap<String, TextGenerator> textGenerators = new HashMap<>(); 
        textGenerators.put("uchimoto", new UchimotoGenerator());  // I don't even know if I spelt is name correctly 
        textGenerators.put("template", new TemplateGenerator()); 

        String corpus = null; 
        int argsi = 0; 

        // Parse the options 
        for(; argsi<args.length; argsi++) {  
            try {
                // Reached end of options 
                if(!args[argsi].startsWith("-")) {
                    break; 
                }

                if("-a".equals(args[argsi])) { 
                    if(textGenerators.containsKey(args[argsi+1])) {
                        tg = textGenerators.get(args[argsi+1]); 
                    }
                    else {
                        System.err.println("Error: Invalid text generation algorithm: " + args[argsi+1]); 
                        System.exit(1); 
                    }
                    argsi += 1; 
                }
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.err.println("Error: Invalid options"); 
                System.err.println(runoptions);
                System.exit(1); 
            }
        }

        if(argsi == args.length) {
            System.err.println("Error: No corpus specified"); 
            System.err.println(runoptions);
            System.exit(1); 
        }
        corpus = args[argsi];
        argsi += 1; 

        if(argsi == args.length) {
            System.err.println("Error: No keywords specified"); 
            System.err.println(runoptions);
            System.exit(1); 
        }
        String[] keywords = Arrays.copyOfRange(args, argsi, args.length); 

        // Add the corpus to the generator. We don't want to add it earlier 
        // (to all algorithms in the HashMap) since it will propably do 
        // some pre-processing. 

        tg.addCorpus(corpus); 

        String headline = tg.generate(keywords); 
        System.out.println(headline);  
    }
}


// Rough outline. Feel free to move stuff around and change it. 
// Everything should be moved to separate files and maybe packages, e.g. .textgenerators 


// Wondering if abstract classes or interfaces are the way to go. 
abstract class TextGenerator {
    String corpusFile; 
    public TextGenerator() {

    }

    /**
     * Adds the corpus file to the generator. Should probably be 
     * overridden if you want to do pre-processing on it. 
    */
    public void addCorpus(String corpusfile) {

    }

    /** 
     * Generates a headline from a given corpus and keywords. Should 
     * be overridden. 
    */
    public String generate(String[] keywords) {       
        return ""; 
    }
}


class UchimotoGenerator extends TextGenerator { 
    public UchimotoGenerator() {
       super(); 
    }
    public void addCorpus(String corpusfile) {

    }

    public String generate(String[] keywords) {
        String s = "Uchimoto! "; 
        for(String ss : keywords) 
            s += ss + " "; 
        return s; 
    }
}


class TemplateGenerator extends TextGenerator { 
    public TemplateGenerator() {
       super(); 
    }
    public void addCorpus(String corpusfile) {

    }

    public String generate(String[] keywords) {
        String s = "Template! "; 
        for(String ss : keywords) 
            s += ss + " "; 
        return s; 
    }
}


