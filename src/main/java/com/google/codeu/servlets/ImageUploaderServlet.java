package com.google.codeu.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet has two jobs:
 *  1) Takes a user's image upload and sends it to Blobstore so it can store it somewhere.
 *  2) Recieves the URL link of where Blobstore stored the image and gives this new link to the MessageServlet 
 *     so it can create a new message on the user-page.
 */
@WebServlet("/image-uploader-form")
public class ImageUploaderServlet extends HttpServlet {

  /**
   * Returns HTML that contains a form for a user to fill in a description and to upload an image from their computer. 
   * The uploaded image is given to Blobstore so it can store it somewhere and return a url link to the new stored image.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the Blobstore URL
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/image-uploader-form") ;

    // Set the response to be in HTML format
    response.setContentType("text/html");

    ServletOutputStream out = response.getOutputStream();

    // Create a <form> where the user can input a text description and have a button to do a image file upload
    out.println("<form method=\"POST\" enctype=\"multipart/form-data\" action=\"" + uploadUrl + "\">");
    out.println("<p>Type in a description:</p>");
    out.println("<textarea name=\"message\"></textarea>");
    out.println("<br/>");
    out.println("<p>Upload the image:</p>");
    out.println("<input type=\"file\" name=\"image\">");
    out.println("<br/><br/>");
    out.println("<button>Submit</button>");
    out.println("</form>");
  }

  /**
   * Obtains the URL link of the new uploaded image from Blobstore. Also puts together the user's description and the 
   * URL link together into one text message and sends this to the MessageServlet.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the message entered by the user.
    String message = request.getParameter("message");

    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");
    if(imageUrl == null) {
      // Send the user back to their user page if no file was uploaded
      UserService userService = UserServiceFactory.getUserService();
      String user = userService.getCurrentUser().getEmail();
      response.sendRedirect("/user-page.html?user=" + user);
      return;
    }

    // Construct the text message that will be sent to the MessageServlet
    String fullMessage = message + " " + imageUrl;

    response.setContentType("text/html");
    ServletOutputStream out = response.getOutputStream();
    
    // Output some HTML that shows the data the user entered.
    // Gives a user a 'Submit' button to confirm that the information is correct.
    // Once 'Submit' is pressed, the description and image URL link is sent to the MessageServlet so it can create a new Message.
    out.println("<form method=\"POST\" action=\"/messages\">");
    out.println("<p>Here's the image you uploaded:</p>");
    out.println("<a href=\"" + imageUrl + "\">");
    out.println("<img src=\"" + imageUrl + "\" />");
    out.println("</a>");
    out.println("<p>Here's the text you entered:</p>");
    out.println(message);
    out.println("<textarea name=\"text\" style=\"display:none;\">" + fullMessage + "</textarea>");
    out.println("<p>Is this info correct?</p>");
    out.println("<input type=\"submit\" name=\"Submit\">");
    out.println("</form>");
  }


  /**
   * Returns a URL that points to the uploaded file, or null if the user didn't upload a file.
   */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName){
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (devserver)
    if(blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    return imagesService.getServingUrl(options);
  }

}
