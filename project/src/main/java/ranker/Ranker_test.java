package ranker;

import java.io.IOException;

import database.mongoDB;

public class Ranker_test

{


    public static void main(String[] args) throws IOException
    {
        // Connect to the database
        //  mongoDB DB = new mongoDB("A");
        mongoDB DB = new mongoDB("A");
        Ranker webRanker = new  Ranker(DB);

        webRanker.fill_matrix();
        webRanker.Rank();

    }



}
