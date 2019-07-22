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
import com.google.codeu.data.Datastore;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/entity-remover")
public class EntityRemoverServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String buffer = request.getReader().readLine();
    String[] array = buffer.split(" ");
    String messageId = array[0].substring(1);
    String userEmail = array[1].substring(0, array[1].length() - 1);

    Entity targetMessage = datastore.getSpecificMessageEntity(userEmail, messageId);
    
    datastore.deleteEntity(targetMessage);

    response.sendRedirect("/user-page.html?user=" + userEmail);
  }

}
