package web;


import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class page2 extends HttpServlet {
    int start, finish,num_of_pages,end;
    List<String> snippet = new ArrayList<>(),content = new ArrayList<>(),link = new ArrayList<>(),title = new ArrayList<>();
    List<Integer>first_index=new ArrayList<>();
    List<String>first_word=new ArrayList<>();
    String search;
    StringTokenizer str_token;
    List<String> file_token = new ArrayList<>();
    String snip=null;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("kb");
        num_of_pages = (int) request.getSession().getAttribute("num_of_pages");
        title = (List<String>) request.getSession().getAttribute("title");
        link = (List<String>) request.getSession().getAttribute("link");
        snippet = (List<String>) request.getSession().getAttribute("snippet");
        content = (List<String>) request.getSession().getAttribute("content");
        str_token = (StringTokenizer) request.getSession().getAttribute("str_token");
        file_token = (List<String>) request.getSession().getAttribute("file_token");
        first_index = (List<Integer>) request.getSession().getAttribute("first_index");
        first_word = (List<String>) request.getSession().getAttribute("first_word");
        search = (String) request.getSession().getAttribute("search");
        end = (int) request.getSession().getAttribute("end");
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
        for (int j = 1; j <= num_of_pages; j++) {
            if (action.equals("button" + j)) {
                ////10 not 2
                start = (j - 1) * 10;
                ////10 not 2
                finish = start + 10;
                if (finish > link.size())
                    finish = link.size();
                for (int i = start; i < finish; i++) {
                    out.println("<p id=\"titles\"><a href=\"");
                    out.println(link.get(i));
                    out.println("\" target=\"_blank\">");
                    out.println(title.get(i));
                    out.println("</a></p>\n" +
                            "        <p id=\"links\">");
                    out.println(link.get(i));
                    out.println("</p>\n" +
                            "        <p id=\"snipped\">");
                    if(first_index.get(i)==null)
                        System.out.println(i);
                    int found = first_index.get(i);

                    end = found + 300;
                    System.out.println(found);
                    if (end > content.get(i).length())
                        end = content.get(i).length();
//                    snippet.add(content.get(i).substring(found, end));
//                    snippet.set(i, snippet.get(i).replaceAll(first_word.get(i), "<strong>" + first_word.get(i) + "</strong>"));
//                    snippet.set(i, snippet.get(i).concat("..."));
//                    snippet.add(s.extractWebPageSnippet(content.get(i), search));
//                    out.println(snippet.get(i));
                    snip="<strong>" + first_word.get(i) + "</strong>";
                    snip=snip.concat(content.get(i).substring(found+first_word.get(i).length(), end));
//                    snip=content.get(i).substring(found, end);
//                    snip=snip.replaceAll(first_word.get(i), "<strong>" + first_word.get(i) + "</strong>");
                    snip= snip.concat("...");

                    out.println(snip);
                    out.println("</p>");
                }
            }
        }
        out.println("        <form action=\"pagereq\" method=\"GET\" id=\"pagereq\">\n" +
                "         <ul id=\"butt\">");
        for (int i = 1; i <= num_of_pages; i++) {
            if (action.equals("button" + i)) {
                out.println("<li ><button type=\"num_of_pages\" name=\"kb\" value=\"button" +
                        i +
                        "\" style=\"text-decoration:underline; background:  rgb(227, 227, 246);\" >" +
                        i +
                        "</button></li>");
            } else {
                out.println("<li ><button type=\"num_of_pages\" name=\"kb\" value=\"button" +
                        i +
                        "\">" +
                        i +
                        "</button></li>");
            }
        }
        out.println("         </ul>\n" +
                "        </form>");
        out.println("    </main>\n" +
                "</body>\n" +
                "</html>");
        out.close();
    }
}