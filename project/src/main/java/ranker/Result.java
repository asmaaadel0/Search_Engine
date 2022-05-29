package ranker;

import java.util.*;

import org.bson.Document;

import ca.rmen.porterstemmer.PorterStemmer;
import database.mongoDB;

import static com.mongodb.client.model.Filters.*;

public class Result
{

    List<String> query_words;

    mongoDB db;
    HashMap<String,Double> word_idf ;
    HashMap<String, List<Document>> word_urlid_tf;
    List<String > all_content = new ArrayList<>();
    List<Integer>all_id=new ArrayList<>();
    public HashMap<Integer, Double> id_rank;
    public HashMap<Integer, Integer> id_count;
    public HashMap<Integer, Document> id_final; // mosh phrase
    public HashMap<Integer, Document> id_temp;
    public HashMap<Integer, Document> id_final_not_phrase; // mosh phrase
    public HashMap<Integer, Document> id_final_phrase; //phrase
    int count;
    PorterStemmer porterStemmer = new PorterStemmer();
    public Result(mongoDB DB, queryprocessor qu)
    {
        db=DB;
        queryprocessor q=qu;
        query_words=q.getwordsfound();
        word_idf=new HashMap<String, Double>();

        word_urlid_tf = new HashMap<String, List<Document>>();
        id_rank=new HashMap<Integer, Double>();
        id_count=new HashMap<Integer, Integer>();
        id_final =new HashMap<Integer, Document>();
        id_temp =new HashMap<Integer, Document>();
        id_final_phrase =new HashMap<Integer, Document>();
        id_final_not_phrase =new HashMap<Integer, Document>();
        Iterator<Document> crawlerCollectionItr = db.getAllPages().iterator();
        while (crawlerCollectionItr.hasNext()) {
            Document d = crawlerCollectionItr.next();
            String content = d.getString("content");
            int id=d.getInteger("id");
            all_content.add(content);
            all_id.add(id);
        }
    }
    public void fill_word_idf()
    {

        Document doc ;
        for(int i=0;i<query_words.size();i++)
        {
            String word=query_words.get(i);
            doc= db.words.find(eq("word",word)).first();
            Double idf = doc.getDouble("IDF");


            word_idf.put(word,idf);

            System.out.printf("word:%s   IDF:%f \n ",word,idf );

        }

    }

    public void fill_word_url_tf()
    {
        Document doc ;
        for(int i=0;i<query_words.size();i++)
        {
            String word=query_words.get(i);
            doc= db.words.find(eq("word",word)).first();
            List<Document> l = doc.getList("pages",Document.class);
            // loop on this for phrase searching
            word_urlid_tf.put(word,l);
            System.out.printf("word:%s ",word);
            for(int j=0;j<l.size();j++)
            {
                Integer id=l.get(j).getInteger("id");
                Double tf=l.get(j).getDouble("TF");
                System.out.printf("id:%d Tf:%f ",id,tf);
            }
        }

        System.out.print(" \n ");

    }

    public void  fill_id_tf_idf()
    {
        for(String i : word_urlid_tf.keySet())
        {
            double idf=word_idf.get(i);
            List<Document> arr=word_urlid_tf.get(i);

            for(int j=0;j<arr.size();j++)
            {
                Integer id=arr.get(j).getInteger("id");
                Double tf=arr.get(j).getDouble("TF");
                Double rank=arr.get(j).getDouble("page_rank");
                if (id_rank.containsKey(id))
                {
                    id_rank.put(id,id_rank.get(id)+((idf*tf)+rank));
                    id_count.put(id,(id_count.get(id))+1);
                    Document d = new Document();
                    //d.append("count",(id_count.get(id))+1 );
                    d.append("count",(id_count.get(id)));
                    d.append("TF_IDF_rank", id_rank.get(id)+((idf*tf)+rank));

                    //  d.append("index", word_urlid_tf.get(i).get(0).getList("index",Integer.class).get(0));
                    id_final.put(id, d);
                }
                else
                {
                    //newly added
                    id_rank.put(id,((tf*idf)+rank));
                    id_count.put(id,1);
                    Document d = new Document();
                    d.append("count",1);
                    d.append("TF_IDF_rank", ((tf*idf)+rank));
                    //		    	            d.append("index", word_urlid_tf.get(i).get(0).getList("index",Integer.class).get(0));
                    id_final.put(id, d);

                }

            }


        }




    }


    public static HashMap<Integer, Document> sortByValue(HashMap<Integer, Document> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Document> > list =
                new LinkedList<Map.Entry<Integer, Document> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Document> >() {
            public int compare(Map.Entry<Integer, Document> o1,
                               Map.Entry<Integer, Document> o2)
            {
                //System.out.println(" o1: "+o1.getValue().getInteger("count") +" o2:"+ o2.getValue().getInteger("count"));
                if((o2.getValue().getInteger("count")).compareTo(o1.getValue().getInteger("count"))==0)
                    return (o2.getValue().getDouble("TF_IDF_rank")).compareTo(o1.getValue().getDouble("TF_IDF_rank"));
                else
                    return( (o2.getValue().getInteger("count")).compareTo(o1.getValue().getInteger("count")));
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Document> temp = new LinkedHashMap<Integer, Document>();
        for (Map.Entry<Integer, Document> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public void sort_id_rank()
    {

        id_temp=sortByValue(id_final);
        id_final=id_temp;
    }

	 /*  public void word_id_index()
		  {


		  }*/



    public void PhraseSearch(String original_query)
    {

        // HashMap<Integer, Document> id_final_phrase=new HashMap<Integer, Document>();
        count=0;
        //MongoCollection<Document> Crawel_collection = db.get_db().getCollection("Crawlled");

        Document doc = null;
        for(int i:id_final.keySet()) {
            count++;
            if(id_final.get(i).getInteger("count") != query_words.size()/*steamed string csize*/)
            {int j;
                // break;
                System.out.println("Here! "+id_final.get(i).getInteger("count"));
                id_final_not_phrase.put(i,id_final.get(i));
//                doc = Crawel_collection.find(Filters.eq("id", i)).first();
                //String current_content=doc.getString("content");
                String current_content=all_content.get(i);
                String sub=original_query;
                String arr[] = sub.split(" ");
                for(j=0;j<arr.length;j++)
                {
                    int indexOfSubStr = current_content.toLowerCase(Locale.ROOT).indexOf(arr[j].toLowerCase(Locale.ROOT));
                    if(indexOfSubStr != -1)
                    {
                        System.out.println("index of sub string not phrase "+indexOfSubStr );
                        id_final.get(i).append("first_index", indexOfSubStr);
                        id_final.get(i).append("first_word", arr[j]);
                        id_final_not_phrase.put(i,id_final.get(i));

                        break;
                    }
                    else
                    {
                        String stem = porterStemmer.stemWord(arr[j]);
                        //String stemcontent=porterStemmer.stemWord(current_content);
                        int len=stem.length()-2;
                        indexOfSubStr=current_content.toLowerCase(Locale.ROOT).indexOf(stem.substring(len).toLowerCase(Locale.ROOT));
                        System.out.println("index of sub string not phrase "+indexOfSubStr );
                        id_final.get(i).append("first_index", indexOfSubStr);
                        id_final.get(i).append("first_word", arr[j]);
                        id_final_not_phrase.put(i,id_final.get(i));
                    }
                }
//	 	        		id_final.get(i).append("first_index", j);
//	 	        		id_final.get(i).append("first_word", arr[j]);
                //System.out.println("i'm inserting in not phrase: "+i);

            }
            else
            {
                //check the content of this url

                //doc = Crawel_collection.find(Filters.eq("id", i)).first();
                //String current_content=doc.getString("content");
                String current_content=all_content.get(i);
                if(current_content.toLowerCase(Locale.ROOT).contains(original_query.toLowerCase(Locale.ROOT)))
                {
                    int indexOfSubStr = current_content.toLowerCase(Locale.ROOT).indexOf(original_query.toLowerCase(Locale.ROOT));
                    System.out.println("index of sub string phrase "+indexOfSubStr);
                    id_final.get(i).append("first_index",indexOfSubStr );
                    id_final.get(i).append("first_word",original_query );
                    //sSystem.out.println("i'm inserting in phrase: "+i);
                    id_final_phrase.put(i,id_final.get(i));

                }
                else

                {
                    int indexOfSubStr = current_content.toLowerCase(Locale.ROOT).indexOf(original_query.toLowerCase(Locale.ROOT));


                    System.out.println("i'm inserting in not phrase: "+i);
                    String sub=original_query;
                    String arr1[] = sub.split(" ");
                    int k;
                    for(k=0;k<arr1.length;k++)
                    {
                        int index = current_content.toLowerCase(Locale.ROOT).indexOf(arr1[k].toLowerCase(Locale.ROOT));
                        if(index != -1)
                        {

                            System.out.println("index of sub string not phrase "+index );
                            id_final.get(i).append("first_index", index);
                            id_final.get(i).append("first_word", arr1[k]);
                            id_final_not_phrase.put(i,id_final.get(i));

                            break;
                        }
                        else
                        {
                            String stem = porterStemmer.stemWord(arr1[k]);
                            //String stemcontent=porterStemmer.stemWord(current_content);
                            int len=stem.length()-2;
                            index=current_content.toLowerCase(Locale.ROOT).indexOf(stem.substring(len).toLowerCase(Locale.ROOT));
                            System.out.println("index of sub string not phrase "+index );
                            id_final.get(i).append("first_index", index);
                            id_final.get(i).append("first_word", arr1[k]);
                            id_final_not_phrase.put(i,id_final.get(i));
                        }
                    }
                }


            }
        }

        // return id_final_phrase;


    }




}



