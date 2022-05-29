package database;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.Iterator;
import java.util.List;
public class mongoDB {
    public static final int max_pages_count = 5000;
    public MongoCollection<Document> keys;
    public MongoCollection<Document> Crawlled;
    public MongoCollection<Document> Indexed;
    public  MongoCollection<Document> words;
    MongoCollection<Document> Seed;
    MongoCollection<Document> suggestions;
    //samaa:
    MongoDatabase db;
    public MongoDatabase get_db() {
        return db;
    }
    //samaa:e

    public mongoDB(String Database) { //constructor holds data base name

        try {
            //data base connnection string
            String uri ;
            if(System.getenv("DB_URI")==null)
                uri="mongodb://localhost:27017/"; //database connecion string
            else
                uri=System.getenv("DB_URI");


            ConnectionString connection_string = new ConnectionString(uri);


            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connection_string).retryWrites(true).build();
            //connect to server
            MongoClient Client = MongoClients.create(settings);

            // Create the data base
            //samaa
            //MongoDatabase db = Client.getDatabase(Database);
            db = Client.getDatabase(Database);
            //samaa:e

            // Create the needed collections
            keys = db.getCollection("keys");
            Crawlled = db.getCollection("Crawlled");
            Indexed = db.getCollection("Indexed");
            words= db.getCollection("Words");
            Seed = db.getCollection("Seed");
            suggestions = db.getCollection("suggestions");
            System.out.println("successfully connected to data base \n");


        } catch (Exception e) {
            System.out.println("faild to connect to data base ");
            e.printStackTrace();
        }

    }


    public boolean url_indexed_before(String url)
    {
        // make a new document of the url sent
        //loop to find it n indexed pages in data base if not fund return false
        return Indexed.find(new Document("url", url)).iterator().hasNext();
    }

    //////////////////////////////////////






    public void add_new_word(String word, List<Document> web_pages)
    {
        //IDF = log(total number of documents/number of documents containing the term).

        //insert new word in words collection ->dictionary
        org.bson.Document doc = new org.bson.Document("word", word).append("IDF", Math.log(Crawlled.countDocuments() / (float) web_pages.size())).append("pages", web_pages);
        words.insertOne(doc);
    }
    public FindIterable<Document>getAllkdocs(){
        return Crawlled.find(new org.bson.Document());
    }


    public void add_new_indexed_page(String url, Integer count)
    {

        org.bson.Document pageDoc = new org.bson.Document("url", url).append("count", count);
        Indexed.insertOne(pageDoc);
    }
    //////////////////////////////////////////////////////////////

    public void updateIDF(String word) {
        int t = words.find(new Document("word", word)).iterator().next().getList("pages", Document.class).size();
        words.updateOne(new Document("word", word),new Document("$set",new Document("IDF", Math.log(Crawlled.countDocuments() / (float) t))));
    }

    public void updateAllIDF() {
        Iterator wordsItr = words.find().iterator();
        while (wordsItr.hasNext()) {
            updateIDF((String) wordsItr.next());
        }
    }
    public FindIterable<Document> getAllPages()
    {
        return Crawlled.find(new org.bson.Document());
    }
    // get the is of page and the outcoming pages in a map
	 /*   public FindIterable<Document> getAllIds()
	    {

	        return Crawlled.find( { status: "A" } )‏;
	    }*/

    public void insertPage(String title, int id, String url, String doc,List<Integer> IDS ) {
        org.bson.Document website = new org.bson.Document("id", id).append("title", title).append("url", url).append("content", doc).append("childrednIDS", IDS);
        Crawlled.insertOne(website);
    }

    public int get_crawlled_pages_count()
    {
        return (int) Crawlled.countDocuments();
    }

    public void get_url_rank(int id,Double rank)
    {

        Crawlled.updateOne(new Document("id",id),new Document("$set",new Document("page_rank",rank )));

    }

    ////////////query////////////////
    public FindIterable<Document> getAllwords()
    {
        return words.find(new org.bson.Document());
    }



    //---------------------------------------------//
    public void insertKey(String key, /*int id,*/ String url) {
        org.bson.Document key_link = new org.bson.Document("key", key).append("url", url);
        keys.insertOne(key_link);
    }
    //------------------------//
    public FindIterable<Document> getAllkKeys() {
        return keys.find(new org.bson.Document());
    }

    public FindIterable<Document> getAllsuggestions() {
        return suggestions.find(new org.bson.Document());
    }
    public void insertsuggest(String suggestion) {
        org.bson.Document website = new org.bson.Document("suggestions", suggestion);
        suggestions.insertOne(website);
    }



///////////////////////////////////////////////////////////////////////////////////

}
