package org.politrons;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * This is an example application to showcase the usage of MongDB and Vert.x Web.
 * <p>
 * In this application you will see the usage of:
 * <p>
 * * JADE templates
 * * Mongo Client
 * * Vert.x Web
 * <p>
 * The application allows to list, create and delete mongo documents using a simple web interface.
 *
 * @author <a href="mailto:pmlopes@gmail.com>Paulo Lopes</a>
 */
@Component
public class VertxRest {

    private Vertx vertx;

    @Resource
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @PostConstruct
    public void start() throws Exception {
        // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
        final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));

        // To simplify the development of the web components we use a Router to route all HTTP requests
        // to organize our code in a reusable way.
        final Router router = Router.router(vertx);
        // Enable the body parser to we can get the form data and json documents in out context.
        router.route().handler(BodyHandler.create());
        setRoutes(mongo, router);
        // start a HTTP web server on port 8080
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void setRoutes(final MongoClient mongo, final Router router) {
        setGetUsersRoute(mongo, router);
        setCreateUserRoute(mongo, router);
        setDeleteUserRoute(mongo, router);
        setGetUserRoute(mongo, router);
        // Serve the non private static pages
        router.route().handler(StaticHandler.create());
    }

    private void setDeleteUserRoute(final MongoClient mongo, final Router router) {
        router.delete("/users/:id").handler(routingContext -> {
            mongo.removeOne("users", new JsonObject().put("_id", routingContext.request().getParam("id")), deleteUserAsyncResultHandler(routingContext));
        });
    }

    private void setCreateUserRoute(final MongoClient mongo, final Router router) {
        // Create a new document on mongo.
        router.post("/users").handler(routingContext -> {
            JsonObject user = new JsonObject()
                    .put("username", routingContext.request().getFormAttribute("username"))
                    .put("email", routingContext.request().getFormAttribute("email"))
                    .put("fullname", routingContext.request().getFormAttribute("fullname"))
                    .put("location", routingContext.request().getFormAttribute("location"))
                    .put("age", routingContext.request().getFormAttribute("age"))
                    .put("gender", routingContext.request().getFormAttribute("gender"));
            mongo.insert("users", user, insertUserAsyncResultHandler(routingContext));
        });
    }

    private void setGetUsersRoute(final MongoClient mongo, final Router router) {
        router.get("/users").handler(routingContext -> {
            mongo.find("users", new JsonObject(), getUsersAsyncResultHandler(routingContext));
        });
    }

    private void setGetUserRoute(final MongoClient mongo, final Router router) {
        router.get("/user/:attributeName/:value").handler(routingContext -> {
            JsonObject query = new JsonObject();
            query.put(routingContext.request().getParam("attributeName"), routingContext.request().getParam("value"));
            mongo.findOne("users", query, null, getUserAsyncResultHandler(routingContext));
        });
    }

    private Handler<AsyncResult<String>> insertUserAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            routingContext.response().setStatusCode(201);
            routingContext.response().end();
        };
    }

    private Handler<AsyncResult<JsonObject>> getUserAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            final JsonObject json = lookup.result();
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            // encode to json string
            routingContext.response().end(json.encode());
        };
    }

    private Handler<AsyncResult<List<JsonObject>>> getUsersAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            // encode to json string
            routingContext.response().end(json.encode());
        };
    }

    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            routingContext.response().setStatusCode(204);
            routingContext.response().end();
        };
    }

}