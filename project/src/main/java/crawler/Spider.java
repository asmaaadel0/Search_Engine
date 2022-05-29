package crawler;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import database.mongoDB;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;

import static crawler.UrlNormalizer.getNormalizedURL;

public class Spider implements Runnable {

    //set of added urls in database:
    //public final Set<String> pagesVisited = new HashSet<String>();
    public final HashMap<String, Integer> pagesVisited = new HashMap<String, Integer>();
    public final Set<String> keys_check = new HashSet<String>();
    public static int MAX_PAGES_TO_SEARCH ;//= 5000;//5000;
    int count_id ;//= 0; // do no tmake ids start with 0
    int count_crawled;// = 0;
    int is_crawled;
    //SN:e
    mongoDB db;
    List<Integer> children;
    List<Integer> subchildren;
    private List<String> seeds = new LinkedList<String>();
    //private List<String> keys = new LinkedList<String>();
    /////////////////
    static final Object LOCK1 = new Object();
    Spider(){}
    Spider(mongoDB DB) {
        this.db = DB;
        children = new LinkedList<Integer>();
        subchildren = new LinkedList<Integer>();
    }
    Spider(int count_id,int current_crawled,mongoDB DB) {
        //both =current_crawled; and count crawled will start with the same value
        this.count_crawled=current_crawled;
        this.is_crawled=current_crawled;
        this.count_id=count_id;
        this.db = DB;
        children = new LinkedList<Integer>();
        subchildren = new LinkedList<Integer>();
    }

    Spider(List<String> seeds, mongoDB DB) {
        this.db = DB;
        this.seeds = seeds;
        this.count_crawled=0;
        this.is_crawled=0;
        this.count_id=0;
        //this.MAX_PAGES_TO_SEARCH=0;
        children = new LinkedList<Integer>();
        subchildren = new LinkedList<Integer>();
        add_to_DB(true," ", this.seeds,null);
        //System.out.println("inside spider constructor");


    }

    Spider(List<String> seeds) {
        this.count_crawled=0;
        this.count_id=0;
        this.is_crawled=0;
        this.seeds = seeds;

        add_to_DB(true," ", this.seeds,null);
    }
    //-------------------------------------------//
    //set crawling continue its parameters
    public void setCrawlContPar(int current_ids,int current_crawled,/*mongoDB DB,*/int MAX_COUNT)
    {
        this.count_crawled=current_crawled;
        this.is_crawled=current_crawled;
        this.count_id=current_ids;
        this.MAX_PAGES_TO_SEARCH=MAX_COUNT;
    }
    //--------------------------------------------//
    //set new crawling its parameters
    public void setCrawlNewPar(List<String> seeds,int MAX_COUNT)
    {
        this.seeds = seeds;
        this.count_crawled=0;
        this.is_crawled=0;
        this.count_id=0;
        this.MAX_PAGES_TO_SEARCH=MAX_COUNT;
        add_to_DB(true," ", this.seeds,null);
        //System.out.println("inside spider fun set");
    }
    public void set_MAX_COUNT(int MAX_COUNT)
    {
        MAX_PAGES_TO_SEARCH=MAX_COUNT;
    }
    @Override
    public void run() {
        this.search();
    }

    public void search() {
        SpiderLeg leg = new SpiderLeg();
        String currentUrl;
        while (count_id < MAX_PAGES_TO_SEARCH) {
            leg = new SpiderLeg();
            synchronized (this.LOCK1) {
                currentUrl = this.get_nextUrl();
            }
            if (currentUrl != "")//m.s of this condition
            {
                //NOTE:we will not make synchronization over the crawler as we did already with the nextUrl
                //as nextUrl check uniqunes
                //so we are sure that there are no two threads that have the same link to be crawled
                if (leg.crawl(currentUrl, this) == true) {
                    //samaa_14: System.out.println("after crawl");
                    synchronized (this) {
                        add_to_DB(false,leg.currentUrl ,leg.getLinks(),leg.htmlDocument);
                    }
                }

            }
        }
    }


    //------------------------------------------------------------------------//
    // A : b ,c ,d
    //F : w , e ,r,b
    public void add_to_DB(boolean isSeeds, String parent_url ,List<String> links/*,SpiderLeg leg*/,org.jsoup.nodes.Document  htmlDocument)//m.s :return boolean or what?=>it depend on database operation , for me i do not need it l7d dlw2te
    {
        System.out.println("working now with url == "+parent_url);
        children = new LinkedList<Integer>();
        int id= -1;//m.s
        boolean fine;//= false;
        SpiderLeg leg=new SpiderLeg();
        org.jsoup.nodes.Document  child_htmlDocument=null;
        MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");
        String link_normalized;
        // List<String> modified_links=new LinkedList<String>();
        for (String link : links)
        {
            fine = false;
            child_htmlDocument=null;

            if(count_id < MAX_PAGES_TO_SEARCH)
            {
                link_normalized=getNormalizedURL(link);

                if(link.startsWith("https:") || link.startsWith("http:") && link_normalized!=null && !link_normalized.equals(parent_url))
                {
                    //Document doc = Crawel_collection.find(Filters.eq("url", link_normalized)).first();
                    //if(doc == null )
                    //if(!(this.pagesVisited.contains(link_normalized)))
                    if(!(this.pagesVisited.containsKey(link_normalized)))
                    {
                        child_htmlDocument=get_url_info2(link_normalized);
                        if(child_htmlDocument != null)
                        {
                            String bodyText=child_htmlDocument.body().text();
                            String title=child_htmlDocument.title();

                            if((title !="" && bodyText!="" ))
                            {
                                //String current_key = null;
                                String []current_key=new String[1];
                                if(!(check_multiple(link_normalized,child_htmlDocument,current_key)))
                                {
                                    fine = true;
                                    //inrement count_id
                                    count_id++;
                                    Crawel_collection.updateOne(Filters.eq("id", 0), new Document("$set", new Document("count_id", count_id) ));
                                    id = count_id;
                                    //db.insertPage(title,id,link_normalized,bodyText,subchildren);
                                    org.bson.Document website = new org.bson.Document("id", id).append("title", title).
                                            append("url", link_normalized).append("content", bodyText).append("childrednIDS", subchildren).
                                            append("key",current_key[0]);
                                    Crawel_collection.insertOne(website);
                                    //this.pagesVisited.add(link_normalized);
                                    this.pagesVisited.put(link_normalized,id);

                                }

                            }
                        }

                    }
                    else
                    {
                        fine = true;
                        //id=doc.getInteger("id");
                        id=this.pagesVisited.get(link_normalized);
                    }
                    if (fine == true && isSeeds == false) {
                        children.add(id);
                        //do not do what i said in tje foolowing else as:
                        //there is no parent to the seeds
                    }
                }

            }

        }
        if (isSeeds == false)
        {
            Crawel_collection.updateOne(Filters.eq("url", parent_url), new Document("$set", new Document("childrednIDS", children) ));
            is_crawled++;
            Crawel_collection.updateOne(Filters.eq("id", 0), new Document("$set", new Document("is_crawled", is_crawled) ));

            //System.out.println("is_crawled ++ with uel == "+parent_url);
            for(int i=0;i<children.size();i++)
            {
                System.out.print(children.get(i)+" -- ");
            }
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
        }while(doc == null);
        Crawel_collection.updateOne(Filters.eq("id", 0), new Document("$set", new Document("count_crawled", count_crawled) ));
        next_url = doc.getString("url");
        return next_url;
    }


    //------------------------------------------------------------------------//
//    public void remove_uncrawled()//SpiderLeg leg)//m.s :return boolean or what?=>it depend on database operation , for me i do not need it l7d dlw2te
//    {
//        //delete from database all links that its id > count_crawled (mm.s:not >=)
//        //MongoCollection<Document> Crawel_collection = database.getCollection("Crawlled");
//        MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");
//        Crawel_collection.deleteMany(Filters.gt("id",count_crawled));
//        //Crawel_collection.deleteOne("url", "fghjnm") ;
//
//
//
//    }

    //------------------------------------------------------------------------//
    public void recrawling(mongoDB DB) {
//        while(true)
//        {
//        }

        org.jsoup.nodes.Document  child_htmlDocument=null;
        MongoCollection<Document> Crawel_collection = DB.get_db().getCollection("Crawlled");
        // MongoCollection<Document> Crawel_collection = DB_forever.get_db().getCollection("Crawlled");
        String url_curr=null;
        Iterator<Document> doc_iterator= DB.getAllkdocs().iterator();
        org.bson.Document doc_current= null;
        while(doc_iterator.hasNext()) {
            doc_current = doc_iterator.next();
            url_curr = doc_current.getString("url");
            if (url_curr != null) {
                child_htmlDocument = get_url_info2(url_curr);
                if (child_htmlDocument != null) {
                    String bodyText = child_htmlDocument.body().text();
                    String title = child_htmlDocument.title();
                    if ((title != "" && bodyText != "")) {
                        //doc_current.update(bodyText, title);
                        Crawel_collection.updateOne(Filters.eq("id", doc_current.getInteger("id")),
                                new Document("$set", new Document("title", title).append("content", bodyText) ));

                    }

                }
            }
        }


    }
    //-----------------------------------------------------------------//
    public boolean check_multiple(String url,org.jsoup.nodes.Document current_doc,String []current_key)//String bodyText)
    {
        String bodyText= current_doc.body().text();
        MongoCollection<Document> KeysList = db.get_db().getCollection("keys");
        //calculate the key of this page
        char[] key = new char[50];
        int p=5;
        if(bodyText.length()>100)
            p*=10;
        for(int i = 0 ; i*p<bodyText.length()&&i<key.length; i++)
        {
            key[i]= bodyText.charAt(i*p);
        }
        String skey = new String(key);
        boolean keyExist = false;
//            Document doc= KeysList.find(Filters.eq("key", skey)).first();
//            if(doc != null)
//            {
//                keyExist = true;
//            }
        if(this.keys_check.contains(skey))
        {
            keyExist = true;
        }
        else
        {
            keys_check.add(skey);
            current_key[0]=skey;
            //db.insertKey(skey,/*int id,*/url);
        }

        return keyExist;

    }
    //----------------------------------------//
    public org.jsoup.nodes.Document get_url_info2(String url) {
        try
        {
            Connection connection = Jsoup.connect(url);//.ignoreContentType(true);//.userAgent(USER_AGENT);
            org.jsoup.nodes.Document htmlDocument_child = connection.get();

            if(connection.response().statusCode() == 200)
            {
                //System.out.println("\n*Visiting* Received web page at " + url);
            }
            if (!connection.response().contentType().contains("text/html")) {
                //System.out.println("Failure Retrieved something other than HTML");
                return null;
            }

            if(htmlDocument_child == null)
            {
                //System.out.println("NULL doc");
                return null;
            }
            if(htmlDocument_child.body() == null)
            {
                //System.out.println("NULL BODY");
                return null;
            }



            //System.out.println(title+bodyText);
            return htmlDocument_child;
        }
        catch(IOException ioe)
        {

            //System.out.println("exceptionnnn");
            return null;
        }


    }
}













