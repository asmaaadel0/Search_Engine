package crawler;
import database.mongoDB;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class SpiderMain {
    //static Spider spider;
    public Spider spider;
    public static void main(String[] args)throws Exception
    //public void CRAWLER_ROOT(mongoDB DB,int num_of_threads)throws Exception
    {
        //mongoDB DB = new mongoDB("A");
        mongoDB DB = new mongoDB("A");
        ///
        //first get the collection "Crawlled" size
        Spider spider=new Spider(DB);
        //spider=new Spider(DB);
        int current_ids=0;
        int current_crawled=0;
        int MAX_COUNT=0;
        boolean start_new_crawl = true;
        MongoCollection<Document> Crawel_collection = DB.get_db().getCollection("Crawlled");
        //if it is >0
        //int col_size = Crawel_collection.dataSize();
        //if(col_size>0)current_crawled

        //Crawel_collection will be null if there is an error:m.s
        if(Crawel_collection != null)
        {
            //System.out.println("Crawled collection size =  "+col_size);
            //get the count_crawled
            Document doc = Crawel_collection.find(Filters.eq("id", 0 )).first();
            if(doc == null)
            {
                // System.out.println("ops!! can not retreive crawled document count , reRun please");
            }
            else
            {
                // current_crawled=doc.getInteger("count_crawled");
                current_crawled=doc.getInteger("is_crawled");
                MAX_COUNT=doc.getInteger("MAX_COUNT");
                current_ids=doc.getInteger("count_id");
                System.out.println("is_crawled ="+current_crawled+"current_ids= "+current_ids);
                //if it != "Crawlled" size -> complete crawling
                // if(current_crawled != current_ids) {
                if(MAX_COUNT != current_ids && current_ids !=0){
                    //Spider spider = new Spider(seeds);
                    spider.setCrawlContPar(current_ids, current_crawled,MAX_COUNT);
                    start_new_crawl = false;

                    //download pagevisited and keys_checks
                    Iterator<Document> keys_iterator= DB.getAllkdocs().iterator();
                    Document doc_current= null;
                    //String current_key;
                    while(keys_iterator.hasNext()){
                        doc_current=keys_iterator.next();
                        // current_key=doc_key.getString("key");
                        spider.keys_check.add(doc_current.getString("key"));
                        spider.pagesVisited.put(doc_current.getString("url"),doc_current.getInteger("id"));
                    }
                    // System.out.println("hereee!!");
                }
                //else -> drop the old collection , start a new crawling
                else
                {
                    Crawel_collection.drop();
                    start_new_crawl=true;
                    // System.out.println("else --- hereee!!");
                }
            }

        }
        //else
        if(start_new_crawl == true)
        {
            //set MAX_COUNT to new crawling
            //  MAX_COUNT=5000;
            MAX_COUNT=100;
            Document crawl_count_doc = new Document("id", 0).append("count_crawled", 0).
                    append("count_id", 0).append("is_crawled",0).append("MAX_COUNT",MAX_COUNT);
            Crawel_collection.insertOne(crawl_count_doc);
            List<String> seeds = new LinkedList<String>();
            seeds.add("https://www.geeksforgeeks.org/");
            seeds.add("https://www.javatpoint.com/");

            // seeds.add("https://www.youm7.com/");
            seeds.add("https://egypt.souq.com/eg-en/");
            seeds.add("https://www.amazon.com/");
            //seeds.add("https://cu.edu.eg/ar/");
            seeds.add( "https://www.nytimes.com/");
            seeds.add("https://en.wikipedia.org/w/index.php?search=");
            seeds.add("https://www.facebook.com/");
            seeds.add("https://www.linkedin.com/home");
            seeds.add("https://www.hackerrank.com/");

            //Spider spider = new Spider(seeds);
            spider.setCrawlNewPar(seeds,MAX_COUNT);
        }

        System.out.print("Enter the number of threads:");
        Scanner keyboard = new Scanner(System.in);
        int num_of_threads=keyboard.nextInt();
        if (num_of_threads < 1) {
            System.out.println("Invalid number of threads");
            System.out.println("Running the Crawler in a Single thread");
            num_of_threads = 1;
        }
        Thread[]crawlers=new Thread[num_of_threads];
        for(int i=0;i<num_of_threads;i++) {
            crawlers[i]=new Thread(spider);
            crawlers[i].setName(String.valueOf(i+1));
            crawlers[i].start();
        }
        for (int i=0;i<num_of_threads;i++)
            crawlers[i].join();

//        for(int i = 0 ; i< spider.keys_check.size();i++)
//        {
        //       System.out.println(spider.keys_check.get(i)+"  ----  "+spider.pagesVisited.containsValue(i+1));
//        }
//        int i=0;
//        Iterator itr = spider.keys_check.iterator();
//
//        // check element is present or not. if not loop will
//        // break.
//        while (itr.hasNext()) {
//            System.out.println(itr.next() + "  ----  " + spider.pagesVisited.containsValue(i + 1));
//            i++;
//            //  System.out.println("Finished Crawling Successfully..");
//        }

        System.out.println("Finished Crawling Successfully..");


    }

}
