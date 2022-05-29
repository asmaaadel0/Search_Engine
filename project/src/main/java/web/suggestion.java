package web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyVetoException;

import database.mongoDB;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(urlPatterns = {"/Suggestion"})
public class suggestion extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");
        try {
            Suggest(response);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

    }

    public void Suggest(HttpServletResponse response) throws IOException, PropertyVetoException {
        List<String> suggestions = new ArrayList<>();
        mongoDB DB = new mongoDB("mongodb");
        for (Document d : DB.getAllsuggestions()) {
            suggestions.add(d.getString("suggestions"));
        }
        PrintWriter out = response.getWriter();
        for (String suggestion : suggestions) out.print(suggestion + "-");
    }

}