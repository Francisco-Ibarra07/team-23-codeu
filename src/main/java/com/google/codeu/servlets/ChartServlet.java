package com.google.codeu.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;

/**
 * Handles fetching all messages for the public feed.
 */
@WebServlet("/bookchart")
public class ChartServlet extends HttpServlet{
  
 private Datastore datastore;

 @Override
  public void init() {
    datastore = new Datastore();
  }
 
 /**
  * Responds with a JSON representation of Message data for all users.
  */
 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Set the type of content that will be returned to 'json' format instead of html
    response.setContentType("application/json");
    response.getWriter().println("this is to test everything works"); 
    
  }
}
