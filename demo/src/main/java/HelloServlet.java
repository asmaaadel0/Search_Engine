
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HelloServlet extends HttpServlet {
    String search;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        // Hello
        final String USER_AGENT =
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
        List<String> seeds = new LinkedList<String>();
        seeds.add("https://www.geeksforgeeks.org/");
        seeds.add("https://www.javatpoint.com/");
        seeds.add("https://www.youm7.com/");
        seeds.add("https://egypt.souq.com/eg-en/");
        seeds.add("https://www.amazon.com/");
        seeds.add("https://cu.edu.eg/ar/");
        seeds.add("https://en.wikipedia.org/w/index.php?search=");
        seeds.add("https://www.facebook.com/");
        seeds.add("https://www.linkedin.com/home");
        seeds.add("https://www.hackerrank.com/");
        System.out.print("Enter the number of threads:");
        search = request.getParameter("search");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + seeds.get(0) + "</h1>");
        out.println("</body></html>");
        for (int i = 0; i < seeds.size(); i++) {
            String url = seeds.get(i);
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            String bodyText = htmlDocument.body().text();
            //return bodyText.toLowerCase().contains(searchWord.toLowerCase());
            char[] snippet = new char[50];
            if (bodyText.toLowerCase().contains(search.toLowerCase())) {
                //set the result
                String page_title = htmlDocument.title();
                for (int j = 0; j < 50; j++)
                    snippet[j] = htmlDocument.body().text().charAt(j);
                //print
                System.out.println(htmlDocument.title() + ", " + Arrays.toString(snippet) + ", " + seeds.get(i));
            }
        }
    }
}
