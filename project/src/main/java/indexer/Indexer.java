package indexer;
import database.mongoDB;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

///////////////////////

import ca.rmen.porterstemmer.PorterStemmer;

public class Indexer
{
    mongoDB db;

    HashMap<String, List<Document>> words_map;
    HashMap<String, Integer> pages_map;
    HashMap<String, Double> rank_map;
    static HashMap<String, List<Integer>> all_words;

    //constructor for the indexer
    public Indexer(mongoDB DB)

    {
        db = DB;
        words_map  = new HashMap<String, List<Document>>();

        pages_map= new HashMap<String, Integer>();

        rank_map= new HashMap<String, Double>();

        all_words  = new HashMap<String, List<Integer>>();

    }

    // extract words from file
    public static List<String> filetokinzer(String str) {


        //return new ArrayList<String>(Arrays.asList(str.toLowerCase().split(" ")));
        StringTokenizer strtoken = new StringTokenizer(str);
        List<String> filetoken = new ArrayList<String>();
        int i=0;
        while(strtoken.hasMoreElements())
        {
            String s=strtoken.nextToken();
            filetoken.add(s);

            if(all_words.containsKey(s))
            {
                List<Integer> arr1=all_words.get(s);

                arr1.add(i);
                all_words.put(s, arr1);

            }
            else
            {

                List<Integer> arr=new ArrayList<Integer>();
                arr.add(i);
                all_words.put(s, arr);

            }
            i++;
        }


        return filetoken;
    }

    /*public static List<String> filetokinzer(String str) {


        //return new ArrayList<String>(Arrays.asList(str.toLowerCase().split(" ")));
    	StringTokenizer strtoken = new StringTokenizer(str);
    	List<String> filetoken = new ArrayList<String>();
    	while(strtoken.hasMoreElements()){
    	filetoken.add(strtoken.nextToken());
    	}
    	return filetoken;
    }*/

    //remove stop words
    public static void removeStopWords(List<String> page) throws IOException {
        List<String> stop_words= Files.readAllLines(Paths.get("C:\\Users\\Asmaa Adel\\Desktop\\demo\\stop_words.txt"));
        page.removeAll(stop_words);
    }


    //update database of indexer
    public void updateIndexerDB() {
        for (String word :  words_map.keySet()) {
            db.add_new_word(word,  words_map.get(word));
        }

        for (String url : pages_map.keySet())
        {
            db.add_new_indexed_page(url, pages_map.get(url));

        }

        for (String url : rank_map.keySet())
        {
            db.Indexed.updateOne(new Document("url",url),new Document("$set",new Document("page rank",rank_map.get(url) )));

        }

        for(String word :  all_words.keySet())
        {
            List<Integer> arr=all_words.get(word);
            for(int j=0;j<arr.size();j++)
                System.out.printf("word:%s,index:%d  ",word,arr.get(j));
            System.out.print(" \n");

        }
    }



    // index the web page
    public void Index(String page, String page_url,double page_rank,int id) throws IOException
    {
        // check if the page is indexed before

        if (db.url_indexed_before(page_url))
        {
            System.out.println("------The page is already indexed -------");

            return;

        }
        else {

            System.out.println("------- Index this page ----------");
        }


        //string and integer
        //string:word   ,integer:count
        ///////////////////////////////////////////////












        ////////////////////////////////////////////

        HashMap<String, Document> words_tf= new HashMap<String, Document>();
        //clean the words to be indexed
        page = page.replaceAll("[^a-zA-Z1-9]", " ");

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
                    Document d = new Document();

                    List<Integer> arr=all_words.get(i);
                    int l=arr.size();

                    d.append("TF",  words_tf.get(stem).getInteger("TF")+1);


                    words_tf.put(stem, d);//word already exists increase Tf

                } else {

                    List<Integer> arr=all_words.get(i);


                    int l=arr.size();

                    Document d = new Document();

                    d.append("TF",1);

                    words_tf.put(stem, d); //newly added
                }
            }
        }

        //
//create a document for each word and put in it the page url ,Tf
        for (String i : words_tf.keySet())
        {
            Document d = new Document();
            d.append("url", page_url);
            d.append("id", id);
            d.append("page_rank",page_rank );
            d.append("TF", words_tf.get(i).getInteger("TF") / (float) wordsCount_inpage); //normalize TF
            d.append("index",words_tf.get(i).getList("index",Integer.class));
            d.append("is_stemmed",words_tf.get(i).getList("stemmed",Integer.class));

            //if the page isn't spam
            if( (words_tf.get(i).getInteger("TF") / (float) wordsCount_inpage)< 0.5)
            {
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

        rank_map.put(page_url,page_rank );
        pages_map.put(page_url, words.size()); // insert urls and words count in it

    }



    //words_tf -> (word,TF)
    //words_map ->(word,document)
    //document(url,NTF)
    //NTF ->normalized term frequency ==TF /(words count in page )
    //pages_map -> (url,words count per page)







}








