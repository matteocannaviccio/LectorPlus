package it.uniroma3.model.console;
/**
 * 
 * @author matteo
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.model.console.ModelIndexer.IndexType;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;

/**
 * 
 * @author matteo
 *
 */
public class Console {

  // commands
  enum Command {
    load, quit, help, relprior, relrank, dbp, relex, model, tRc, pRc, tpRc, pTRc
  };

  // usage
  private static String usage = String
      .format("%-20s %-65s %s\n", "[HELP]", "/h", "prints this help message").toString()
      + String.format("%-20s %-65s %s\n", "[QUIT]", "/q", "quits").toString()
      + String.format("%-20s %-65s %s\n", "[prediction]",
          "/p/<[typeSubject]>/<phrase>/<[typeObject]>", "compute naive bayes prediction").toString()
      + String.format("%-20s %-65s %s\n", "[relation count]", "/rc/<relation>",
          "return the COUNT of the relation").toString()
      + String.format("%-20s %-65s %s\n", "[relation prior]", "/rp/<relation>",
          "return the PRIOR of the relation").toString()
      + String.format("%-20s %-65s %s\n", "[phrase -> types, relations]", "/ptlf/<phrase>",
          "return the typed relations that label the phrase").toString()
      + String.format("%-20s %-65s %s\n", "[phrase/rel count]",
          "/ptc/<[typeSubject]>/<phrase>/<[typeObject]>",
          "return the count of typed phrase with each relation").toString();

  /*
   * 
   */
  public static void main(String[] args) {
    Configuration.init(new String[0]);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
    Configuration.updateParameter("language", "en");

    final int PERCENTAGE_UNL = 100;
    ModelIndexer model_indexes = new ModelIndexer(
        Configuration.getLectorFolder() + "/model_indexer_" + PERCENTAGE_UNL + "/", PERCENTAGE_UNL);

    boolean running = true;
    String[] token = null;

    Model modelOnlyPositive = null;
    Model modelWithNone = null;
    Model modelTextExt = null;
    Model modelMiddle = null;

    System.out.println("type /h for help; /q to quit\n");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    while (running) {
      try {
        System.out.print("> ");
        String line = br.readLine();

        token = line.split("/");

        Command cmd = Command.valueOf(token[1]);
        CounterMap<String> res = new CounterMap<String>();

        switch (cmd) {
          case quit: // QUIT
            running = false;
            break;

          case help: // HELP
            System.out.println(usage);
            break;

          case relprior: // RELATION PRIOR
            String input = token[2];
            System.out.printf("\t%-20s %-10s %s\n", "relation", "--->", input);
            int occ = model_indexes.getKeyCount(input, IndexType.relations);
            System.out.printf("\t%-20s %-10s %s\n", "occ.", "--->", occ);
            double prior = (double) occ / model_indexes.getTotRelations();
            System.out.printf("\t%-20s %-10s %s\n", "prior", "--->", prior);
            break;

          case relrank: // RELATION RANKING
            CounterMap<String> rels =
                model_indexes.matchAllSimple(IndexType.relations2typedphrases);
            for (Map.Entry<String, Integer> r : Ranking.getRanking(rels).entrySet()) {
              System.out.println(r.getKey() + "\t" + r.getValue());
            }
            break;

          case dbp: // RELATION EXAMPLES
            input = token[2]; // relation
            Lector.init("FE");
            Lector.getDBPedia().getSomeFacts(input, 25);
            Lector.close();
            break;

          case relex: // RELATION EXAMPLES
            String amount = token[2]; // relation
            int limit = -1;
            if (!amount.equals("all"))
              limit = Integer.parseInt(amount);
            input = token[3]; // relation
            res = model_indexes.getKeyValuesCounts(input, IndexType.relations2typedphrases);

            /*******************/

            int max = 0;
            Map<String, Integer> ranked_typed_phrases = Ranking.getTopKRanking(res, limit);
            for (Map.Entry<String, Integer> typh : ranked_typed_phrases.entrySet())
              if (typh.getKey().length() > max)
                max = typh.getKey().length();
            int spanRel = input.length() + 5;

            System.out.printf(
                "\t%-5s %-5s %-" + spanRel + "s %-" + max + "s %-15s %-15s %-15s %-15s %-15s %s\n",
                "", "", "RELATION", "TYPED-PHRASE", "", "freq.", "%rel", "%LAB", "%UNL",
                "c(tp, TOT)");
            System.out.println(
                "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            /*******************/

            int count = 1;
            for (Map.Entry<String, Integer> typh : ranked_typed_phrases.entrySet()) {
              CounterMap<String> typ_rels =
                  model_indexes.getKeyValuesCounts(typh.getKey(), IndexType.typedphrases2relations);

              // prendi la typed phrase solo se identifica davvero la relazione in input (evita
              // falsi positivi di tipo 2)
              int tot = typ_rels.calculateSum();
              boolean firstNone = false;
              if (Ranking.getRanking(typ_rels).entrySet().iterator().next().getKey()
                  .equals(input)) {
                firstNone = true;
              }
              typ_rels.remove("NONE");
              int labeled = typ_rels.calculateSum(); // now without NONE
              if (Ranking.getRanking(typ_rels).entrySet().iterator().next().getKey()
                  .equals(input)) {
                double lab = (double) labeled / tot;
                double unl = (double) (tot - labeled) / tot;
                double rel = (double) typh.getValue() / tot;
                int freq = typh.getValue();
                if (firstNone)
                  System.out.printf(
                      "\t%-5s %-5s %-" + spanRel + "s %-" + max
                          + "s %-15s %-15s %-15s %-15s %-15s %s\n",
                      count + ")", "*", input, typh.getKey().replaceAll("\t", " "), "--->", freq,
                      String.format("%.2f", rel), String.format("%.2f", lab),
                      String.format("%.2f", unl), tot);
                else
                  System.out.printf(
                      "\t%-5s %-5s %-" + spanRel + "s %-" + max
                          + "s %-15s %-15s %-15s %-15s %-15s %s\n",
                      count + ")", "", input, typh.getKey().replaceAll("\t", " "), "--->", freq,
                      String.format("%.2f", rel), String.format("%.2f", lab),
                      String.format("%.2f", unl), tot);
                count += 1;
              }
            }
            break;

          case pRc: // PHRASE -> RELATION COUNT
            String phrase = token[2];
            res = model_indexes.getKeyValuesCounts(phrase, IndexType.phrases2relations);
            System.out.println("List of relations associated with the phrase: " + phrase);
            if ((res == null || res.isEmpty()))
              System.out.println("Found: 0 instances.");
            else {
              int total = res.calculateSum();
              System.out.println("Found: " + res.size() + " with a total of " + total);
              for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 50).entrySet()) {
                System.out.printf("\t%-10s %-20s %-25s %-20s %s\n", rel.getValue(),
                    String.format("%.2f", (double) rel.getValue() / total), rel.getKey(), "---->",
                    phrase);
              }
            }
            break;

          case pTRc: // PHRASE -> TYPES AND RELATIONS COUNT
            phrase = token[2];
            res = model_indexes.getKeyValuesCounts(phrase, IndexType.phrases2typedrelations);
            System.out.println(
                "List of relations and pairs of types associated with the phrase: " + phrase);
            if ((res == null || res.isEmpty()))
              System.out.println("Found: 0 instances.");
            else {
              int total = res.calculateSum();
              System.out.println("Found: " + res.size() + " with a total of " + total);
              for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 50).entrySet()) {
                String subjectType = null;
                String objectType = null;
                String relation = null;
                if (rel.getKey().split("\t").length == 3) {
                  subjectType = rel.getKey().split("\t")[0];
                  relation = rel.getKey().split("\t")[1];
                  objectType = rel.getKey().split("\t")[2];
                } else {
                  subjectType = rel.getKey().split("\t")[0];
                  relation = "NONE";
                  objectType = rel.getKey().split("\t")[1];
                }
                System.out.printf("\t%-10s %-20s %-25s %-20s %-20s %-15s %s\n", rel.getValue(),
                    String.format("%.2f", (double) rel.getValue() / total), relation, "---->",
                    subjectType, phrase, objectType);
              }
            }
            break;

          case tpRc: // TYPED PHRASE -> RELATION COUNT
            input = token[2];
            String[] tokens = input.split(" ");

            /********************** Parse typed phrase **********************/
            String subType = tokens[0];
            if (!(tokens[0].startsWith("[") && tokens[0].endsWith("]")))
              subType =
                  "[" + tokens[0].substring(0, 1).toUpperCase() + tokens[0].substring(1) + "]";

            String objType = tokens[tokens.length - 1];
            if (!(tokens[tokens.length - 1].startsWith("[")
                && tokens[tokens.length - 1].endsWith("]")))
              objType = "[" + tokens[tokens.length - 1].substring(0, 1).toUpperCase()
                  + tokens[tokens.length - 1].substring(1) + "]";

            phrase = StringUtils.join(Arrays.copyOfRange(tokens, 1, tokens.length - 1), " ");
            String typedphrase = subType + "\t" + phrase + "\t" + objType;
            /**********************************************************************************/

            int totNONE = 0;
            int totLAB = 0;
            res = model_indexes.getKeyValuesCounts(typedphrase, IndexType.typedphrases2relations);
            System.out.println("List of relations associated with the typed phrase: " + subType
                + " " + phrase + " " + objType);
            if ((res == null || res.isEmpty()))
              System.out.println("Found: 0 instances.");
            else {
              int total = res.calculateSum();
              System.out.println("Found: " + res.size() + " with a total of " + total);
              int others = res.get("NONE");
              for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 50).entrySet()) {
                int pos = rel.getValue();
                int samples = total - others;
                double p = (double) pos / samples;
                double variance = 2 * Math.sqrt(
                    ((double) (p * (1 - p)) / samples) * (double) (total - samples) / (total - 1));
                if (!rel.getKey().equals("NONE")) {
                  totLAB += rel.getValue();
                  System.out.printf("\t%-10s %-10s %-20s %-25s %-20s %s\n", rel.getValue(),
                      String.format("%.2f", (double) rel.getValue() / total),
                      String.format("%.2f", p) + " +- " + String.format("%.3f", variance),
                      rel.getKey(), "---->", subType + " " + phrase + " " + objType);
                } else {
                  totNONE += rel.getValue();
                  System.out.printf("\t%-10s %-10s %-20s %-25s %-20s %s\n", rel.getValue(),
                      String.format("%.2f", (double) rel.getValue() / total), "-", rel.getKey(),
                      "---->", subType + " " + phrase + " " + objType);
                }
              }
            }

            System.out.println("Tot Lab = " + totLAB);
            System.out.println("Tot Unl = " + totNONE);


            break;

          case load:
            // System.out.println("\t-> Init TextExt Model ...");
            // modelTextExt = Model.getNewModel(new DBModel(Configuration.getDBModel()),
            // "model_triples", 1, 100, ModelType.ModelTextExt, true);

            System.out.println("\t-> Init OnlyPositive Model ...");
            modelOnlyPositive = Model.getNewModel(new DBModel(Configuration.getDBModel()),
                "model_triples", 1, 0, ModelType.ModelNaiveBayes, 0.4);

            System.out.println("\t-> Init WithNone Model ...");
            modelWithNone = Model.getNewModel(new DBModel(Configuration.getDBModel()),
                "model_triples", 1, 100, ModelType.ModelNaiveBayes, 0.4);

            System.out.println("\t-> Init Middle Model ...");
            modelMiddle = Model.getNewModel(new DBModel(Configuration.getDBModel()),
                "model_triples", 1, 25, ModelType.ModelNaiveBayes, 0.4);
            break;

          case model: // TYPED PHRASE -> RELATION COUNT

            if (modelOnlyPositive == null) {
              System.out.println("Load a model first (with the command /load).");
              break;
            }

            input = token[2];
            tokens = input.split(" ");

            /********************** Parse typed phrase **********************/
            subType = tokens[0];
            if (!(tokens[0].startsWith("[") && tokens[0].endsWith("]")))
              subType =
                  "[" + tokens[0].substring(0, 1).toUpperCase() + tokens[0].substring(1) + "]";

            objType = tokens[tokens.length - 1];
            if (!(tokens[tokens.length - 1].startsWith("[")
                && tokens[tokens.length - 1].endsWith("]")))
              objType = "[" + tokens[tokens.length - 1].substring(0, 1).toUpperCase()
                  + tokens[tokens.length - 1].substring(1) + "]";

            phrase = StringUtils.join(Arrays.copyOfRange(tokens, 1, tokens.length - 1), " ");
            typedphrase = subType + "\t" + phrase + "\t" + objType;
            /**********************************************************************************/

            System.out.println("Prediction for : " + typedphrase);

            System.out.println("Model Only Positive");
            System.out.println(modelOnlyPositive.predict(subType, phrase, objType));
            System.out.println(" ");

            System.out.println("Model With None");
            System.out.println(modelWithNone.predict(subType, phrase, objType));
            System.out.println(" ");

            System.out.println("Model Middle");
            System.out.println(modelMiddle.predict(subType, phrase, objType));
            System.out.println(" ");

            // System.out.println("Model TextExt");
            // System.out.println(modelTextExt.predict(subType, phrase, objType));
            // System.out.println(" ");

            break;

          case tRc: // TYPED PHRASE -> RELATION COUNT
            input = token[2];
            tokens = input.split(" ");
            subType = "[" + tokens[0].substring(0, 1).toUpperCase() + tokens[0].substring(1) + "]";
            objType = "[" + tokens[tokens.length - 1].substring(0, 1).toUpperCase()
                + tokens[tokens.length - 1].substring(1) + "]";
            String types = subType + "\t" + objType;

            res = model_indexes.getKeyValuesCounts(types, IndexType.types2relations);
            System.out.println(
                "List of relations associated with the typed phrase: " + subType + " " + objType);
            if ((res == null || res.isEmpty()))
              System.out.println("Found: 0 instances.");
            else {
              int total = res.calculateSum();
              System.out.println("Found: " + res.size() + " with a total of " + total);
              for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 50).entrySet()) {
                System.out.printf("\t%-10s %-20s %-25s %-20s %s\n", rel.getValue(),
                    String.format("%.2f", (double) rel.getValue() / total), rel.getKey(), "---->",
                    subType + " " + objType);
              }
            }
            break;

          default:
            System.out.println("unrecognized command /'" + cmd + "'/. try again.");
            break;
        }

      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("ERROR! try again.");
      }
    }

  }


}
