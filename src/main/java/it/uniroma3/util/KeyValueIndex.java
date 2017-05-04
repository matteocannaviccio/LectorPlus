package it.uniroma3.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * 
 * 
 * @author matteo
 *
 */
public class KeyValueIndex {

    private IndexSearcher indexSearcher;

    /**
     * This is the constructor if we need to create the index.
     * @param kvPairsFilePath
     * @param kvIndexPath
     */
    public KeyValueIndex(String kvPairsFilePath, String kvIndexPath){
	this.createIndex(kvPairsFilePath, kvIndexPath);
	this.indexSearcher = createSearcher(kvIndexPath);
    }
    
    /**
     * This is the constructor if we already have the index.
     * @param kvIndexPath
     */
    public KeyValueIndex(String kvIndexPath){
	this.indexSearcher = createSearcher(kvIndexPath);
    }

    /**
     * 
     * @param kvIndexPath
     * @return
     */
    private IndexWriter createWriter(String kvIndexPath) {
	IndexWriter writer = null;
	try {
	    if (new File(kvIndexPath).exists())
		new File(kvIndexPath).delete();
	    FSDirectory dir = FSDirectory.open(Paths.get(kvIndexPath));
	    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
	    config.setOpenMode(OpenMode.CREATE);
	    writer = new IndexWriter(dir, config);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return writer;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    private IndexSearcher createSearcher(String kvIndexPath){
	IndexSearcher searcher = null;
	try {
	    Directory dir = FSDirectory.open(Paths.get(kvIndexPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    searcher = new IndexSearcher(reader);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return searcher;
    }

    /**
     * 
     * @param keywordToEncode
     * @return
     */
    private String encodeBase64(String keywordToEncode){
	return Base64.getEncoder().withoutPadding().encodeToString(keywordToEncode.getBytes());
    }

    /**
     * 
     * @param keywordToDecode
     * @return
     */
    private String decodeBase64(String keywordToDecode){
	return new String(Base64.getDecoder().decode(keywordToDecode));
    }

    /**
     * 
     * 
     * 
     * @param pathToPhrases
     */
    private void createIndex(String kvPairsFilePath, String kvIndexPath) {
	File kvPairsFile = new File(kvPairsFilePath);
	IndexWriter writer = createWriter(kvIndexPath);
	int count_bad = 0;
	int count_ok = 0;
	try {
	    BufferedReader input = new BufferedReader(new FileReader(kvPairsFile));
	    String line = input.readLine();
	    while (line != null){
		/*
		 * we read the key-value file and index the encoded strings
		 */
		String[] field = line.split("\t");
		
		// skip bad-formatted lines
		if (field.length != 2){
		    count_bad ++;
		    line = input.readLine();
		    continue;
		}else{
		    count_ok ++;
		}
		
		String key = encodeBase64(field[0]);
		String value = encodeBase64(field[1]);

		/*
		 * indexing key-value pairs using the name of the fields
		 */
		Document doc = new Document();
		doc.add(new StringField("key", key, Store.YES));
		doc.add(new StringField("value", value, Store.YES));
		writer.addDocument(doc);

		line = input.readLine();
	    }

	    input.close();
	    writer.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	System.out.print(count_ok +"(correct lines) and " + count_bad +"(bad lines)");
    }

    /**
     * 
     * @param value
     * @return
     */
    public Set<String> retrieveKeys(String value){
	Set<String> keys = new HashSet<String>();
	String encodedValue = this.encodeBase64(value);
	Query query = new TermQuery(new Term("value", encodedValue));
	try {
	    TopDocs hits = this.indexSearcher.search(query, 10000);
	    for (ScoreDoc sd : hits.scoreDocs) {
		Document d = this.indexSearcher.doc(sd.doc);
		String decodedKey = this.decodeBase64(d.getField("key").stringValue());
		keys.add(decodedKey);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return keys;
    }

    /**
     * 
     * @param key
     * @return
     */
    public Set<String> retrieveValues(String key){
	Set<String> values = new HashSet<String>();
	String encodedKey = this.encodeBase64(key);
	Query query = new TermQuery(new Term("key", encodedKey));
	try {
	    TopDocs hits = this.indexSearcher.search(query, 20);
	    for (ScoreDoc sd : hits.scoreDocs) {
		Document d = this.indexSearcher.doc(sd.doc);
		String decodedValue = this.decodeBase64(d.getField("value").stringValue());
		values.add(decodedValue);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return values;
    }

}