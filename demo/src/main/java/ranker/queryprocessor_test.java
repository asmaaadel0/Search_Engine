package ranker;

import database.mongoDB;
import org.bson.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class queryprocessor_test {
    public static void main(String[] args) throws IOException {
        // Connect to the database
        mongoDB DB = new mongoDB("A");
        String query;
        //Scanner sc = new Scanner(System.in);
        List<String> allwords=new ArrayList<String>();
        List<String> wordsfound=new ArrayList<String>();
        Iterator<Document> wordsCollectionItr = DB.getAllwords().iterator();
        System.out.println("Enter the query:");
        queryprocessor process=new queryprocessor(DB);
        //query = sc.nextLine();
        query=process.getquery();
        System.out.println(query);
        while (wordsCollectionItr.hasNext()) {
            Document d = wordsCollectionItr.next();
            String word = d.getString("word");
            allwords.add(word);
        }
        process.processes(query,allwords);
        wordsfound=process.getwordsfound();
        for (int i=0;i<wordsfound.size();i++)
            System.out.println(wordsfound.get(i));
        //////////////////////
        Result result=new Result (DB,process);

        result.fill_word_idf();
        result.fill_word_url_tf();

        result.fill_id_tf_idf();
        for(Integer i : result.id_rank.keySet())
        {
            System.out.printf("id:%d , TF-IDF:%f \n ",i,result.id_rank.get(i));
        }

        for(Integer i : result.id_count.keySet())
        {
            System.out.printf("id:%d , words_count:%d \n ",i,result.id_count.get(i));
        }

        result.sort_id_rank();
        for(Integer i : result.id_final.keySet())
        {
            System.out.printf("id:%d \n ",i);
        }


    }




}