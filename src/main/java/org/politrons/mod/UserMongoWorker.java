package org.politrons.mod;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

/**
 * Created by pabloperezgarcia on 24/9/15.
 */


public class UserMongoWorker extends AbstractVerticle {


    public static final String MONGO_FIND_USER = "mongo.find.user";
    public static final String MONGO_DELETE_USER = "mongo.delete.user";
    public static final String FIND_USER_CLIENT = "find.user.client";
    public static final String DELETE_USER_CLIENT = "delete.user.client";
    public static final String ADD_USER_CLIENT = "add.user.client";
    public static final String MONGO_ADD_USER = "mongo.add.user";
    public static final String UPDATE_USER_CLIENT = "update.user.client";
    public static final String FIND_USERS_CLIENT = "find.users.client";
    public static final String MONGO_FIND_USERS = "mongo.find.users";
    public static final String STATUS = "status";
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;
    public static final String MONGO_UPDATE_USER = "mongo.update.user";

    @Override
    public void start() throws Exception {
        System.out.println("[Worker] Starting in " + Thread.currentThread().getName());
        final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
        EventBus eb = vertx.eventBus();
        defineConsumers(mongo, eb);
    }

    /**
     * We define all our mongo crud consumers to be consume for our clients
     *
     * @param mongo
     * @param eb
     */
    private void defineConsumers(final MongoClient mongo, final EventBus eb) {
        eb.consumer(MONGO_ADD_USER, message -> {
            System.out.println("[Worker] add user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            mongo.insert("users", user, insertUserAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_UPDATE_USER, message -> {
            System.out.println("[Worker] update user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            JsonObject query = new JsonObject().put("_id", user.getValue("_id"));
            JsonObject update = new JsonObject().put("$set", user);
            mongo.update("users", query, update, updateUserAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_FIND_USERS, message -> {
            System.out.println("[Worker] find user name" + Thread.currentThread().getName());
            mongo.find("users", new JsonObject(), getUsersAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_FIND_USER, message -> {
            System.out.println("[Worker] find users name" + Thread.currentThread().getName());
            JsonObject jsonObject = (JsonObject) message.body();
            JsonObject query = new JsonObject();
            query.put(jsonObject.getString("searchBy"), jsonObject.getString("inputValue"));
            mongo.findOne("users", query, null, getEventBusUserAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_DELETE_USER, message -> {
            System.out.println("[Worker] delete user name" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("username", message.body());
            mongo.removeOne("users", new JsonObject().put("_id", message.body()), deleteUserAsyncResultHandler(eb));
        });
    }

    private Handler<AsyncResult<List<JsonObject>>> getUsersAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            eb.publish(FIND_USERS_CLIENT, json.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the mongo update transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<Void>> updateUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(MONGO_FIND_USERS, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the mongo insert transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<String>> insertUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(ADD_USER_CLIENT, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the mongo delete transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(DELETE_USER_CLIENT, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the mongo find transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<JsonObject>> getEventBusUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject json = lookup.result();
            if (json == null) {
                json = new JsonObject();
                json.put(STATUS, ERROR);
                eb.publish(FIND_USER_CLIENT, json.encode());
                return;
            }
            json.put(STATUS, SUCCESS);
            eb.publish(FIND_USER_CLIENT, json.encode());
        };
    }

}