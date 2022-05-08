package ranker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.bson.Document;

import database.mongoDB;
import indexer.Indexer;

public class Ranker_test

{


    public static void main(String[] args) throws IOException
    {
        // Connect to the database
        mongoDB DB = new mongoDB("A");
        Ranker webRanker = new  Ranker(DB);

        webRanker.fill_matrix();
        webRanker.Rank();

    }



}
