package com.google.codeu.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
/**
 * Responds with a hard-coded message for testing purposes.
 */
@WebServlet("/about")
public class AboutMeServlet extends HttpServlet{

    private Datastore datastore;

    @Override
    public void init() {
        datastore = new Datastore();
    }

    /**
     * Responds with the "about me" section for a particular user
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Sets the body type to HTML
        response.setContentType("text/html");

        String user = request.getParameter("user");

        // Request is invalid, return an empty response
        if(user == null || user.equals("")) {
            return;
        }

        // If the user is valid, print out a short sentence with their name
        String aboutMe = "This is " + user + "'s about me.";
        response.getOutputStream().println(aboutMe);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.sendRedirect("/index.html");
            return;
        }

        String userEmail = userService.getCurrentUser().getEmail();
        System.out.println("Saving about me for " + userEmail);
        // TODO: save the data

        response.sendRedirect("/user-page.html?user=" + userEmail);
    }
}
