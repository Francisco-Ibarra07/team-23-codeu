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
@WebServlet("/feed")
public class MessageFeedServlet extends HttpServlet{
  
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

    // Use the datastore object to get a list of posted messages across ALL users
    List<Message> messages = datastore.getAllMessages();
    Gson gson = new Gson();

    // Convert the messages into JSON format
    String json = gson.toJson(messages);

    // Return this json to the requestee
    response.getOutputStream().println(json);
  }
}
