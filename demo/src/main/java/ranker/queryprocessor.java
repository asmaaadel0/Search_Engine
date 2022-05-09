package ranker;

import ca.rmen.porterstemmer.PorterStemmer;
import database.mongoDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class queryprocessor {
    mongoDB db;
    static List<String> wordsfound = new ArrayList<String>();
    private PorterStemmer stemmer;
    private String query;

    public queryprocessor(String query) {
        this.query = query;
    }

    public queryprocessor(mongoDB DB, String query) {
        this.query = query;
        this.db = DB;
        stemmer = new PorterStemmer();
    }

    queryprocessor(mongoDB DB) {
        this.db = DB;
        stemmer = new PorterStemmer();
    }

    //remove stop words
    public static void removeStopWords(List<String> page) throws IOException {
        List<String> stop_words = Files.readAllLines(Paths.get("C:\\Users\\Asmaa Adel\\Desktop\\demo\\stop_words.txt"));
        page.removeAll(stop_words);
    }

    //convert words to their stems
    public String stem(String word) {
        try {
            return stemmer.stemWord(word);
        } catch (ArrayIndexOutOfBoundsException ignore) {
            return null;
        }
    }

    //conevert query to words
    public static List<String> filetokenizer(String str) {
        StringTokenizer strtoken = new StringTokenizer(str);
        List<String> filetoken = new ArrayList<String>();
        while (strtoken.hasMoreTokens()) {
            filetoken.add(strtoken.nextToken());
        }
        return filetoken;
    }

    public void processes(String query, List<String> allwords) throws IOException {
        query = query.replaceAll("[^a-zA-Z1-9]", " ");// remove single chars and digits
        query = query.toLowerCase(Locale.ROOT);
        List<String> words = filetokenizer(query);
        removeStopWords(words);
        for (String i : words) {
            i = stem(i);
            if (allwords.contains(i))
                wordsfound.add(i);

        }
    }

    //return words found in query in database
    public static List<String> getwordsfound() {
        return wordsfound;
    }

    public String getquery() {
        return query;
    }
}