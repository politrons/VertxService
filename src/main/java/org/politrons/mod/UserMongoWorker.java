package org.politrons.mod;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.mongo.MongoClient;
import org.politrons.VertxRest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by pabloperezgarcia on 24/9/15.
 */

@Component
public class UserMongoWorker extends AbstractVerticle {

    public static final String MONGO_DELETE_USER_LOGIN = "mongo.delete.user.login";
    Logger logger = LoggerFactory.getLogger(UserMongoWorker.class);

    public static final String MONGO_FIND_USER = "mongo.find.user";
    public static final String MONGO_DELETE_USER = "mongo.delete.user";
    public static final String MONGO_ADD_USER = "mongo.add.user";
    public static final String MONGO_FIND_USERS = "mongo.find.users";
    public static final String MONGO_UPDATE_USER = "mongo.update.user";
    public static final String MONGO_TRACK_USER = "mongo.track.user";
    public static final String MONGO_FIND_USERS_LOGIN = "mongo.find.users.login";
    public static final String MONGO_UPDATE_USER_PAGE = "mongo.update.user.page";
    public static final String MONGO_FIND_USERS_PAGE = "mongo.find.users.page";

    public static final String STATUS = "status";
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;
    public static final String MONGO_FIND_USER_ID = "mongo.find.user.id";


    @Override
    public void start() throws Exception {
        logger.info("[Worker] Starting in " + Thread.currentThread().getName());
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
            logger.info("[Worker] add user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            mongo.insert("users", user, insertUserAsyncResultHandler(message));
        });
        eb.consumer(MONGO_UPDATE_USER, message -> {
            logger.info("[Worker] update user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            JsonObject query = new JsonObject().put("_id", user.getValue("_id"));
            JsonObject update = new JsonObject().put("$set", user);
            mongo.update("users", query, update, updateUserAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_FIND_USERS, message -> {
            logger.info("[Worker] find users name" + Thread.currentThread().getName());
            mongo.find("users", new JsonObject(), getUsersAsyncResultHandler(message));
        });
        eb.consumer(MONGO_FIND_USER, message -> {
            logger.info("[Worker] find user name" + Thread.currentThread().getName());
            JsonObject jsonObject = (JsonObject) message.body();
            JsonObject query = new JsonObject();
            query.put(jsonObject.getString("searchBy"), jsonObject.getString("inputValue"));
            mongo.findOne("users", query, null, getEventBusUserAsyncResultHandler(message));
        });
        eb.consumer(MONGO_DELETE_USER, message -> {
            logger.info("[Worker] delete user name" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("username", message.body());
            mongo.removeOne("users", new JsonObject().put("_id", message.body()), deleteUserAsyncResultHandler(message));
        });
        eb.consumer(MONGO_TRACK_USER, message -> {
            logger.info("[Worker] Update track user name" + Thread.currentThread().getName());
            JsonObject user = (JsonObject) message.body();
            JsonObject query = new JsonObject().put("_id", user.getValue("_id"));
            JsonObject update = new JsonObject().put("$set", user);
            mongo.update("users", query, update, updateUserTrackAsyncResultHandler(eb, user));
        });
        eb.consumer(MONGO_FIND_USER_ID, message -> {
            logger.info("[Worker] find users track name" + Thread.currentThread().getName());
            JsonObject jsonObject = (JsonObject) message.body();
            JsonObject query = new JsonObject();
            query.put("_id", jsonObject.getString("_id"));
            mongo.findOne("users", query, null, getUserTrackAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_FIND_USERS_LOGIN, message -> {
            logger.info("[Worker] find users name" + Thread.currentThread().getName());
            mongo.find("user_login", new JsonObject(), getUsersLoginAsyncResultHandler(message));
        });
        eb.consumer(MONGO_DELETE_USER_LOGIN, message -> {
            logger.info("[Worker] delete user name" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("username", message.body());
            mongo.removeOne("user_login", new JsonObject().put("_id", message.body()), deleteUserLoginAsyncResultHandler(eb));
        });
        eb.consumer(MONGO_UPDATE_USER_PAGE, message -> {
            logger.info("[Worker] update user page" + Thread.currentThread().getName());
            JsonObject jsonObject = (JsonObject) message.body();
            JsonObject query = new JsonObject();
            query.put("username", jsonObject.getString("username"));
            JsonObject update = new JsonObject().put("$set", new JsonObject().put("page", jsonObject.getString("page")));
            mongo.update("users", query, update, updateUserPageAsyncResultHandler(message));
        });
        eb.consumer(MONGO_FIND_USERS_PAGE, message -> {
            logger.info("[Worker] find users in page" + Thread.currentThread().getName());
            JsonObject query = new JsonObject();
            query.put("page", message.body());
            mongo.find("users", query, getUsersAsyncResultHandler(message));
        });
    }

    /**
     * Callback method to be invoked with the result of the auth users find
     * @param message
     * @return
     */
    private Handler<AsyncResult<List<JsonObject>>> getUsersAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            message.reply(json.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the users login find
     * @param message
     * @return
     */
    private Handler<AsyncResult<List<JsonObject>>> getUsersLoginAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
            message.reply(json.encode());
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
     * @param message
     * @return
     */
    private Handler<AsyncResult<String>> insertUserAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            message.reply(jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth delete transaction
     *
     * @param message
     * @return
     */
    private Handler<AsyncResult<Void>> deleteUserAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            message.reply(jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the login account delete transaction
     *
     * @param eb
     * @return
     */
    private Handler<AsyncResult<Void>> deleteUserLoginAsyncResultHandler(final EventBus eb) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            eb.publish(MONGO_FIND_USERS_LOGIN, null);
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
     * Callback method to be invoked with the result of the auth insert transaction
     *
     * @param message
     * @return
     */
    private Handler<AsyncResult<Void>> updateUserPageAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(STATUS, SUCCESS);
            message.reply(jsonObject.encode());
        };
    }

    /**
     * Callback method to be invoked with the result of the auth find transaction
     *
     * @param message
     * @return
     */
    private Handler<AsyncResult<JsonObject>> getEventBusUserAsyncResultHandler(final Message<Object> message) {
        return lookup -> {
            SharedData sd = vertx.sharedData();
            LocalMap<String, String> stringMap = sd.getLocalMap("myFirstMap");
            logger.info(stringMap.get("myFirstValue"));
            JsonObject json = lookup.result();
            if (json == null) {
                json = new JsonObject();
                json.put(STATUS, ERROR);
                message.reply(json.encode());
                return;
            }
            json.put(STATUS, SUCCESS);
            message.reply(json.encode());
        };
    }

}