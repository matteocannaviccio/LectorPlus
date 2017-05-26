package it.uniroma3.extractor.util.nlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.util.Token;
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
public class OpenNLP {
    private POSTagger posTagger;
    private Tokenizer tokenizer;
    private DictionaryLemmatizer lemmatizer;

    /**
     * 
     * @param task
     */
    public OpenNLP(){
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
    public Token[] applyPOSTagger(String sentece){
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
    public String getSingular(String word, String postag){
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
	InputStream model = new FileInputStream(Configuration.getLemmatizerModel());
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
	POSModel model = new POSModel(new FileInputStream(Configuration.getPOSModel()));
	POSTagger sdetector = new POSTaggerME(model);
	return sdetector;
    }

}
