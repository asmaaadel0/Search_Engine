package indexer;

import database.mongoDB;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import ca.rmen.porterstemmer.PorterStemmer;

public class Indexer {
    mongoDB db;

    //PorterStemmer porterStemmer = new PorterStemmer();
    //String stem = porterStemmer.stemWord("incorporated");


    HashMap<String, List<Document>> words_map;
    HashMap<String, Integer> pages_map;
    HashMap<String, Double> rank_map;

    //constructor for the indexer
    public Indexer(mongoDB DB) {
        db = DB;
        words_map = new HashMap<String, List<Document>>();

        pages_map = new HashMap<String, Integer>();

        rank_map = new HashMap<String, Double>();


    }

    // extract words from file
    public static List<String> filetokinzer(String str) {


        //return new ArrayList<String>(Arrays.asList(str.toLowerCase().split(" ")));
        StringTokenizer strtoken = new StringTokenizer(str);
        List<String> filetoken = new ArrayList<String>();
        while (strtoken.hasMoreElements()) {
            filetoken.add(strtoken.nextToken());
        }
        return filetoken;
    }

    //remove stop words
    public static void removeStopWords(List<String> page) throws IOException {
        List<String> stop_words = Files.readAllLines(Paths.get("C:\\Users\\Asmaa Adel\\Desktop\\demo\\stop_words.txt"));
        page.removeAll(stop_words);
    }


    //upodate fata pase of indeing
    public void updateIndexerDB() {
        for (String word : words_map.keySet()) {
            db.add_new_word(word, words_map.get(word));
        }

        for (String url : pages_map.keySet()) {
            db.add_new_indexed_page(url, pages_map.get(url));

        }

        for (String url : rank_map.keySet()) {
            db.Indexed.updateOne(new Document("url", url), new Document("$set", new Document("page rank", rank_map.get(url))));

        }
    }


    // index the web page
    public void Index(String page, String page_url, double page_rank, int id) throws IOException {
        // check if the page is indexed before

        if (db.url_indexed_before(page_url)) {
            System.out.println("------The page is already indexed -------");

            return;

        } else {

            System.out.println("------- Index this page ----------");
        }


        //string and integer
        //string:word   ,integer:count

        HashMap<String, Integer> words_tf = new HashMap<String, Integer>();
        //clean the words to be indexed
        page = page.replaceAll("[^a-zA-Z1-9]", " ");// remove single chars and digits

        List<String> words = filetokinzer(page);


        //remove stop words
        removeStopWords(words);

        int wordsCount_inpage = words.size();

        //loop on array of words
        //create the in inverted file
        //calculate word -> TF
        for (String i : words) {
            if (i.length() != 0) {
                PorterStemmer porterStemmer = new PorterStemmer();
                String stem = porterStemmer.stemWord(i);
                // System.out.print(stem+" \n");
                //if the word is repeated
                if (words_tf.containsKey(stem)) {
                    words_tf.put(stem, words_tf.get(stem) + 1);//word already exists increase Tf
                } else {
                    words_tf.put(stem, 1); //newly added
                }
            }
        }

        //
//create a document for each word and put in it the page url ,Tf
        for (String i : words_tf.keySet()) {
            Document d = new Document();
            d.append("url", page_url);
            d.append("id", id);
            d.append("page_rank", page_rank);
            d.append("TF", words_tf.get(i) / (float) wordsCount_inpage); //normalize TF
            if ((words_tf.get(i) / (float) wordsCount_inpage) < 0.5) {
                if (words_map.containsKey(i)) //if word is already exist
                {
                    words_map.get(i).add(d); //add document to array of documents
                } else {
                    List<Document> arr = new ArrayList<Document>(); //page isn't found so make new array of documents
                    arr.add(d); //add doucument to array
                    words_map.put(i, arr);
                }
            }
        }

        rank_map.put(page_url, page_rank);
        pages_map.put(page_url, words.size()); // insert urls and words count in it

    }


    //words_tf -> (word,TF)
    //words_map ->(word,document)
    //document(url,NTF)
    //NTF ->normalized term frequency ==TF /(words count in page )
    //pages_map -> (url,words count per page)


}








