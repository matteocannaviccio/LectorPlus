package it.uniroma3.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.PatternComparator;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
/**
 * 
 * @author matteo
 *
 */
public class ExpertNLP {

    private POSTagger posTagger;
    private Tokenizer tokenizer;
    private DictionaryLemmatizer lemmatizer;


    /**
     * 
     * @param task
     */
    public ExpertNLP(){
	try {
	    posTagger = obtainPOSTagger();
	    tokenizer = obtainTokenizer();
	    lemmatizer = obtainLemmatizer();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @return
     */
    public Token[] tagFirstSentence(String sentece){
	String[] tokens = this.tokenizer.tokenize(sentece);
	String[] tags = this.posTagger.tag(tokens);
	Token[] tagSequence = new Token[tokens.length];

	for(int i = 0; i < tokens.length ; i++){
	    tagSequence[i] = new Token(tokens[i], tags[i]);

	}
	return tagSequence;
    }


    /**
     * 
     * @return
     */
    public String tagBlock(String block){
	String[] tokens = block.split("[^<>\\w\\{Punct}]+");
	String[] tags = this.posTagger.tag(tokens);

	ConcurrentSkipListSet<Pair<String, String>> regexes = new ConcurrentSkipListSet<Pair<String, String>>(new PatternComparator());
	StringBuffer currentToken = new StringBuffer();
	String currentLabel = "-";

	for(int i = 0; i < tokens.length ; i++){
	    String word = tokens[i];
	    String type = tags[i];

	    //System.out.println(word + "\t" + type);
	    // Build annotations

	    if (!word.startsWith("SE") && !word.startsWith("PE")){
		if (type.equals("NNP") || type.equals("NNPS") ){
		    currentToken = currentToken.append(" " + word);
		}else if(currentLabel.equals("NNP") || currentLabel.equals("NNPS")){
		    // add regex and replacement in the map
		    String regex = "(?![A-Z-]+<)\\b" + Pattern.quote(currentToken.toString().trim()) + "\\b(?![^<]*?>)";
		    String replacement = Matcher.quoteReplacement(currentLabel + "<" + currentToken.toString().trim() + ">");
		    regexes.add(Pair.make(regex, replacement));
		    currentToken = new StringBuffer();

		}else{
		    currentToken = new StringBuffer();
		}
		currentLabel = type;
	    }
	}
	if (currentLabel.equals("NNP") || currentLabel.equals("NNPS")){
	    // add regex and replacement in the map
	    String regex = "(?![A-Z-]+<)\\b" + Pattern.quote(currentToken.toString().trim()) + "\\b(?![^<]*?>)";
	    String replacement = Matcher.quoteReplacement(currentLabel + "<" + currentToken.toString().trim() + ">");
	    regexes.add(Pair.make(regex, replacement));
	}

	/*
	 * Apply all the annotations (regex with the replacements).
	 */
	for(Pair<String, String> regex : regexes){
	    block = block.replaceAll(regex.key, regex.value);

	}
	return block;
    }


    /**
     * 
     * @return
     */
    public synchronized String getSingular(String word, String postag){
	return lemmatizer.apply(word, postag);
    }


    /**
     * 
     * @return
     * @throws InvalidFormatException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Tokenizer obtainTokenizer() throws InvalidFormatException, FileNotFoundException, IOException{
	TokenizerModel model = new TokenizerModel(new FileInputStream(Configuration.getTokenModel()));
	Tokenizer sdetector = new TokenizerME(model);
	return sdetector;
    }

    /**
     * 
     * @return
     * @throws InvalidFormatException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public DictionaryLemmatizer obtainLemmatizer() throws InvalidFormatException, FileNotFoundException, IOException{
	InputStream model = new FileInputStream(Configuration.getLemmaDictionary());
	DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(model);
	return lemmatizer;
    }

    /**
     * 
     * @return
     * @throws InvalidFormatException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public POSTagger obtainPOSTagger() throws InvalidFormatException, FileNotFoundException, IOException{
	POSModel model = new POSModel(new FileInputStream(Configuration.getPosTagModel()));
	POSTagger sdetector = new POSTaggerME(model);
	return sdetector;
    }


    public static void main(String[] args){
	/*
	 * Read the config file and instanciate a Configuration object.
	 */
	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");
	String text = "PE_ALIAS<Alexander_Kerensky> was a Russian lawyer, and Alexander politician who served as the SE_NAME<Minister_of_Justice> in the newly formed SE_ORG<Russian_Provisional_Government>, as SE_ORG<Minister_of_War>, and second SE_ORG<Prime_Minister_of_Russia> of the between July and November 1917. A leader of the moderate-socialist SE_ORG<Trudoviks> faction of the SE_ORG<Socialist_Revolutionary_Party>, Kerensky was a key political figure in the SE_ORG<Russian_Revolution> of 1917. On 7 November, his government was overthrown by the SE_ORG<Vladimir_Lenin>-led SE_ORG<Bolshevik>s in the SE_ORG<October_Revolution>. PE_PRO<Alexander_Kerensky> spent the remainder of his life in exile, in Paris and New York City, but was buried in London.";
	ExpertNLP expert = new ExpertNLP();
	System.out.println(expert.tagBlock(text));
    }

}
