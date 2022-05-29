package crawler;
import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SpiderLeg {
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
//    private static final String USER_AGENT =
//            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links ;
    Document htmlDocument;
    ArrayList disallowList ;
    String currentUrl;
    public static final String AGENT = "*";

    // private ConcurrentHashMap<String, RobotsRules> concMap=new ConcurrentHashMap<String, RobotsRules>();
    //--------------------------------------------------------------------------------------//
    SpiderLeg(String url)
    {
        currentUrl=url;
        links = new LinkedList<String>();
        disallowList = new ArrayList();
    }
    //--------------------------------------------------------------------------------------//
    SpiderLeg() {
        links = new LinkedList<String>();
        disallowList = new ArrayList();
    }
    //--------------------------------------------------------------------------------------//
    public boolean crawl(String url, Spider spiderobj)
    {
        //disallowList = new ArrayList();
        try
        {

            currentUrl = url;
            Connection connection = Jsoup.connect(url);//.ignoreContentType(true);//.userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            if (connection.response().statusCode() == 200)
            {
                //System.out.println("\n*Visiting* Received web page at " + url);
            }
            if (!connection.response().contentType().contains("text/html")) {
                //System.out.println("Failure Retrieved something other than HTML");
//                System.out.println("url ==  "+url);
                return false;
            }
            //make sure that we need the following
            if(this.htmlDocument == null)
            {
                //System.out.println("NULL url ==  "+url);
                return false;
            }
            if(this.htmlDocument.body() == null)
            {
                //System.out.println("NULL_BODY url ==  "+url);
                return false;
            }
            if( setRobotList(url)/*,spiderobj)*/ == false)
            {
                disallowList = new ArrayList();
            }
            Elements linksOnPage = htmlDocument.select("a[href]");
            //System.out.println(htmlDocument.title()+"==>"+url);
            //System.out.println("Found (" + linksOnPage.size() + ") links");
            String url_L;
            for (Element link : linksOnPage)
            {

                url_L= link.absUrl("href");
//                if(getNormalizedURL(url_L)==null)
//                {
//                   // System.out.println("url ==  "+url);
//                }
                if(checkRobots55(url_L) == true )
                {
                    // this.links.add(url_L);
                    if (!links.contains(url_L))
                    {
                        this.links.add(url_L);
                    }
                    //  System.out.println(Thread.currentThread().getName() + ": "+currentUrl+ "add =>>"+ url_L);
                }

            }
            return true;
        }
        catch(IOException ioe)
        {
            return false;
        }
    }
    //--------------------------------------------------------------------------------------//
    public boolean setRobotList(String currentUrl )//,Spider spy)
    {
        try
        {
            URL robotsFileUrl =
                    new URL(currentUrl + "/robots.txt");
            ////////////////////////////////
            //for debugging:print the robot.txt of the currentUrl
//            synchronized (spy)
//            {
//                System.out.println(currentUrl+"disallowed list:/n");
//                currentUrl += "/robots.txt";
//                Connection connection = Jsoup.connect(currentUrl);//.userAgent(USER_AGENT);
//                Document htmlDocument22 = connection.get();
//                String bodyText =htmlDocument22.body().text();
//                System.out.println(bodyText+"/n");
//                System.out.println("-------------------------------------------------");
//            }
            ///////////////////////////////
            BufferedReader reader = new BufferedReader(new InputStreamReader(robotsFileUrl.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf("Disallow:") == 0) {
                    String disallowPath = line.substring("Disallow:".length());
                    int commentIndex = disallowPath.indexOf("#");
                    if (commentIndex != - 1) {
                        disallowPath = disallowPath.substring(0, commentIndex);
                    }
                    disallowPath = disallowPath.trim();
                    disallowList.add(disallowPath);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //------------------------------------------------------//
//    public boolean checkRobots(String link) {
//        boolean notBlocked = true;
//
//        try {
//            URL url = new URL(link);
//            String origin = url.getProtocol() + "://" + url.getHost();
//            String robot = origin + "/robots.txt";
//            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(robot).openStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                if (line.startsWith("User-agent: *"))
//                    break;
//            }
//            while ((line = in.readLine()) != null) {
//                if (line.startsWith("Disallow: ") && link.startsWith(origin + line.substring(10))) {
//                    notBlocked = false;
//                }
//                if (line.startsWith("Allow: ") && link.startsWith(origin + line.substring(8))) {
//                    notBlocked = true;
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("Error in parsing robots.txt");
//            return false;
//        }
//        if (!notBlocked) {
//            System.out.println(Thread.currentThread().getName() + ": "+this.currentUrl+" =>>>" + link + " --> [Blocked]");
//        }
//        return notBlocked;
//    }

    //------------------------------------------------------//
    public void reSetLinks()
    {
        List<String> links = new LinkedList<String>();
    }
    //------------------------------------------------------//
    public List<String> getLinks()
    {
        return this.links;
    }

    //------------------------------------------------------//
    public boolean checkRobots55(String link)
    {
        boolean allowed=true;
        for(int i =0 ; i<disallowList.size();i++)
        {
            // if(disallowList.get(i)==link)
            if(link.startsWith(this.currentUrl + disallowList.get(i)))
            {
                // System.out.println(Thread.currentThread().getName() + ": " + link + " --> [samaa:Blocked]");
                allowed =  false;
                break;
            }
        }
        return allowed;
    }

}
///////////////////////////////////////////////////////////////////////////