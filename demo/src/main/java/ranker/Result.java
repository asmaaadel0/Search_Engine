package ranker;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.eq;
import ca.rmen.porterstemmer.PorterStemmer;
import database.mongoDB;

public class Result
{

    List<String> query_words;

    mongoDB db;
    HashMap<String,Double> word_idf ;
    HashMap<String, List<Document>> word_urlid_tf;

    public HashMap<Integer, Double> id_rank;
    public HashMap<Integer, Integer> id_count;
    public HashMap<Integer, Document> id_final;

    public Result (mongoDB DB,queryprocessor qu)
    {
        db=DB;
        queryprocessor q=qu;
        query_words=q.getwordsfound();
        word_idf=new HashMap<String, Double>();

        word_urlid_tf = new HashMap<String, List<Document>>();
        id_rank=new HashMap<Integer, Double>();
        id_count=new HashMap<Integer, Integer>();
        id_final =new HashMap<Integer, Document>();
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
                    d.append("count",(id_count.get(id))+1 );
                    d.append("TF_IDF_rank", id_rank.get(id)+((idf*tf)+rank));
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

        id_final=sortByValue(id_final);
    }





}



