package web;

import ranker.Result;
import ranker.queryprocessor;
import database.mongoDB;
import org.bson.Document;

import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class webpage extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String query,query_with_stop_words,search;
        int num_of_pages = 0,end = 0,counter = 0;
        StringTokenizer str_token;
        List<String> file_token = new ArrayList<>();
        List<String> title = new ArrayList<>(), link = new ArrayList<>(), content = new ArrayList<>(), snippet = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        mongoDB DB = new mongoDB("mongodb");
        queryprocessor process;
        List<Integer>first_index=new ArrayList<>();
        List<String>first_word=new ArrayList<>();
        List<String>all_title= new ArrayList<>(),all_content= new ArrayList<>(),all_link= new ArrayList<>();
        HashMap<Integer, Document> temp_map1; // mosh phrase
        HashMap<Integer, Document> temp_map2=null;
        //List<String> all_words = new ArrayList<>(),words_found;
//        long start,finish;
//        float sec;
//        long start_page,end_page;
//        float sec_page;
        String snip;
        /////////////////////////////////////
//        start_page=System.currentTimeMillis();
        search = request.getParameter("search");
        PrintWriter out = response.getWriter();
        process = new queryprocessor(DB, search);
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
        //Iterator<Document> wordsCollectionItr = DB.getAllwords().iterator();
        query = process.getquery();
        query=query.replace(".","");
        query_with_stop_words=query;
        //System.out.println(query);
//        while (wordsCollectionItr.hasNext()) {
//            Document d = wordsCollectionItr.next();
//            String word = d.getString("word");
//            all_words.add(word);
//        }

        process.processes(query);

        //words_found = process.getwordsfound();
        //for (String value : words_found) System.out.println(value);
        //////////////////////
        Result result = new Result(DB, process);
        result.fill_word_idf();
        result.fill_word_url_tf();
        result.fill_id_tf_idf();
        for (Integer i : result.id_rank.keySet()) {
            System.out.printf("id:%d , TF-IDF:%f \n ", i, result.id_rank.get(i));
        }

        for (Integer i : result.id_count.keySet()) {
            System.out.printf("id:%d , words_count:%d \n ", i, result.id_count.get(i));
        }
        result.sort_id_rank();

        for (Integer i : result.id_final.keySet()) {
            System.out.printf("id:%d \n ", i);
        }
        result.sort_id_rank();
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

        }

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

        // Loop through the crawled pages and index them
        for (Document d : DB.getAllPages()) {
            all_title.add(d.getString("title"));
            all_link.add(d.getString("url"));
            all_content.add(d.getString("content"));
        }
        if(temp_map1!=null) {
            for (Integer i : temp_map1.keySet()) {

                title.add(all_title.get(i));
                link.add(all_link.get(i));
                content.add(all_content.get(i));

                counter++;
            }

            for (Integer i : temp_map1.keySet())
            {
                first_index.add(temp_map1.get(i).getInteger("first_index"));
                first_word.add(temp_map1.get(i).getString("first_word"));
            }
        }
        if(temp_map2!=null) {
            for (Integer i : temp_map2.keySet()) {
                title.add(all_title.get(i));
                link.add(all_link.get(i));
                content.add(all_content.get(i));
                counter++;
            }
            for (Integer i : temp_map2.keySet())
            {
                first_index.add(temp_map2.get(i).getInteger("first_index"));
                first_word.add(temp_map2.get(i).getString("first_word"));
            }
        }
        str_token = new StringTokenizer(search);
        while (str_token.hasMoreTokens()) {
            file_token.add(str_token.nextToken());
        }
        for (Document d : DB.getAllsuggestions()) {
            suggestions.add(d.getString("suggestions"));
        }
        if (!suggestions.contains(search))
            DB.insertsuggest(search);
        //System.out.println(counter);
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
                out.println("<p id=\"pakage\">");
                out.println("<p id=\"titles\"><a href=\"");
                out.println(link.get(i));
                out.println("\" target=\"_blank\">");
                out.println(title.get(i));
                out.println("</a></p>\n" +
                        "        <p id=\"links\">");
                out.println(link.get(i));
                out.println("</p>\n" +
                        "        <p id=\"snipped\">");
                int found = first_index.get(i);
                //System.out.println("index "+first_index.get(i));
                end = found + 300;
                //System.out.println(found);
                if (end > content.get(i).length())
                    end = content.get(i).length();
//                snippet.add(content.get(i).substring(found, end));
//                snippet.set(i, snippet.get(i).replaceAll(first_word.get(i), "<strong>" + first_word.get(i) + "</strong>"));
//                snippet.set(i, snippet.get(i).concat("..."));
//                snippet.add(s.extractWebPageSnippet(content.get(i), search));
//                out.println(snippet.get(i));
                snip="<strong>" + first_word.get(i) + "</strong>";

                snip=snip.concat(content.get(i).substring(found+first_word.get(i).length(), end));
//                snip=content.get(i).substring(found, end);
//                snip=snip.replaceAll(first_word.get(i), "<strong>" + first_word.get(i) + "</strong>");
                snip= snip.concat("...");

                out.println(snip);
                out.println("</p>");
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
        request.getSession().setAttribute("file_token", file_token);
        request.getSession().setAttribute("str_token", str_token);
        request.getSession().setAttribute("first_index", first_index);
        request.getSession().setAttribute("first_word", first_word);
//        end_page = System.currentTimeMillis();
//        sec_page = (end_page - start_page) / 1000F; System.out.println("total_time= "+sec_page + " seconds");
        ////////////////////////
        out.close();
    }
}