package ranker;

import database.mongoDB;
import org.bson.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class queryprocessor_test {
    public static void main(String[] args) throws IOException {
        // Connect to the database
        //mongoDB DB = new mongoDB("A");
        mongoDB DB = new mongoDB("A+");
        String query;
        String query_with_stop_words;
        HashMap<Integer, Document> temp_map1=null; // mosh phrase
        HashMap<Integer, Document> temp_map2=null;
        //List<String> All_words=new ArrayList<String>();
        Scanner sc = new Scanner(System.in);
        List<String> allwords=new ArrayList<String>();
        List<String> wordsfound=new ArrayList<String>();
        Iterator<Document> wordsCollectionItr = DB.getAllwords().iterator();
        System.out.println("Enter the query:");
        query = sc.nextLine();
        query_with_stop_words=query;
        while (wordsCollectionItr.hasNext()) {
            Document d = wordsCollectionItr.next();
            String word = d.getString("word");
            allwords.add(word);
        }
        queryprocessor process=new queryprocessor(DB);
        process.processes(query);
        wordsfound=process.getwordsfound();
        for (int i=0;i<wordsfound.size();i++)
            System.out.println(wordsfound.get(i));
        //////////////////////
        Result result=new Result (DB,process);

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


        if(query_with_stop_words.endsWith("\"")&&query_with_stop_words.startsWith("\""))
        {
            result.PhraseSearch(query_with_stop_words.replaceAll("\"",""));

            System.out.println(query_with_stop_words.replaceAll("\"",""));
            temp_map1=result.id_final_phrase;
        }
        else
        {
            result.PhraseSearch(query_with_stop_words);
            temp_map1=result.id_final_phrase;
            temp_map2=result.id_final_not_phrase;

//		System.out.println("*********temp map**********");
//		for(Integer i : temp_map.keySet())
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
        if(temp_map1!=null)
            for(Integer i : temp_map1.keySet())
            {
                System.out.printf("id:%d ,index:%s ,word:%s \n ",i,temp_map1.get(i).get("first_index"),temp_map1.get(i).get("first_word"));
            }
        System.out.println("*********temp map2**********");
        if(temp_map2!=null)
            for(Integer i : temp_map2.keySet())
            {
                System.out.printf("id:%d ,index:%s ,word:%s  \n ",i,temp_map2.get(i).get("first_index"),temp_map2.get(i).get("first_word"));
            }


    }





}