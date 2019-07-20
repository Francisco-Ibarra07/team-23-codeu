/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.servlets;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/entity-editor")
public class EntityEditorServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    String messageId = request.getParameter("messageId");
    String newEditedText = Jsoup.clean(request.getParameter("text"), Whitelist.none());
    newEditedText = detectAndReplaceMediaLinks(newEditedText);

    // Replace the old text in the entity with the new one
    Entity targetMessage = datastore.getSpecificMessageEntity(userEmail, messageId);
    targetMessage.setProperty("text", newEditedText);

    // Update the new entity on datastore
    datastore.storeEntity(targetMessage);

    response.sendRedirect("/user-page.html?user=" + userEmail);
  }

  /**
   * This method defines some Regular Expressions (RegEx) to detect any URL's linking to images or videos.
   * 
   * If a URL link is found, the link in the string is replaced and stored with a <img> or <video> HTML tag so 
   * that the link is rendered when the user page is refreshed.
   * 
   * If a format like '![caption here](/url/of/image.jpg)' is detected, the link will be stored in a <figure> HTML tag.
   * This <figure> tag helps by putting both the image and text in one container.
   * 
   * If no links were found, the user text will not be affected.
   * 
   * @param userText is the incoming text that the user typed in before hitting "Submit"
   * @return the new text containing HTML content
   */
  private String detectAndReplaceMediaLinks(String userText) {

    String updatedText;

    // Regexes to detect image links, video links, or image caption formats like ![caption here](/url/of/image.jpg)
    String imageRegex = "(https?://\\S+\\.(png|jpg|gif))";
    String videoRegex = "(https?://\\S+\\.(mp4|webm|ogg|3gp))";
    String localhostFormatRegex = "(https?://(localhost:8080/){1}.+)";
    String liveappFormatRegex = "(https?://(lh3.googleusercontent.com){1}.+)";
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
      updatedText = updatedText.replaceAll(liveappFormatRegex, imageReplacement);   
    }

    return updatedText;
  }
}
