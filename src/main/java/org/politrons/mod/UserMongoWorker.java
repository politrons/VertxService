package org.politrons.mod;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by pabloperezgarcia on 24/9/15.
 */


public class UserMongoWorker extends AbstractVerticle {


    public static final String MONGO_FIND_USER = "mongo.find.user";
    public static final String MONGO_DELETE_USER = "mongo.delete.user";
    public static final String FIND_USER_CLIENT = "find.user.client";
    public static final String DELETE_USER_CLIENT = "delete.user.client";

    @Override
    public void start() throws Exception {
        System.out.println("[Worker] Starting in " + Thread.currentThread().getName());
        final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
        EventBus eb = vertx.eventBus();
        defineConsumers(mongo, eb);
    }

    private void defineConsumers(final MongoClient mongo, final EventBus eb) {
        eb.consumer(MONGO_FIND_USER, message -> {
            System.out.println("[Worker] find user name" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("username", message.body());
            mongo.findOne("users", query, null, getEventBusUserAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_DELETE_USER, message -> {
            System.out.println("[Worker] delete user name" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("username", message.body());
            mongo.removeOne("users", new JsonObject().put("_id", message.body()), deleteUserAsyncResultHandler(eb));
        });
    }

    private Handler<AsyncResult<JsonObject>> getEventBusUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            if (lookup.failed()) {
                eb.publish("find.user.client", "user does not exist");
                return;
            }
            final JsonObject json = lookup.result();
            eb.publish(FIND_USER_CLIENT, json.encode());
        };
    }
    
    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("status", 1);
            eb.publish(DELETE_USER_CLIENT, jsonObject.encode());
        };
    }
}