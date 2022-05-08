package crawler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import java.net.*;
//import java.net.MalformedURLException;
//import java.io.IOException;
public class SpiderLeg {
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
//    private static final String USER_AGENT =
//            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links ;
    Document htmlDocument;
    ArrayList disallowList ;
    String currentUrl;
    public static final String AGENT = "*";

    private ConcurrentHashMap<String, RobotsRules> concMap=new ConcurrentHashMap<String, RobotsRules>();
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
            //else?
            if (!connection.response().contentType().contains("text/html")) {
                //System.out.println("*Failure* Retrieved something other than HTML");
                return false;
            }
            //make sure that we need the following
            if(this.htmlDocument == null)
                return false;
            if(this.htmlDocument.body() == null)
                return false;

//            if(chek_multiple(currentUrl, spiderobj) == true)
//            {
//                return false;
//            }
            // String bodyText =this.htmlDocument.body().text();
     /*      if( setRobotList(url,spiderobj) == false)
           {
               System.out.println(Thread.currentThread().getName()+"error in robot list with link"+currentUrl);
               return false;
           }*/

            //for debugging
//            for(int i =0 ; i<disallowList.size();i++) {
//                System.out.println(Thread.currentThread().getName() + ": "+currentUrl+ " =>> " + disallowList.get(i) + " --> [robotlist]");
//            }
            if(spiderobj.count_id > spiderobj.MAX_PAGES_TO_SEARCH)
                return true;

            Elements linksOnPage = htmlDocument.select("a[href]");
            //System.out.println(htmlDocument.title()+"==>"+url);
            //System.out.println("Found (" + linksOnPage.size() + ") links");
            String url_L;
            for (Element link : linksOnPage)
            {
                //check the # before /                                  //m.s??
                // remove # from the url if exist
                url_L= link.absUrl("href");//attr("abs:href");//m.s of this
                if (url_L.contains("#")) {
                    url_L = url_L.substring(0, url_L.indexOf("#") - 1);
                }


                // synchronized (spiderobj)
                {
                    //if(checkRobots55(url_L) == true )//&& checkRobots(url_L) == true)
                    if(isUrlAllowed(url_L)== true )
                    {
                        // remove / from the url if exist
                        if (url_L.endsWith("/")) {
                            url_L = url_L.substring(0, url_L.length() - 1);
                        }

                        ////////////samaa_6: if(chek_multiple(url_L, spiderobj) == false)

                        {
                            // this.links.add(url_L);
                            if (!links.contains(url_L))
                            {
                                this.links.add(url_L);
                            }

                            //////////samaa_6:  System.out.println(Thread.currentThread().getName() + ": "+currentUrl+ "add =>>"+ url_L);
                        }
                    }
                }

                // this.links.add(link.absUrl("href"));

            }
            return true;
        }
        catch(IOException ioe)
        {
            return false;
        }
    }
    //--------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------//
    public boolean add_seeds_keys(String url,Spider spiderobj)
    {
//        try
//        {
        char[] key = new char[50];
        Connection connection = Jsoup.connect(url);//.userAgent(USER_AGENT);
        Document htmlDocument_child = null;
        try {
            htmlDocument_child = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(connection.response().statusCode() == 200)
        {
            //System.out.println("\n*Visiting* Received web page at " + url);
        }
        if (!connection.response().contentType().contains("text/html")) {
            //System.out.println("*Failure* Retrieved something other than HTML");
            return false;
        }

        if(htmlDocument_child == null)
            return false;
        if(htmlDocument_child.body() == null)
            return false;
        String bodyText = htmlDocument_child.body().text();
        // String bodyText =this.htmlDocument.body().text();
        int p=5;
        if(bodyText.length()>100)
            p*=10;
        for(int i = 0 ; i*p<bodyText.length()&&i<key.length; i++)
            key[i]= bodyText.charAt(i*p);
        String skey = new String(key);
        boolean keyExist = false;
        // synchronized (spiderobj)
        {
            List<String> keysList= spiderobj.getKeyList();
            for (int i=0;i<keysList.size() ;i++) {
                if (Objects.equals(keysList.get(i), skey))
                    keyExist = true;
            }
            if(!keyExist)
            {
                synchronized (spiderobj)
                {
                    keysList.add(skey);
                }
            }

            return keyExist;
        }
        //       }
//        catch(IOException ioe)
//        {
//            return false;
//        }
    }
    //--------------------------------------------------------------------------------------//
    public boolean chek_multiple(String url,Spider spiderobj)
    {
//        try
//        {
        char[] key = new char[50];

//            Document htmlDocument_child = Jsoup.connect(url).userAgent(USER_AGENT).get();
//            if (Jsoup.connect(url).userAgent(USER_AGENT).response().statusCode() == 200)
//                //System.out.println("\n*Visiting* Received web page at " + url);
//                if(htmlDocument_child == null)
//                    return false;
//            if(htmlDocument_child.body() == null)
//                return false;
//            String bodyText = htmlDocument_child.body().text();
        String bodyText =this.htmlDocument.body().text();
        int p=5;
        if(bodyText.length()>100)
            p*=10;
        for(int i = 0 ; i*p<bodyText.length()&&i<key.length; i++)
            key[i]= bodyText.charAt(i*p);
        String skey = new String(key);
        boolean keyExist = false;
        //synchronized (spiderobj)
        {
            List<String> keysList= spiderobj.getKeyList();
            for (int i=0;i<keysList.size() ;i++) {
                if (Objects.equals(keysList.get(i), skey))
                    keyExist = true;
            }
            if(!keyExist)
            {
                synchronized (spiderobj)
                {
                    keysList.add(skey);
                }
            }

            return keyExist;
        }

        //       }
//        catch(IOException ioe)
//        {
//            return false;
//        }
    }
    //--------------------------------------------------------------------------------------//
    //    public boolean searchForWord(String searchWord)
//    {
//        if(this.htmlDocument == null)
//        {
//            System.out.println("ERROR! Call crawl() before performing analysis on the document");
//            return false;
//        }
//        System.out.println("Searching for the word " + searchWord + "...");
//        String bodyText = this.htmlDocument.body().text();
//        return bodyText.toLowerCase().contains(searchWord.toLowerCase());
//    }
    //------------------------------------------------------//
    public boolean setRobotList(String currentUrl ,Spider spy)
    {
        try
        {
            URL robotsFileUrl =
                    new URL(currentUrl + "/robots.txt");
            ////////////////////////////////
            //for debugging:print the robot.txt of the currentUrl
            synchronized (spy)
            {
                System.out.println(currentUrl+"disallowed list:/n");
                currentUrl += "/robots.txt";
                Connection connection = Jsoup.connect(currentUrl);//.userAgent(USER_AGENT);
                Document htmlDocument22 = connection.get();
                String bodyText =htmlDocument22.body().text();
                System.out.println(bodyText+"/n");
                System.out.println("pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp");
            }
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
    public boolean checkRobots(String link) {
        boolean notBlocked = true;

        try {
            URL url = new URL(link);
            String origin = url.getProtocol() + "://" + url.getHost();
            String robot = origin + "/robots.txt";
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(robot).openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("User-agent: *"))
                    break;
            }
            while ((line = in.readLine()) != null) {
                if (line.startsWith("Disallow: ") && link.startsWith(origin + line.substring(10))) {
                    notBlocked = false;
                }
                if (line.startsWith("Allow: ") && link.startsWith(origin + line.substring(8))) {
                    notBlocked = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in parsing robots.txt");
            return false;
        }
        if (!notBlocked) {
            System.out.println(Thread.currentThread().getName() + ": "+this.currentUrl+" =>>>" + link + " --> [Blocked]");
        }
        return notBlocked;
    }

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
                System.out.println(Thread.currentThread().getName() + ": " + link + " --> [samaa:Blocked]");
                allowed =  false;
                break;
            }
        }
        return allowed;
    }

    //////////////////////////////////////////////////////////////////////////
    private class RobotsRules {
        public List<String> disallowedUrls;
        public boolean ready;

        public RobotsRules() {
            disallowedUrls = new ArrayList<String>();
            ready = false;
        }
    }


    public boolean isUrlAllowed(String url) {
        this.putifAbsentRules(url);
        String hostName = null;
        try {
            hostName = new URL(url).getHost();
        } catch (MalformedURLException e) {
            return false;
        }

        List<String> disallowedUrls = this.concMap.get(hostName).disallowedUrls;
        for (String disallowdUrl : disallowedUrls) {
            // match
            Pattern p = Pattern.compile(disallowdUrl);//. represents single character
            Matcher m = p.matcher(url);
            if (m.find()) {
                // LogOutput.printMessage("URL disallowed By robots.txt : " + url);
                //samaa_6: System.out.println("URL disallowed By robots.txt : " + url);
                return false;
            }
        }
        return true;
    }


    private void putifAbsentRules(String url) {
        String hostName = null;
        try {
            hostName = new URL(url).getHost();
        } catch (MalformedURLException e1) {
            //e1.printStackTrace();
        }

        if (hostName == null) {
            return;
        }
        // if absent put it in the map
        RobotsRules rules = this.concMap.putIfAbsent(hostName, new RobotsRules());
        // if null this means it was not visited.
        if (rules == null) {
            //LogOutput.printMessage("Host : " + hostName + " first time prepare robots.txt");
            //samaa_6:System.out.println("Host : " + hostName + " first time prepare robots.txt");
            // go prepare the rules for this host.
            this.update(hostName, this.parseRobotsFile(this.readRobotsFile(url)));
            return;
        }
        // if rules not equal null, that means that the robots text of this host is being prepared,
        // wait for it to be ready by checking the ready flag.
        synchronized (rules) {
            while (!rules.ready) {
                try {
                    //LogOutput.printMessage("Host : " + hostName + " Waiting for robots.txt file");
                    System.out.println("Host : " + hostName + " Waiting for robots.txt file");
                    rules.wait();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }


    public List<String> readRobotsFile(String url) {
        List<String> lines = new ArrayList<String>();
        try {
            URL ur = new URL(url);
            url = ur.getProtocol() + "://" + ur.getHost() + "/robots.txt";
        } catch (MalformedURLException e1) {
            return lines;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return lines;
    }


    public List<String> parseRobotsFile(List<String> fileLines) {
        List<String> disallowedUrls = new ArrayList<String>();
        String userAgent = "";
        for (String line : fileLines) {
            line = line.toLowerCase();
            if (line.startsWith("user-agent")) {
                userAgent = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("disallow")) {
                if (userAgent.equals(AGENT)) {
                    disallowedUrls.add(this.preparePattern(line.substring(line.indexOf(":") + 1).trim()));
                }
            }
        }
        return disallowedUrls;
    }


    public String preparePattern(String p) {
        p = p.replaceAll("\\.", "\\\\.");
        p = p.replaceAll("\\?", "[?]"); // match "?" mark.
        p = p.replaceAll("\\*", ".*"); // if "*" match any sequence of characters.
        p = p.replaceAll("\\{", "%7B");
        p = p.replaceAll("\\}", "%7D");
        return p;
    }

    /**
     * After parsing the Robots file and all rules are ready,
     * update the ready flag of the host rules and assign them.
     *
     * @param hostName
     * @param disallowedUrls
     */
    private void update(String hostName, List<String> disallowedUrls) {
        RobotsRules rules = this.concMap.get(hostName);
        synchronized (rules) {
            rules.ready = true;
            rules.disallowedUrls = disallowedUrls;
            rules.notifyAll();
        }
    }
}
///////////////////////////////////////////////////////////////////////////

