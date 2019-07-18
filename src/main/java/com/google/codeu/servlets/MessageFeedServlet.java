package com.google.codeu.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


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

    /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String userText = Jsoup.clean(request.getParameter("text"), Whitelist.none());

    // Utilize helper method to replace any image links into html tags
    String newText = detectAndReplaceMediaLinks(userText);
    
    // Store new text to the database
    Message message = new Message(user, newText);
    datastore.storeMessage(message);

    //redirect back to feed.html instead of user.html
    response.sendRedirect("/feed.html");
  }


 private String detectAndReplaceMediaLinks(String userText) {

    String updatedText;

    // Regexes to detect image links, video links, or image caption formats like ![caption here](/url/of/image.jpg)
    String imageRegex = "(https?://\\S+\\.(png|jpg|gif))";
    String videoRegex = "(https?://\\S+\\.(mp4|webm|ogg|3gp))";
    String localhostFormatRegex = "(https?://(localhost:8080/){1}.+)";
    String imageCaptionFormatRegex = "!{1}\\[{1}(.+)]{1}\\({1}(https?://\\S+\\.(png|jpg|gif))\\){1}";

    // If a link is found, this is what it would be replaced with
    String imageReplacement = "<img src=\"$1\" alt=\"Couldn't load image\" />";
    String imageCaptionReplacement = "<figure>" + "<img src=\"$2\" alt=\"Couldn't load image\" />" + "<figcaption>$1</figcaption>" + "</figure>";
    String videoReplacement = "<video width=\"320\" height=\"240\" controls loop>" + "<source src=\"$1\" type=\"video/mp4\">\n" + "</video>";
  
    // Check to see if the text contains a ![Caption Text](http//LinkToImage.jpg)
    Boolean captionFormatIsFound = Pattern.compile(imageCaptionFormatRegex).matcher(userText).find();
    if(captionFormatIsFound) {
      updatedText = userText.replaceAll(imageCaptionFormatRegex, imageCaptionReplacement);
    }
    else {
      // Replace any image or video links as <img> tags so they can be rendered on a website                      
      updatedText = userText.replaceAll(imageRegex, imageReplacement); 
      updatedText = updatedText.replaceAll(videoRegex, videoReplacement);    
      updatedText = updatedText.replaceAll(localhostFormatRegex, imageReplacement);    
    }

    return updatedText;
  }

}
