package crawler;

import crawler.Spider;
import database.mongoDB;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class SpiderTest {
    public static void main(String[] args) throws Exception {
        mongoDB DB = new mongoDB("A");
        //  mongoDB DB = new mongoDB("B55");
        List<String> seeds = new LinkedList<String>();
        seeds.add("https://www.geeksforgeeks.org/");
        seeds.add("https://www.javatpoint.com/");

        // seeds.add("https://www.youm7.com/");
        seeds.add("https://egypt.souq.com/eg-en/");
        seeds.add("https://www.amazon.com/");
        seeds.add("https://cu.edu.eg/ar/");
        seeds.add("https://en.wikipedia.org/w/index.php?search=");
        seeds.add("https://www.facebook.com/");
        seeds.add("https://www.linkedin.com/home");
        seeds.add("https://www.hackerrank.com/");
        System.out.print("Enter the number of threads:");
        Scanner keyboard = new Scanner(System.in);
        int num_of_threads = keyboard.nextInt();
        if (num_of_threads < 1) {
            System.out.println("Invalid number of threads");
            System.out.println("Running the Crawler in a Single thread");
            num_of_threads = 1;
        }
        //Spider spider = new Spider(seeds);
        Spider spider = new Spider(seeds, DB);
        Thread[] crawlers = new Thread[num_of_threads];
        for (int i = 0; i < num_of_threads; i++) {
            crawlers[i] = new Thread(spider);
            crawlers[i].setName(String.valueOf(i + 1));
            crawlers[i].start();
        }
        for (int i = 0; i < num_of_threads; i++)
            crawlers[i].join();
        System.out.println("Finished Crawling Successfully..");
//        for (int i=0;i<spider.pagesVisited.size();i++)
//            System.out.println(spider.pagesVisited.get(i));

//        System.out.println(spider.pagesVisited.size());
    }
}
