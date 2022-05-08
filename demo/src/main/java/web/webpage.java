package web;
import ranker.Result;
import ranker.queryprocessor;
import database.mongoDB;
import org.bson.Document;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class webpage extends HttpServlet {
    String search;
    String query;
    int counter = 0;
    int num_of_pages = 0;
    int end = 0;
    StringTokenizer strtoken ;
    List<String> filetoken = new ArrayList<String>();
    String con;
    List<String> title = new ArrayList<>(), link = new ArrayList<>(), content = new ArrayList<>(), snippet = new ArrayList<>();
    List<String> suggestions = new ArrayList<>();
    mongoDB DB = new mongoDB("A");
    queryprocessor process;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        search = request.getParameter("search");
        PrintWriter out = response.getWriter();
        //process=new queryprocessor(search);

        process=new queryprocessor(DB,search);

        out.println("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"styles/style2.css\" />\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>search</title>\n" +
                "</head>\n" +
                "<header>\n" +
                "    <ul>\n" +
                "        <li>\n" +
                "            <a href=\"index.html\"><img src=\"images/R1.jpg\" alt=\"photo\" /></a>\n" +
                "        </li>\n" +
                "    <li>\n" +
                "        <h1>Results</h1>\n" +
                "    </li>\n" +
                "</ul>\n" +
                "  </header>\n" +
                "<body>\n" +
                "    <main>");
        // Hello
        List<String> allwords=new ArrayList<String>();
        List<String> wordsfound=new ArrayList<String>();
        Iterator<Document> wordsCollectionItr = DB.getAllwords().iterator();
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
        for (Integer i : result.id_final.keySet())
        {
            Document doc= DB.Crawlled.find(eq("id",i)).first();
            title.add(doc.getString("title"));
            link.add(doc.getString("url"));
            content.add(doc.getString("content"));
//            System.out.println(title.get(counter));
//            System.out.println(link.get(counter));
            if(counter==1)
                System.out.println(content.get(counter));
            counter++;
        }
        System.out.println(counter);
//        for (Document d : DB.getAllPages()) {
//            con = d.getString("content");
//            if (con.contains(search.toLowerCase())) {
//                title.add(d.getString("title"));
//                link.add(d.getString("url"));
//                content.add(d.getString("content"));
//                counter++;
//            }
//        }
        strtoken = new StringTokenizer(search);
        while(strtoken.hasMoreTokens()){
            filetoken.add(strtoken.nextToken());}
        for (Document d : DB.getAllsuggestions()) {
            suggestions.add(d.getString("suggestions"));
        }
        if(!suggestions.contains(search))
            DB.insertsuggest(search);
        System.out.println(counter);
        if (counter == 0)
            out.println("<p id=\"no\">NO RESULT!!</p>");
        else {
            ////10 not 2
            if (counter % 10 != 0 && counter > 10)
                counter++;
            ////10 not 2
            num_of_pages = counter / 10;
            int it = counter;
            ////10 not 2
            if (it > 10)
                ////10 not 2
                it = 10;
            for (int i = 0; i < it; i++) {
                out.println("<p id=\"titles\"><a href=\"");
                out.println(link.get(i));
                out.println("/\">");
                out.println(title.get(i));
                out.println("</a></p>\n" +
                        "        <p id=\"links\">");
                out.println(link.get(i));
                out.println("</p>\n" +
                        "        <p id=\"snipped\">");
                int found=content.get(i).indexOf(search);
                if(found==-1) {
                    for (int o = 0; o < filetoken.size(); o++) {
                        if (content.get(i).indexOf(filetoken.get(o)) != -1) {
                            found = content.get(i).indexOf(filetoken.get(o));
                            break;
                        }
                    }
                }
                if(found==-1)
                {
                    filetoken=queryprocessor.getwordsfound();
                    for (int o = 0; o < filetoken.size(); o++) {
                        if (content.get(i).indexOf(filetoken.get(o)) != -1) {
                            found = content.get(i).indexOf(filetoken.get(o));
                            break;
                        }
                    }
                }
                if(found==-1)
                    found=0;
                end = found + 300;
                System.out.println(found);
                if (end > content.get(i).length())
                    end = content.get(i).length();
                snippet.add(content.get(i).substring(found, end));
                //snippet.set(i, snippet.get(i).replaceAll(search.toLowerCase(Locale.ROOT), "<strong>" + search + "</strong>"));
                snippet.set(i, snippet.get(i).concat("..."));
                out.println(snippet.get(i));
                out.println("</p>");
            }
            out.println("        <form action=\"pagereq\" method=\"GET\" id=\"pagereq\">\n" +
                    "         <ul id=\"butt\">");
            out.println("<li ><button type=\"num_of_pages\" name=\"kb\" value=\"button" +
                    1 +
                    "\" style=\"text-decoration:underline; background:  rgb(227, 227, 246);\" >" +
                    1 +
                    "</button></li>");
            for (int i = 2; i <= num_of_pages; i++) {
                out.println("<li ><button type=\"num_of_pages\" name=\"kb\" value=\"button" +
                        i +
                        "\">" +
                        i +
                        "</button></li>");
            }
            out.println("         </ul>\n" +
                    "        </form>");
        }
        out.println("    </main>\n" +
                "</body>\n" +
                "</html>");
        request.getSession().setAttribute("num_of_pages", num_of_pages);
        request.getSession().setAttribute("title", title);
        request.getSession().setAttribute("link", link);
        request.getSession().setAttribute("snippet", snippet);
        request.getSession().setAttribute("content", content);
        request.getSession().setAttribute("end", end);
        request.getSession().setAttribute("search", search);
        request.getSession().setAttribute("filetoken",filetoken);
        request.getSession().setAttribute("strtoken",strtoken);
        out.close();
    }
}