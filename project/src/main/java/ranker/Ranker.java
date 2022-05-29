package ranker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import database.mongoDB;

public class Ranker

{

    static mongoDB db;

    HashMap<Integer, List<Integer>> ids_map;

    //constructor for the indexer
    public Ranker(mongoDB DB)

    {
        db = DB;
        ids_map  = new HashMap<Integer, List<Integer>>();

    }
    public static List<List<Integer>>  fill_matrix()  //List<List<Integer>>
    {

        Iterator<Document> CrawlerCollectionItr = db.getAllPages().iterator();
        int dimensions=db.get_crawlled_pages_count();
        dimensions++;//6

        List<List<Integer>> matrix=new ArrayList<List<Integer>>(dimensions);

        for(int e=0;e<dimensions;e++)//0 1 2 3 4 5
        {
            List<Integer> temp =new ArrayList<Integer>(dimensions);
            for(int j=0;j<dimensions;j++)//6
            {
                temp.add(null);

            }
            matrix.add(temp);

        }

        System.out.printf("size: %d \n",dimensions);
        for(int l=0;l<dimensions;l++)
            for(int m=0;m<dimensions;m++)
                matrix.get(l).add(m, 0);

        int i = 1;
        while (CrawlerCollectionItr.hasNext()) {
            Document d = CrawlerCollectionItr.next();

            Integer id=d.getInteger("id");
            List<Integer> arr =  d.getList("childrednIDS",Integer.class);
            int size;
            System.out.printf("rank page: %d \n",id);
            if(arr != null)
                size=arr.size();
            else
                size=0;
            Integer j;

            for( j=0;j<size;j++)
            {
                System.out.printf("i'm child: %d \t",arr.get(j));

                matrix.get(id).add(arr.get(j), 1);
            }

            i++;
            System.out.printf("\n ");

        }

        return matrix;



    }

    public static void   Rank()
    {

        List<List<Integer>> adjacency_matrix=fill_matrix();
        int dimensions=db.get_crawlled_pages_count();
        dimensions++;
        for(int l=0;l<dimensions;l++)
        {
            for(int m=0;m<dimensions;m++)
                System.out.printf(adjacency_matrix.get(l).get(m)+" \t");
            System.out.printf("\n");
        }


        Double page_rank[] = new Double[dimensions];

        double temp_page_rank[] = new double[dimensions];

        /////////////lol////////////////////
        double InitialPageRank;
        double OutgoingLinks = 0;
        double DampingFactor = 0.85;

        int ExternalNodeNumber;
        int InternalNodeNumber;
        int k = 1; // For Traversing
        int ITERATION_STEP = 1;
        InitialPageRank = 1 /(double)(dimensions-1);
        System.out.printf("\n page rank \n"+ InitialPageRank);

        // 0th ITERATION  _ OR _ INITIALIZATION PHASE //

        for (k = 1; k < dimensions; k++)//1 2 3 4 5 1 <d
        {
            page_rank[k] = InitialPageRank;
        }

        System.out.printf("\n Initial PageRank Values , 0th Step \n");
        for (k = 1; k <dimensions; k++) {
            System.out.printf(" Page Rank of " + k + " is :\t" + page_rank[k] + "\n");
        }

        while (ITERATION_STEP <= 100) // Iterations
        {
            // Store the PageRank for All Nodes in Temporary Array
            for (k = 1; k < dimensions; k++) {
                temp_page_rank[k] = page_rank[k];
                page_rank[k] = 0.0;
            }

            for (InternalNodeNumber = 1; InternalNodeNumber < dimensions; InternalNodeNumber++) {
                for (ExternalNodeNumber = 1; ExternalNodeNumber < dimensions; ExternalNodeNumber++) {
                    if (adjacency_matrix.get(ExternalNodeNumber).get(InternalNodeNumber) == 1) {
                        k = 1;
                        OutgoingLinks = 0; // Count the Number of Outgoing Links for each ExternalNodeNumber
                        while (k < dimensions) {
                            if (adjacency_matrix.get(ExternalNodeNumber).get(k) == 1) {
                                OutgoingLinks = OutgoingLinks + 1; // Counter for Outgoing Links
                            }
                            k = k + 1;
                        }
                        // Calculate PageRank
                        page_rank[InternalNodeNumber] += temp_page_rank[ExternalNodeNumber] * (1 / OutgoingLinks);
                    }
                }
            }

            System.out.printf("\n After " + ITERATION_STEP + "th Step \n");

            for (k = 1; k < dimensions; k++)
                System.out.printf(" Page Rank of " + k + " is :\t" + page_rank[k] + "\n");

            ITERATION_STEP = ITERATION_STEP + 1;
        }
        // Add the Damping Factor to PageRank
        for (k = 1; k <dimensions; k++) {
            page_rank[k] = (1 - DampingFactor) + DampingFactor * page_rank[k];
        }

        // Display PageRank
        System.out.printf("\n Final Page Rank : \n");
        for (k = 1; k < dimensions; k++) {
            System.out.printf(" Page Rank of " + k + " is :\t" + page_rank[k] + "\n");

            db.get_url_rank(k,page_rank[k]);
        }






    }
















}
