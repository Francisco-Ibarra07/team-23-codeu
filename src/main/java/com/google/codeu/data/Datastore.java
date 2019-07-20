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

package com.google.codeu.data;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.datastore.v1.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.*; 

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Returns the total number of messages for all users. */
  public int getTotalMessageCount(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("isFulfilled", message.getIsFulfilled());
    messageEntity.setProperty("timestamp", message.getTimestamp());
    
    datastore.put(messageEntity);
  }

  /**
  * Gets messages posted by a specific user.
  *
  * @return a list of messages posted by the user, or empty list if user has never posted a
  *     message. List is sorted by time descending.
  */
  public List<Message> getMessages(String user) {
    List<Message> messages = new ArrayList<>();

    Query query = new Query("Message")
        .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
        .addSort("timestamp", SortDirection.DESCENDING);
    
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");
        Boolean isFulfilled = (Boolean) entity.getProperty("isFulfilled");

        Message message = new Message(id, user, text, timestamp, isFulfilled);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  /**
  * Gets messages posted by multiple users
  *
  * @return a list of messages posted by multiple users. This list is the public feed
  */
  public List<Message> getAllMessages(){

    // Initialize a list to store all messages
    List<Message> messages = new ArrayList<>();

    // Query the database for a list of Entities
    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    // For each of the entities (user messages), grab all info and create a "Message" object
    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty("user");
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");
        Boolean isFulfilled = (Boolean) entity.getProperty("isFulfilled");
        
        // A Message object is constructed from a user's UUID, user's email, their message, and a timestamp
        Message message = new Message(id, user, text, timestamp, isFulfilled);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  public Set<String> getUsers(){
    Set<String> users = new HashSet<>();
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    for(Entity entity : results.asIterable()) {
      users.add((String) entity.getProperty("user"));
    }
    return users;
  }
}
