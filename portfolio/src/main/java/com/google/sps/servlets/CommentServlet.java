// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.repackaged.com.google.api.client.json.Json;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.JsonParser;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
import com.google.sps.data.Comment;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebServlet(urlPatterns = {"/comment", "/comment/*"})
public class CommentServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);

        try {
            Query query = new Query("Comment").addSort("timestamp", Query.SortDirection.DESCENDING);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            PreparedQuery results = datastore.prepare(query);

            List<Comment> comments = new ArrayList<>();
            for (Entity entity : results.asIterable()) {
                long id = entity.getKey().getId();
                String content = (String) entity.getProperty("content");
                long timestamp = (long) entity.getProperty("timestamp");

                Comment comment = new Comment(id, content, timestamp);
                comments.add(comment);
            }

            Gson gson = new Gson();
            json.add("comments", gson.toJsonTree(comments, new TypeToken<List<Comment>>() {
            }.getType()));

        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        resp.setContentType("application/json;");
        resp.getWriter().println(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);

        try {
            JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(req.getInputStream())).getAsJsonObject();
            String content = jsonObject.get("content").getAsString();
            Objects.requireNonNull(content);

            long timestamp = System.currentTimeMillis();

            Entity commentEntity = new Entity("Comment");
            commentEntity.setProperty("content", content);
            commentEntity.setProperty("timestamp", timestamp);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            long id = datastore.put(commentEntity).getId();
            json.addProperty("id", id);

        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        resp.setContentType("application/json;");
        resp.getWriter().println(json);

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);

        try {
            long id = Long.parseLong(req.getPathInfo().substring(1));

            Key commentEntityKey = KeyFactory.createKey("Comment", id);
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.delete(commentEntityKey);

        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        resp.setContentType("application/json;");
        resp.getWriter().println(json);
    }
}
