package mongo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.JadeTemplateEngine;
import utils.ExampleRunner;

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
public class VertxRest extends AbstractVerticle {

    private static final String WEB_EXAMPLES_JAVA_DIR = "src/main/java/";

    public static void main(String[] args) {
        ExampleRunner.runJavaExample(WEB_EXAMPLES_JAVA_DIR, VertxRest.class, false);
    }

    @Override
    public void start() throws Exception {
        // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
        final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
        // In order to use a JADE template we first need to create an engine
        final JadeTemplateEngine jade = JadeTemplateEngine.create();
        // To simplify the development of the web components we use a Router to route all HTTP requests
        // to organize our code in a reusable way.
        final Router router = Router.router(vertx);
        // Enable the body parser to we can get the form data and json documents in out context.
        router.route().handler(BodyHandler.create());
        setRoutes(mongo, jade, router);
        // start a HTTP web server on port 8080
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void setRoutes(final MongoClient mongo, final JadeTemplateEngine jade, final Router router) {
        setEntryPoint(jade, router);
        setGetUsersRoute(mongo, router);
        setCreateUserRoute(mongo, router);
        setDeleteUserRoute(mongo, router);
        // Serve the non private static pages
        router.route().handler(StaticHandler.create());
    }

    private void setDeleteUserRoute(final MongoClient mongo, final Router router) {
        // Remove a document from mongo.
        router.delete("/users/:id").handler(routingContext -> {
            // catch the id to remove from the url /users/:id and transform it to a mongo query.
            mongo.removeOne("users", new JsonObject().put("_id", routingContext.request().getParam("id")), deleteUserAsyncResultHandler(routingContext));
        });
    }

    private void setCreateUserRoute(final MongoClient mongo, final Router router) {
        // Create a new document on mongo.
        router.post("/users").handler(routingContext -> {
            // since jquery is sending data in multipart-form format to avoid preflight calls, we need to convert it to JSON.
            JsonObject user = new JsonObject()
                    .put("username", routingContext.request().getFormAttribute("username"))
                    .put("email", routingContext.request().getFormAttribute("email"))
                    .put("fullname", routingContext.request().getFormAttribute("fullname"))
                    .put("location", routingContext.request().getFormAttribute("location"))
                    .put("age", routingContext.request().getFormAttribute("age"))
                    .put("gender", routingContext.request().getFormAttribute("gender"));

            // insert into mongo
            mongo.insert("users", user, insertUserAsyncResultHandler(routingContext));
        });
    }

    private void setGetUsersRoute(final MongoClient mongo, final Router router) {
        // Read all users from the mongo collection.
        router.get("/users").handler(routingContext -> {
            // issue a find command to mongo to fetch all documents from the "users" collection.
            mongo.find("users", new JsonObject(), getUsersAsyncResultHandler(routingContext));
        });
    }

    private void setEntryPoint(final JadeTemplateEngine jade, final Router router) {
        // Entry point to the application, this will render a custom JADE template.
        router.get("/").handler(routingContext -> {
            // we define a hardcoded title for our application
            routingContext.put("title", "Vert.x Web");
            // and now delegate to the engine to render it.
            jade.getJadeConfiguration().getSharedVariables();
            jade.render(routingContext, "templates/index", renderTemplateAsyncResultHandler(routingContext));
        });
    }

    private Handler<AsyncResult<Buffer>> renderTemplateAsyncResultHandler(final RoutingContext routingContext) {
        return res -> {
            if (res.succeeded()) {
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(res.result());
            } else {
                routingContext.fail(res.cause());
            }
        };
    }

    private Handler<AsyncResult<String>> insertUserAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            // error handling
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            // inform that the document was created
            routingContext.response().setStatusCode(201);
            routingContext.response().end();
        };
    }

    private Handler<AsyncResult<List<JsonObject>>> getUsersAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            // error handling
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            // now convert the list to a JsonArray because it will be easier to encode the final object as the response.
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            // since we are producing json we should inform the browser of the correct content type.
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            // encode to json string
            routingContext.response().end(json.encode());
        };
    }

    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final RoutingContext routingContext) {
        return lookup -> {
            // error handling
            if (lookup.failed()) {
                routingContext.fail(lookup.cause());
                return;
            }
            // inform the browser that there is nothing to return.
            routingContext.response().setStatusCode(204);
            routingContext.response().end();
        };
    }

}