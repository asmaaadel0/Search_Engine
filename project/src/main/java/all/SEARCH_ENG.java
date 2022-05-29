package all;

import crawler.SpiderTest;
import database.mongoDB;
import indexer.Indexer;
import org.bson.Document;
import ranker.Ranker;
import ranker.Result;
import ranker.queryprocessor;

import java.io.IOException;
import java.util.*;

public class SEARCH_ENG implements Runnable{
    static mongoDB DBL;
    static mongoDB DB1=new mongoDB("DB1");
    static mongoDB DB2=new mongoDB("DB2");
    volatile static boolean busy=false;
    @Override
    public void run() {
        if (Thread.currentThread().getName().equals("1")) {
//            DB1.get_db().drop();
//            DB2.get_db().drop();
            //first time to crawel and indexing
            SpiderTest craweling =new SpiderTest();
            System.out.print("Enter the number of threads:");
            Scanner keyboard = new Scanner(System.in);
            int num_of_threads=keyboard.nextInt();
            if (num_of_threads < 1) {
                System.out.println("Invalid number of threads");
                System.out.println("Running the Crawler in a Single thread");
                num_of_threads = 1;
            }
            //crawel
            try {

                craweling.CRAWLER_ROOT(DB1,num_of_threads);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //ranker
            Ranker webRanker = new  Ranker(DB1);

            webRanker.fill_matrix();
            webRanker.Rank();

            //indexer


            // Create an instance from the Indexer
            Indexer webIndexerMain = new Indexer(DB1);

            // Retrieve all the crawled pages from the DB
            Iterator<Document> CrawlerCollectionItr = DB1.getAllPages().iterator();

            // Loop through the crawled pages and index them
            int i = 1;
            while (CrawlerCollectionItr.hasNext()) {
                Document d = CrawlerCollectionItr.next();
                Integer id=d.getInteger("id");
                if(id==0)
                    continue;
                else
                {
                    String page = d.getString("content");
                    String url = d.getString("url");
                    Double page_rank=d.getDouble("page_rank");
                    // Integer id=d.getInteger("id");
                    System.out.print(page_rank);
                    System.out.printf("index page: %d url:%s \n", i, url);
                    try {
                        webIndexerMain.Index(page, url,page_rank,id);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    i++;
                }
            }

            // Save the indexed words in the DB


            webIndexerMain.updateIndexerDB();
            System.out.println("Update Database...");



            /////////////////////
            DBL=DB1;
            busy=true;
            while (true)
            {
                //DB_forever = new mongoDB("TEMPDB");
                if(DBL==DB1)
                {
                    //System.out.println("DBL==DB1");

                    //Recrawel
                    //craweling.spider.recrawling(DB2);
                    SpiderTest craweling1 =new SpiderTest();
                    //crawel
                    try {
                        craweling1.CRAWLER_ROOT(DB2,num_of_threads);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                    //Reranker
                    Ranker webRanker1 = new  Ranker(DB2);

                    webRanker1.fill_matrix();
                    webRanker1.Rank();


                    //Reindexer

                    // Create an instance from the Indexer
                    Indexer webIndexerMain1 = new Indexer(DB2);

                    // Retrieve all the crawled pages from the DB
                    Iterator<Document> CrawlerCollectionItr1 = DB2.getAllPages().iterator();

                    // Loop through the crawled pages and index them
                    int i1 = 1;
                    while (CrawlerCollectionItr1.hasNext()) {
                        Document d1 = CrawlerCollectionItr1.next();
                        Integer id1=d1.getInteger("id");
                        if(id1==0)
                            continue;
                        else
                        {
                            String page1 = d1.getString("content");
                            String url1 = d1.getString("url");
                            Double page_rank1=d1.getDouble("page_rank");

                            //System.out.print(page_rank);
                            //System.out.printf("index page: %d url:%s \n", i1, url1);
                            try {
                                webIndexerMain1.Index(page1, url1,page_rank1,id1);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            i1++;
                        }

                    }

                    // Save the indexed words in the DB


                    webIndexerMain1.updateIndexerDB();
                    System.out.println("Update Database...");

                    DBL=DB2;
                    // DB1.dropDatabase();
                    DB1.get_db().drop();

                    //System.out.println("swap");
                }

                else if(DBL==DB2)
                {
                    System.out.println("DBL==DB2");
                    //Recrawel
                    //  craweling.spider.recrawling(DB1);

                    SpiderTest craweling1 =new SpiderTest();
                    //crawel
                    try {
                        craweling1.CRAWLER_ROOT(DB1,num_of_threads);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    //Reranker
                    Ranker webRanker1 = new  Ranker(DB1);


                    webRanker1.fill_matrix();
                    webRanker1.Rank();


                    //Reindexer

                    // Create an instance from the Indexer
                    Indexer webIndexerMain1 = new Indexer(DB1);

                    // Retrieve all the crawled pages from the DB
                    Iterator<Document> CrawlerCollectionItr1 = DB1.getAllPages().iterator();

                    // Loop through the crawled pages and index them
                    int i1 = 1;
                    while (CrawlerCollectionItr1.hasNext()) {
                        Document d1 = CrawlerCollectionItr1.next();
                        Integer id1=d1.getInteger("id");
                        if(id1==0)
                            continue;
                        else
                        {
                            String page1 = d1.getString("content");
                            String url1 = d1.getString("url");
                            Double page_rank1=d1.getDouble("page_rank");

                            System.out.print(page_rank1);
                            System.out.printf("index page: %d url:%s \n", i1, url1);
                            try {
                                webIndexerMain1.Index(page1, url1,page_rank1,id1);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            i1++;
                        }
                    }
                    // Save the indexed words in the DB


                    webIndexerMain1.updateIndexerDB();
                    System.out.println("Update Database...");

                    DBL=DB1;
                    DB2.get_db().drop();
                    System.out.println("swap");

                }


//                    //finally :copy the database to the searching database
//                    synchronized (this)
//                    {
//                        DB_forever.copyDatabase("TEMPDB","SearchingDB");
//                    }
//                    //then delete the tempdb to full it again
//                    DB_forever.dropDatabase();
            }
        }
        if (Thread.currentThread().getName().equals("2")) {
            while (busy==false);
            System.out.println("runnn");
            while (true) {
                mongoDB DB = DBL;
                String query;
                String query_with_stop_words;
                HashMap<Integer, Document> temp_map1 = null; // mosh phrase
                HashMap<Integer, Document> temp_map2 = null;
                //List<String> All_words=new ArrayList<String>();
                Scanner sc = new Scanner(System.in);
                List<String> allwords = new ArrayList<String>();
                List<String> wordsfound = new ArrayList<String>();
                Iterator<Document> wordsCollectionItr = DB.getAllwords().iterator();
                System.out.println("Enter the query:");
                query = sc.nextLine();
                query_with_stop_words = query;
                while (wordsCollectionItr.hasNext()) {
                    Document d = wordsCollectionItr.next();
                    String word = d.getString("word");
                    allwords.add(word);
                }
                queryprocessor process = new queryprocessor(DB, query);
                try {
                    process.processes(query);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wordsfound = process.getwordsfound();
                for (int i = 0; i < wordsfound.size(); i++)
                    System.out.println(wordsfound.get(i));
                //////////////////////
                Result result = new Result(DB, process);

                result.fill_word_idf();
                result.fill_word_url_tf();

                result.fill_id_tf_idf();
   	/*for(Integer i : result.id_rank.keySet())
   	{
   		System.out.printf("id:%d , TF-IDF:%f \n ",i,result.id_rank.get(i));
   	}*/

	/*for(Integer i : result.id_count.keySet())
   	{
   		System.out.printf("id:%d , words_count:%d \n ",i,result.id_count.get(i));
   	}*/

                /////////////////

                /////////////////
                result.sort_id_rank();
	/*for(Integer i : result.id_final.keySet())
   	{
   		//System.out.printf("id:%d: index:%d \n ",i,result.id_final.get(i).getInteger("index"));
		System.out.printf("id:%d:  \n ",i);

   	}*/


                if (query_with_stop_words.endsWith("\"") && query_with_stop_words.startsWith("\"")) {
                    result.PhraseSearch(query_with_stop_words.replaceAll("\"", ""));

                    //.out.println(query_with_stop_words.replaceAll("\"", ""));
                    temp_map1 = result.id_final_phrase;
                } else {
                    result.PhraseSearch(query_with_stop_words);
                    temp_map1 = result.id_final_phrase;
                    temp_map2 = result.id_final_not_phrase;

//		System.out.println("*********temp map**********");
//		for(Integer i : temp_map1.keySet())
//	   	{
//	   		System.out.printf("id:%d:  \n ",i);
//	   	}

                }


	/*System.out.println("id final phrase \n ");
	for(Integer i : result.id_final_phrase.keySet())
   	{
   		System.out.printf("id:%d:  \n ",i);
   	}
	System.out.println("id final not phrase \n ");
	for(Integer i : result.id_final_not_phrase.keySet())
   	{
   		System.out.printf("id:%d:  \n ",i);
   	}*/
                    System.out.println("*********temp map1**********");
                    if (temp_map1 != null)
                        for (Integer i : temp_map1.keySet()) {
                            System.out.printf("id:%d ,index:%s ,word:%s \n ", i, temp_map1.get(i).get("first_index"), temp_map1.get(i).get("first_word"));
                        }
                    System.out.println("*********temp map2**********");
                    if (temp_map2 != null)
                        for (Integer i : temp_map2.keySet()) {
                            System.out.printf("id:%d ,index:%s ,word:%s  \n ", i, temp_map2.get(i).get("first_index"), temp_map2.get(i).get("first_word"));
                        }

            }
        }

    }
}
