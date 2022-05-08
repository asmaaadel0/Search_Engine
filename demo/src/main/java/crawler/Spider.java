package crawler;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import crawler.SpiderLeg;
import database.mongoDB;
//import org.bson.Document;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

//import java.util.*;
//import java.util.regex.*;
//import javax.swing.*;
//import javax.swing.table.*;
//import java.util.Set;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.HashSet;
public class Spider implements Runnable {
    //SN
    public static final int MAX_PAGES_TO_SEARCH = 100;//5000;
    // private static int num_of_seeds;
    // final List<String> pagesVisited = new LinkedList<String>();
    // final List<String> pagesToVisit = new LinkedList<String>();
    int count_id = 0; // do no tmake ids start with 0
    int count_crawled = 0;
    //SN:e
    mongoDB db;
    List<Integer> children;
    List<Integer> subchildren;
    private List<String> seeds = new LinkedList<String>();
    private List<String> keys = new LinkedList<String>();
    /////////////////
    static final Object LOCK1 = new Object();

    Spider() {

    }

    Spider(List<String> seeds, mongoDB DB) {
        this.db = DB;
        this.seeds = seeds;

        children = new LinkedList<Integer>();
        subchildren = new LinkedList<Integer>();
        add_to_DB(true, " ", this.seeds, null);


    }

    Spider(List<String> seeds) {
        //SN
        //       num_of_seeds=seeds.size();
        this.seeds = seeds;
//        for (int i=0;i<num_of_seeds;i++)
//            this.pagesToVisit.add(seeds.get(i));
        add_to_DB(true, " ", this.seeds, null);
        //SN:e

    }

    @Override
    public void run() {
        this.search();
    }

    public void search() {
        SpiderLeg leg = new SpiderLeg();
        //SN
//        //add the keys of seeds to the keys list
//        for (int i = 0; i < this.seeds.size(); i++) {
//            leg.add_seeds_keys(seeds.get(i), this);
//        }

        String currentUrl;
        //while (this.pagesVisited.size()+seeds.size()< MAX_PAGES_TO_SEARCH)
        while (count_crawled < MAX_PAGES_TO_SEARCH) {
            //SN :e
            leg = new SpiderLeg();
            //SN
            synchronized (this.LOCK1) {
                //currentUrl = this.nextUrl();
                currentUrl = this.get_nextUrl();
            }
            //SN:e

            //  }
            // if((checkRobots(currentUrl))&& !leg.chek_multiple(currentUrl, this))
            if (currentUrl != "")//m.s of this condition
            {
                //NOTE:we will not make synchronization over the crawler as we did already with the nextUrl
                //as nextUrl check uniqunes
                //so we are sure that there are no two threads that have the same link to be crawled
                if (leg.crawl(currentUrl, this) == true) {

                    synchronized (this) {
                        // try {
                        //check that the pagesToVisit list will be unique while adding the links list of the current url
                        //add_to_p_t_v(pagesToVisit, leg);
                        //SN
                        add_to_DB(false, leg.currentUrl, leg.getLinks(), leg.htmlDocument);

                        //SN:e
                        //  } catch (MalformedURLException e) {
                        //     e.printStackTrace();
                        // }
                        //this.notifyAll();
                    }
                }

            }
        }
        // System.out.println("\n**Done** Visited " + this.pagesVisited.size() + " web page(s)");

        //SN
        //samaa_6: remove_uncrawled();
    }


    //------------------------------------------------------------------------//

    public List<String> getKeyList() {
        return this.keys;
    }

////////////sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss//////////////////
    // A : b ,c ,d
    //F : w , e ,r,b

    //crawled_urls:no need to this boolean=>crawled or not
    //id    url       array of children ids (array or vector?)
    public void add_to_DB(boolean isSeeds, String parent_url, List<String> links/*,SpiderLeg leg*/, org.jsoup.nodes.Document htmlDocument)//m.s :return boolean or what?=>it depend on database operation , for me i do not need it l7d dlw2te
    {
        children = new LinkedList<Integer>();
        int id;//= -1;//m.s
        //MongoCollection<Document> Crawel_collection = database.getCollection("Crawlled");
        MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");
        // List<String >links=leg.getLinks();
        //links.size()
        for (String link : links) {
//            if(count_id >= MAX_PAGES_TO_SEARCH)
//            {
//                return;
//            }
            //samaa
            if (count_id < MAX_PAGES_TO_SEARCH) {
                //reintialize the id
                //id = -1;//m.s
                //select id from crawled_urls where url='link' =>link not links
                // get the doc of the child if exist
                Document doc = Crawel_collection.find(Filters.eq("url", link)).first();
                //String id = doc.get("_id").toString();
                //id=doc.get("_id");
                //not existed before
                //if (id == -1)///m.s
                //System.out.println("returned doc +++> "+doc);
                if (doc == null) {
                    //inrement count_id
                    count_id++;
                    id = count_id;
                    //add to database
                    //Document doc = null;

                    db.insertPage("", id, link, "", subchildren);
                    //db.insertPage("",id,parent_url,"",subchildren);
                    // insert (id,link,null -or 0?:i mean there is no children to it uit-) in crawled_urls
                } else {
                    //do not insert it
                    //just det its id
                    System.out.println(parent_url + " adddd ======> " + doc.getString("url") + "with id = " + doc.getInteger("id"));//count_id);//+"returned doc +++> "+doc));
                    id = doc.getInteger("id");
                }


                if (isSeeds == true) {
                    //do not do what i said in tje foolowing else as:
                    //there is no parent to the seeds
                } else {
                    children.add(id);
                    //IMPORTANT
                    //for both cases:existed or not =>put the id of the child to the array of children id to its parent
                    //for leg.currentUrl(or parent_url), put this id(select array child ids where url='leg.currentUrl' + append to it this id-or array od ids if you make it after the loop-??)
                    //you can put it in an array and then put it in the leg.currentUrl at the end of the loop
                }
            }


        }
        if (isSeeds == false) {
            //                db.Crawlled.updateOne(
//                Filters.eq("title", "children","content"),
//                Updates.set(leg.htmlDocument.title(), children,leg.htmlDocument.body()));


//        Crawel_collection.updateOne(Filters.eq("url",parent_url),
//        Updates.combine(Updates.set("title", htmlDocument.title()),
//                Updates.set("content", htmlDocument.body()),
//                Updates.set("childrednIDS", children))) ;

            // Crawel_collection.updateOne(Filters.eq("url",parent_url), Updates.set("content", htmlDocument.body()));

            Crawel_collection.updateOne(Filters.eq("url", parent_url), new Document("$set", new Document("title", htmlDocument.title())));
            Crawel_collection.updateOne(Filters.eq("url", parent_url), new Document("$set", new Document("content", htmlDocument.body().text())));
            Crawel_collection.updateOne(Filters.eq("url", parent_url), new Document("$set", new Document("childrednIDS", children)));

            //new Document("$set",new Document("content", htmlDocument.body())));


//             System.out.print("i am : "+parent_url+" ,, my children ids are ");
//             for(int i =0 ; i<children.size();i++ )
//             {
//                 System.out.print(children.get(i)+" --- ");
//             }
        }


    }

    //------------------------------------------------------------------------//
    public String get_nextUrl() {
        //MongoCollection<Document> Crawel_collection = database.getCollection("Crawlled");
        MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");
        Document doc;
        String next_url;
        //increment count_crawled before retreive the link
        count_crawled++;
        //next_url= the following
        //if there is no document uit -> busy waiting , do not use wait as i do not want this thread to leave the lock until it has one url
        do {
            doc = Crawel_collection.find(Filters.eq("id", count_crawled)).first();
            System.out.println(Thread.currentThread().getName() + ": " + "Busy waiting with count_crawled =  " + count_crawled);
        } while (doc == null);
        System.out.println(Thread.currentThread().getName() + ": i got a link");
        //if(doc == null)4
        {
            //this.wait()
        }
        // else ??
        next_url = doc.getString("url");
        //next_url = doc.get("url").toString();
        //select url from crawled_urls where id='count_crawled'
        //?need to check if it retreved sucseecfully or not (m.s)


        return next_url;
    }

    //------------------------------------------------------------------------//
    public void remove_uncrawled()//SpiderLeg leg)//m.s :return boolean or what?=>it depend on database operation , for me i do not need it l7d dlw2te
    {
        //delete from database all links that its id > count_crawled (mm.s:not >=)
        //MongoCollection<Document> Crawel_collection = database.getCollection("Crawlled");
        MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");
        Crawel_collection.deleteMany(Filters.gt("id", count_crawled));
        //Crawel_collection.deleteOne("url", "fghjnm") ;


    }

}




