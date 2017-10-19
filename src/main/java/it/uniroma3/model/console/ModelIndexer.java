package it.uniroma3.model.console;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import it.uniroma3.config.Lector;
import it.uniroma3.main.util.CounterMap;

/**
 * This code aims to pr
 * 
 * @author matteo
 *
 */
public class ModelIndexer {

  public enum IndexType {
    phrases, relations, typedphrases, types, phrases2relations, phrases2typedrelations, typedphrases2relations, relations2typedphrases, types2relations
  };

  private IndexSearcher phrasesSearcher;
  private String phrasesPATH = "p_index";

  private IndexSearcher relationsSearcher;
  private String relationsPATH = "r_index";

  private IndexSearcher typedphrasesSearcher;
  private String typedphrasesPATH = "tp_index";

  private IndexSearcher typesSearcher;
  private String typesPATH = "t_index";

  private IndexSearcher phrase2relationSearcher;
  private String phrase2relationPATH = "p2r_index";

  private IndexSearcher phrase2typedrelationSearcher;
  private String phrase2typedrelationPATH = "p2tr_index";

  private IndexSearcher typedphrase2relationSearcher;
  private String typedphrase2relationPATH = "tp2r_index";

  private IndexSearcher types2relationSearcher;
  private String types2relationPATH = "t2r_index";

  private IndexSearcher relation2typedphraseSearcher;
  private String relation2typedphrasePATH = "r2tp_index";


  /**
   * 
   * @param filtered_triples
   * @param folderIndex
   */
  public ModelIndexer(String folderIndex, int PERCENTAGE) {
    File folder = new File(folderIndex);
    String phrasesIndexPath = folder.getAbsolutePath() + "/" + phrasesPATH;
    String relationsIndexPath = folder.getAbsolutePath() + "/" + relationsPATH;
    String typedphrasesIndexPath = folder.getAbsolutePath() + "/" + typedphrasesPATH;
    String typesIndexPath = folder.getAbsolutePath() + "/" + typesPATH;
    String phrase2relationIndexPath = folder.getAbsolutePath() + "/" + phrase2relationPATH;
    String phrase2typedrelationIndexPath =
        folder.getAbsolutePath() + "/" + phrase2typedrelationPATH;
    String typedphrase2relationIndexPath =
        folder.getAbsolutePath() + "/" + typedphrase2relationPATH;
    String types2relationIndexPath = folder.getAbsolutePath() + "/" + types2relationPATH;
    String relations2typedphraseIndexPath =
        folder.getAbsolutePath() + "/" + relation2typedphrasePATH;

    // create the indexes if they do not exist
    if (!folder.exists()) {
      folder.mkdirs();

      Lector.init("FE");
      System.out.println("\t-> Init Model Indexer...");
      CounterMap<String> filtered_triples =
          Lector.getDbmodel(false).retrieveEvidence("model_triples", 1, PERCENTAGE);
      Lector.close();

      this.createIndexes(filtered_triples, phrasesIndexPath, relationsIndexPath,
          typedphrasesIndexPath, typesIndexPath, phrase2relationIndexPath,
          phrase2typedrelationIndexPath, typedphrase2relationIndexPath, types2relationIndexPath,
          relations2typedphraseIndexPath);
    }

    this.phrasesSearcher = createSearcher(phrasesIndexPath);
    this.relationsSearcher = createSearcher(relationsIndexPath);
    this.typedphrasesSearcher = createSearcher(typedphrasesIndexPath);
    this.typesSearcher = createSearcher(typesIndexPath);
    this.phrase2relationSearcher = createSearcher(phrase2relationIndexPath);
    this.phrase2typedrelationSearcher = createSearcher(phrase2typedrelationIndexPath);
    this.typedphrase2relationSearcher = createSearcher(typedphrase2relationIndexPath);
    this.types2relationSearcher = createSearcher(types2relationIndexPath);
    this.relation2typedphraseSearcher = createSearcher(relations2typedphraseIndexPath);
  }

  /**
   * 
   * @param kvIndexPath
   * @return
   */
  private IndexWriter createWriter(String kvIndexPath) {
    IndexWriter writer = null;
    try {
      Directory dir = null;
      if (new File(kvIndexPath).exists())
        new File(kvIndexPath).delete();
      dir = FSDirectory.open(Paths.get(kvIndexPath));
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
  private IndexSearcher createSearcher(String kvIndexPath) {
    IndexSearcher searcher = null;
    try {
      IndexReader reader = DirectoryReader.open(new MMapDirectory(Paths.get(kvIndexPath)));
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
  private String encodeBase64(String keywordToEncode) {
    return Base64.getEncoder().withoutPadding().encodeToString(keywordToEncode.getBytes());
  }

  /**
   * 
   * @param keywordToDecode
   * @return
   */
  private String decodeBase64(String keywordToDecode) {
    return new String(Base64.getDecoder().decode(keywordToDecode));
  }

  /**
   * 
   * @param typedphrase2relationIndexPath
   * @param phrase2typedrelationIndexPath
   * @param phrase2relationIndexPath
   * @param typesIndexPath
   * @param typedphrasesIndexPath
   * @param relationsIndexPath
   * @param phrasesIndexPath
   * @param kvPairsList
   * @param kvIndexPath
   * @return
   */
  private void createIndexes(CounterMap<String> everything, String phrasesIndexPath,
      String relationsIndexPath, String typedphrasesIndexPath, String typesIndexPath,
      String phrase2relationIndexPath, String phrase2typedrelationIndexPath,
      String typedphrase2relationIndexPath, String types2relationIndexPath,
      String relations2typedphraseIndexPath) {

    IndexWriter phrasesIndexWriter = createWriter(phrasesIndexPath);
    IndexWriter relationsIndexWriter = createWriter(relationsIndexPath);
    IndexWriter typedphrasesIndexWriter = createWriter(typedphrasesIndexPath);
    IndexWriter typesIndexWriter = createWriter(typesIndexPath);
    IndexWriter phrase2relationIndexWriter = createWriter(phrase2relationIndexPath);
    IndexWriter phrase2typedrelationIndexWriter = createWriter(phrase2typedrelationIndexPath);
    IndexWriter typedphrase2relationIndexWriter = createWriter(typedphrase2relationIndexPath);
    IndexWriter types2relationIndexWriter = createWriter(types2relationIndexPath);
    IndexWriter relations2typedphraseIndexWriter = createWriter(relations2typedphraseIndexPath);

    int totPhrases = 0;
    int totRelations = 0;
    int totTypedPhrases = 0;
    int totTypes = 0;

    try {
      for (Map.Entry<String, Integer> e : everything.entrySet()) {
        String entry = e.getKey();
        int count = e.getValue();
        String[] fields = entry.split("\t");
        String type_subject = fields[0];
        String phrase_placeholder = fields[1];
        String type_object = fields[2];
        String relation = fields[3];
        String typedphrase = type_subject + "\t" + phrase_placeholder + "\t" + type_object;
        String typedrelation = type_subject + "\t" + relation + "\t" + type_object;
        String types = type_subject + "\t" + type_object;


        /*
         * phrase ---> count
         */
        Document doc = new Document();
        doc.add(new StringField("key", encodeBase64(phrase_placeholder), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        totPhrases += count;
        phrasesIndexWriter.addDocument(doc);

        /*
         * relation ---> count
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(relation), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        totRelations += count;
        relationsIndexWriter.addDocument(doc);

        /*
         * typedphrase ---> count
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(typedphrase), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        totTypedPhrases += count;
        typedphrasesIndexWriter.addDocument(doc);

        /*
         * types ---> count
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(types), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        totTypes += count;
        typesIndexWriter.addDocument(doc);

        /*
         * phrases ---> relation
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(phrase_placeholder), Store.YES));
        doc.add(new StringField("value", encodeBase64(relation), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        phrase2relationIndexWriter.addDocument(doc);

        /*
         * phrases ---> type relation type
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(phrase_placeholder), Store.YES));
        doc.add(new StringField("value", encodeBase64(typedrelation), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        phrase2typedrelationIndexWriter.addDocument(doc);

        /*
         * typedphrase ---> relation
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(typedphrase), Store.YES));
        doc.add(new StringField("value", encodeBase64(relation), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        typedphrase2relationIndexWriter.addDocument(doc);

        /*
         * relation ---> typedphrase
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(relation), Store.YES));
        doc.add(new StringField("value", encodeBase64(typedphrase), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        relations2typedphraseIndexWriter.addDocument(doc);

        /*
         * types ---> relation
         */
        doc = new Document();
        doc.add(new StringField("key", encodeBase64(types), Store.YES));
        doc.add(new StringField("value", encodeBase64(relation), Store.YES));
        doc.add(new StringField("count", String.valueOf(count), Store.YES));
        types2relationIndexWriter.addDocument(doc);
      }

      // NOW AD THE TOTAL AS LAST ENTRY and then close
      /*
       * phrase ---> count
       */
      Document doc = new Document();
      doc.add(new StringField("key", encodeBase64("TOTAL"), Store.YES));
      doc.add(new StringField("count", String.valueOf(totPhrases), Store.YES));
      phrasesIndexWriter.addDocument(doc);

      /*
       * relation ---> count
       */
      doc = new Document();
      doc.add(new StringField("key", encodeBase64("TOTAL"), Store.YES));
      doc.add(new StringField("count", String.valueOf(totRelations), Store.YES));
      relationsIndexWriter.addDocument(doc);

      /*
       * typedphrase ---> count
       */
      doc = new Document();
      doc.add(new StringField("key", encodeBase64("TOTAL"), Store.YES));
      doc.add(new StringField("count", String.valueOf(totTypedPhrases), Store.YES));
      typedphrasesIndexWriter.addDocument(doc);

      /*
       * types ---> count
       */
      doc = new Document();
      doc.add(new StringField("key", encodeBase64("TOTAL"), Store.YES));
      doc.add(new StringField("count", String.valueOf(totTypes), Store.YES));
      typesIndexWriter.addDocument(doc);

      phrasesIndexWriter.close();
      relationsIndexWriter.close();
      typedphrasesIndexWriter.close();
      typesIndexWriter.close();
      phrase2relationIndexWriter.close();
      phrase2typedrelationIndexWriter.close();
      typedphrase2relationIndexWriter.close();
      types2relationIndexWriter.close();
      relations2typedphraseIndexWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param key
   * @param index
   * @return
   */
  public int getKeyCount(String key, IndexType index) {
    int total = 0;
    IndexSearcher searcher = getIndexSearcherFromString(index);
    String encodedKey = this.encodeBase64(key);
    Query query = new TermQuery(new Term("key", encodedKey));
    try {
      TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
      for (ScoreDoc sd : hits.scoreDocs) {
        Document d = searcher.doc(sd.doc);
        int count = Integer.parseInt(d.getField("count").stringValue());
        total += count;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return total;
  }

  /**
   * 
   * @param key
   * @param index
   * @return
   */
  public CounterMap<String> getKeyValuesCounts(String key, IndexType index) {
    IndexSearcher searcher = getIndexSearcherFromString(index);
    CounterMap<String> values = new CounterMap<String>();
    String encodedKey = this.encodeBase64(key);
    Query query = new TermQuery(new Term("key", encodedKey));
    try {
      TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
      for (ScoreDoc sd : hits.scoreDocs) {
        Document d = searcher.doc(sd.doc);
        String decodedValue = this.decodeBase64(d.getField("value").stringValue());
        int count = Integer.parseInt(d.getField("count").stringValue());
        values.add(decodedValue, count);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return values;
  }

  /**
   * 
   * @return
   */
  public CounterMap<String> matchAllSimple(IndexType index) {
    IndexSearcher searcher = getIndexSearcherFromString(index);
    CounterMap<String> results = new CounterMap<String>();
    Query query = new MatchAllDocsQuery();
    try {
      TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
      for (ScoreDoc sd : hits.scoreDocs) {
        Document d = searcher.doc(sd.doc);
        String relation = this.decodeBase64(d.getField("key").stringValue());
        // String tp = this.decodeBase64(d.getField("value").stringValue());
        int count = Integer.parseInt(d.getField("count").stringValue());
        // if (!tp.split("\t")[1].equals(",") && !tp.split("\t")[1].equals("and"))
        results.add(relation, count);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return results;
  }

  /**
   * 
   * @return
   */
  public Map<String, CounterMap<String>> matchAllComposite(IndexType index) {
    IndexSearcher searcher = getIndexSearcherFromString(index);
    Map<String, CounterMap<String>> results = new HashMap<String, CounterMap<String>>();
    Query query = new MatchAllDocsQuery();
    try {
      TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
      for (ScoreDoc sd : hits.scoreDocs) {
        Document d = searcher.doc(sd.doc);
        String relation = this.decodeBase64(d.getField("key").stringValue());
        String tp = this.decodeBase64(d.getField("value").stringValue());
        int count = Integer.parseInt(d.getField("count").stringValue());
        if (!results.containsKey(relation))
          results.put(relation, new CounterMap<String>());
        results.get(relation).add(tp, count);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return results;
  }

  /**
   * 
   * @param index
   * @return
   */
  private IndexSearcher getIndexSearcherFromString(IndexType index) {
    IndexSearcher searcher = null;

    switch (index) {
      case phrases:
        searcher = this.phrasesSearcher;
        break;

      case relations:
        searcher = this.relationsSearcher;
        break;

      case typedphrases:
        searcher = this.typedphrasesSearcher;
        break;

      case types:
        searcher = this.typesSearcher;
        break;

      case phrases2relations:
        searcher = this.phrase2relationSearcher;
        break;

      case phrases2typedrelations:
        searcher = this.phrase2typedrelationSearcher;
        break;

      case typedphrases2relations:
        searcher = this.typedphrase2relationSearcher;
        break;

      case types2relations:
        searcher = this.types2relationSearcher;
        break;

      case relations2typedphrases:
        searcher = this.relation2typedphraseSearcher;
        break;
    }

    return searcher;
  }

  /**
   * @return the totPhrases
   */
  public int getTotPhrases() {
    return getKeyCount("TOTAL", IndexType.phrases);
  }

  /**
   * @return the totRelations
   */
  public int getTotRelations() {
    return getKeyCount("TOTAL", IndexType.relations);
  }

  /**
   * @return the totTypedPhrases
   */
  public int getTotTypedPhrases() {
    return getKeyCount("TOTAL", IndexType.typedphrases);
  }

  /**
   * @return the totTypes
   */
  public int getTotTypes() {
    return getKeyCount("TOTAL", IndexType.types);
  }



}
