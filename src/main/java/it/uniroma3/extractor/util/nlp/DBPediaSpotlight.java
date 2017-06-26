package it.uniroma3.extractor.util.nlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.entitydetection.PatternComparator;
import it.uniroma3.extractor.util.Pair;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * @author matteo
 */
public class DBPediaSpotlight {

    private static HttpClient CLIENT = new HttpClient(new MultiThreadedHttpConnectionManager());

    private static String API_URL = "http://localhost:2222/";
    private Process process;
    private double confidence;
    private int support;

    /**
     * @param confidence
     * @param support
     */
    public DBPediaSpotlight(double confidence, int support) {
        this.confidence = confidence;
        this.support = support;
        this.process = runServer();

    }

    /**
     *
     */
    private Process runServer() {
        System.out.print("\n-> Loading DBPedia Spotlight ... ");
        Process p = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-Xmx16g",
                "-jar",
                Configuration.getSpotlightJar(),
                Configuration.getSpotlightModel(),
                Configuration.getSpotlightLocalURL()
            );
            File dirErr = new File(Configuration.getSpotlightLocalERR());
            pb.redirectError(dirErr);
            p = pb.start();
            p.waitFor(120, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(" Done!");
        return p;
    }

    /**
     * Destroy the process, if it is alive.
     */
    public void killProcess() {
        if (process != null)
            process.destroy();
    }

    /**
     * Creates the request to the DBPediaSpotlight service.
     *
     * @param text
     * @return
     */
    private JsonObject getAnnotatedText(String text) {
        InputStream responseBody = null;    // Read the response body.

        try {

            GetMethod getMethod = new GetMethod(
                API_URL + "rest/annotate/?"
                    + "confidence=" + this.confidence + "&support=" + this.support
                    + "&text=" + URLEncoder.encode(text, "utf-8"));

            getMethod.addRequestHeader(new Header("Accept", "application/json"));
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

            // Execute the method.
            int statusCode = CLIENT.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("Method failed: " + getMethod.getStatusLine());
            }

            responseBody = getMethod.getResponseBodyAsStream();

            getMethod.releaseConnection();


        } catch (IOException e) {
            e.printStackTrace();
        }


        // Create the JSON object.
        JsonObject response = null;
        try {

            response = new JsonParser().parse(new InputStreamReader(responseBody)).getAsJsonObject();

        } catch (Exception e) {
            System.out.println("****");
            e.printStackTrace();
            System.out.println(responseBody);
            System.out.println("****");
        }
        return response;
    }

    /**
     * @param input
     * @return
     */
    private List<Annotation> process(String text) {
        JsonObject response = getAnnotatedText(text);
        JsonArray jarray = response.getAsJsonArray("Resources");
        List<Annotation> annotatedEntities = new ArrayList<Annotation>();
        Gson gson = new Gson();
        if (jarray != null)
            for (JsonElement jres : jarray) {
                Annotation ann = gson.fromJson(jres, Annotation.class);
                ann.setWikid();
                if (Character.isUpperCase(ann.getSurfaceForm().charAt(0)) ||
                    Character.isDigit(ann.getSurfaceForm().charAt(0)))
                    annotatedEntities.add(ann);
            }
        return annotatedEntities;
    }

    /**
     * Returns a list of pairs composed by surface form and related entity.
     * Entities are marked with PE or SE based on the input PE given.
     *
     * @param text
     * @param PE
     * @return
     */
    private List<Pair<String, String>> getAnnotations(String text, String PE) {
        List<Annotation> annotations = process(text);
        List<Pair<String, String>> textualAnnotations = new ArrayList<Pair<String, String>>();
        for (Annotation an : annotations) {
            if (an.getWikid().equals(PE))
                textualAnnotations.add(Pair.make(an.getSurfaceForm(), an.toString("PE")));
            else
                textualAnnotations.add(Pair.make(an.getSurfaceForm(), an.toString("SE")));
        }
        return textualAnnotations;
    }

    /**
     * To match a name it has to be in a sentence sourrounded by two boarders (\\b) that are
     * not square bracket, _ or pipe | (which are terms that are inside a wikilink).
     * <p>
     * https://regex101.com/r/qdZyYl/4
     *
     * @param name
     * @return
     */
    private String createRegexName(String name) {
        return "(\\s[^\\sA-Z]++\\s|(?:^|\\. |, |: |\\n)(?:\\w++\\s)?)\\b(" + Pattern.quote(name) + ")\\b(?!\\s[A-Z][a-z]++|-|<| <)";
    }

    /**
     * @param sentence
     * @param replacement
     * @param pattern
     * @return
     * @throws Exception
     */
    private String applyRegex(String sentence, String replacement, String pattern) throws Exception {
        StringBuffer tmp = new StringBuffer();
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(sentence);
            while (m.find()) {
                // we attached the part of text before the entities (m.group(1)) and then the entity replaced.
                m.appendReplacement(tmp, Matcher.quoteReplacement(m.group(1)) + Matcher.quoteReplacement(replacement));
            }
            m.appendTail(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        return tmp.toString();
    }

    /**
     * @param block
     * @param PE
     * @return
     */
    public synchronized List<String> annotateText(String block, String PE) {
        List<String> sentences = new LinkedList<String>();

        for (String part : StupidNLP.splitSentence(block)) {
            List<Pair<String, String>> annotations = getAnnotations(part, PE);

            List<Pair<String, String>> regex2entity = new ArrayList<Pair<String, String>>();
            for (Pair<String, String> entity : annotations) {
                regex2entity.add(Pair.make(createRegexName(entity.key), entity.value));
            }

            Collections.sort(regex2entity, new PatternComparator());
            String annotatedBlock = part;

            for (Pair<String, String> regex : regex2entity) {
                try {

                    annotatedBlock = applyRegex(annotatedBlock, regex.value, regex.key);

                } catch (Exception e) {
                    System.out.println("Exception in:	" + regex.value);
                    System.out.println("using the regex:	" + regex.key);
                    System.out.println("--------------------------------------------------");
                    break;
                }
            }
            sentences.add(annotatedBlock);
        }
        return sentences;
    }

    public static void main(String[] args) {
        Configuration.init(new String[0]);
        DBPediaSpotlight spot = new DBPediaSpotlight(0.5, 0);
        System.out.println(spot.annotateText("Berners-Lee was born in <SE-AUG<London>>, <SE-AUG<England>>, one of four children born to "
            + "<SE-AUG<Mary_Lee_Woods>> and Conway Berners-Lee. His parents worked on the first commercially built computer, the "
            + "Ferranti Mark 1.", "Tim_Berners-Lee"));
        spot.killProcess();

    }


}