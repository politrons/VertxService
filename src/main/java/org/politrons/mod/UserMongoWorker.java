package org.politrons.mod;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.politrons.VertxRest;

import java.util.List;

/**
 * Created by pabloperezgarcia on 24/9/15.
 */


public class UserMongoWorker extends AbstractVerticle {


    public static final String MONGO_FIND_USER = "auth.find.user";
    public static final String MONGO_DELETE_USER = "auth.delete.user";
    public static final String MONGO_ADD_USER = "auth.add.user";
    public static final String MONGO_FIND_USERS = "auth.find.users";
    public static final String MONGO_UPDATE_USER = "auth.update.user";
    public static final String MONGO_TRACK_USER = "auth.track.user";
    public static final String STATUS = "status";
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;
    public static final String MONGO_FIND_USER_ID = "auth.find.user.id";


    @Override
    public void start() throws Exception {
        System.out.println("[Worker] Starting in " + Thread.currentThread().getName());
        final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
        EventBus eb = vertx.eventBus();
        defineConsumers(mongo, eb);
    }

    /**
     * We define all our auth crud consumers to be consume for our clients
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
            System.out.println("[Worker] find users name" + Thread.currentThread().getName());
            mongo.find("users", new JsonObject(), getUsersAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_FIND_USER, message -> {
            System.out.println("[Worker] find user name" + Thread.currentThread().getName());
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
        eb.consumer(MONGO_TRACK_USER, message -> {
            System.out.println("[Worker] Update track user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            JsonObject query = new JsonObject().put("_id", user.getValue("_id"));
            JsonObject update = new JsonObject().put("$set", user);
            mongo.update("users", query, update, updateUserTrackAsyncResultHandler(eb, user));
        });
        eb.consumer(MONGO_FIND_USER_ID, message -> {
            System.out.println("[Worker] find users track name" + Thread.currentThread().getName());
            JsonObject jsonObject = (JsonObject) message.body();
            JsonObject query = new JsonObject();
            query.put("_id", jsonObject.getString("_id"));
            mongo.findOne("users", query, null, getUserTrackAsyncResultHandler(eb));
        });


    }

    /**
     * Callback method to be invoked with the result of the auth users find
     * @param eb
     * @return
     */
    private Handler<AsyncResult<List<JsonObject>>> getUsersAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            eb.publish(VertxRest.FIND_USERS_CLIENT, json.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth update transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<Void>> updateUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(VertxRest.UPDATE_USER_CLIENT, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth insert transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<String>> insertUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(VertxRest.ADD_USER_CLIENT, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth delete transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(VertxRest.DELETE_USER_CLIENT, jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth update and we redirect to publish the update value
     *
     * @param eb
     * @param user
     * @return
     */
    private Handler<AsyncResult<Void>> updateUserTrackAsyncResultHandler(final EventBus eb, final JsonObject user) {
        return lookup -> {
            eb.publish(MONGO_FIND_USER_ID, user);
        };
    }

    private Handler<AsyncResult<JsonObject>> getUserTrackAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject json = lookup.result();
            if (json == null) {
                json = new JsonObject();
                json.put(STATUS, ERROR);
                eb.publish(VertxRest.TRACK_USER_CLIENT, json.encode());
                return;
            }
            json.put(STATUS, SUCCESS);
            eb.publish(VertxRest.TRACK_USER_CLIENT, json.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth find transaction
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
                eb.publish(VertxRest.FIND_USER_CLIENT, json.encode());
                return;
            }
            json.put(STATUS, SUCCESS);
            eb.publish(VertxRest.FIND_USER_CLIENT, json.encode());
        };
    }

}