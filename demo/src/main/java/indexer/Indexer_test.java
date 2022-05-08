package indexer;

import database.mongoDB;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Sorts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Indexer_test {

    public static void main(String[] args) throws IOException {
        // Connect to the database
        mongoDB DB = new mongoDB("A");
        // mongoDB DB = new mongoDB("B");

        //


//        List<Integer> arr = new ArrayList<Integer>(); //page isn't found so make new array of documents
//        arr.add(1); //add doucument to array
//        arr.add(2);
//
        //insert new word in words collection ->dictionary
//        List<Integer> arr1 = new ArrayList<Integer>(); //page isn't found so make new array of documents
//        arr1.add(2);
//
//        DB.insertPage("koko", 1, "koko.com","hello koko okokokokokokoko sow are you",arr1);
//        List<Integer> arr2 = new ArrayList<Integer>(); //page isn't found so make new array of documents
//        arr2.add(5); //add doucument to array
//
//
//       DB.insertPage("nono", 2, "nono.com", "eucation  eess ies cats computation",arr2 );
//
//
//
//       List<Integer> arr3 = new ArrayList<Integer>(); //page isn't found so make new array of documents
//       arr3.add(1); //add doucument to array
//       arr3.add(2);
//       arr3.add(4); //add doucument to array
//       arr3.add(5);
//
//       DB. insertPage("dodo", 3, "dodo.com", "ko ko ko ko ko ko ko",arr3 );
//
//       List<Integer> arr4 = new ArrayList<Integer>(); //page isn't found so make new array of documents
//        arr4.add(3); //add doucument to array
//        arr4.add(5);
//
//       DB. insertPage("lolo", 4, "lolo.com", "lololololololy ",arr4 );
//
//
//      List<Integer> arr5 = new ArrayList<Integer>(); //page isn't found so make new array of documents
//     arr5.add(4); //add doucument to array
//
//     DB. insertPage("soso", 5, "soso.com", "saw saw saw  ",arr5 );

//
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 for new Indexing \n");
        int ans = sc.nextInt();
        boolean update = ans == 2;


        // Create an instance from the Indexer
        Indexer webIndexerMain = new Indexer(DB);

        // Retrieve all the crawled pages from the DB
        Iterator<Document> CrawlerCollectionItr = DB.getAllPages().iterator();

        // Loop through the crawled pages and index them
        int i = 1;
        while (CrawlerCollectionItr.hasNext()) {
            Document d = CrawlerCollectionItr.next();
            String page = d.getString("content");
            String url = d.getString("url");
            Double page_rank = d.getDouble("page_rank");
            Integer id = d.getInteger("id");
            //System.out.print(page_rank);
            System.out.printf("index page: %d url:%s \n", i, url);
            webIndexerMain.Index(page, url, page_rank, id);
            i++;
        }

        // Save the indexed words in the DB


        webIndexerMain.updateIndexerDB();
        System.out.println("Update Database...");
        //for sorting result
       /* FindIterable<Document> iterDoc = DB.Indexed.find().sort(Sorts.ascending("page rank"));
        Iterator it = iterDoc.iterator();
        while (it.hasNext()) {
           System.out.println(it.next());
        }*/

        if (update) {

            DB.updateAllIDF();
            System.out.println("Update IDF...");
        }
    }

    // test ranker


}
