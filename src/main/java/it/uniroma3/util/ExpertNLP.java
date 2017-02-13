package it.uniroma3.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class ExpertNLP {

    private static String token_model_path = "/Users/matteo/Work/Repository/ualberta/lectorplus/models/en-token.bin";
    private static String sentences_model_path = "/Users/matteo/Work/Repository/ualberta/lectorplus/models/en-sent.bin";
    private static String lemmatizer_dictonary_path = "/Users/matteo/Work/Repository/ualberta/lectorplus/models/en-lemmatizer.dict";
    private static String postagger_model_path = "/Users/matteo/Work/Repository/ualberta/lectorplus/models/en-pos-maxent.bin";

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
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @return
     */
    public synchronized List<Pair<String, String>> tagSentence(String sentece){
	String[] tokens = this.tokenizer.tokenize(sentece);
	String[] tags = this.posTagger.tag(tokens);
	List<Pair<String, String>> tagSequence = new ArrayList<Pair<String, String>>(tokens.length);
	
	for(int i = 0; i < tokens.length ; i++){
	    tagSequence.add(Pair.make(tokens[i].trim(), tags[i].trim()));
	    
	}
	return tagSequence;
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
    public static Tokenizer obtainTokenizer() throws InvalidFormatException, FileNotFoundException, IOException{
	TokenizerModel model = new TokenizerModel(new FileInputStream(token_model_path));
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
    public static DictionaryLemmatizer obtainLemmatizer() throws InvalidFormatException, FileNotFoundException, IOException{
	InputStream model = new FileInputStream(lemmatizer_dictonary_path);
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
    public static POSTagger obtainPOSTagger() throws InvalidFormatException, FileNotFoundException, IOException{
	POSModel model = new POSModel(new FileInputStream(postagger_model_path));
	POSTagger sdetector = new POSTaggerME(model);
	return sdetector;
    }

}
