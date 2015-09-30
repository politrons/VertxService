/**
 * Created by pabloperezgarcia on 27/9/15.
 */


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.politrons.mod.UserMongoWorker;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class UserMongoWorkerTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(UserMongoWorker.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testDeploying(TestContext context){
        Async async = context.async();
        vertx.deployVerticle(new UserMongoWorker(), ar -> {
            if (ar.succeeded()) {
                async.complete();
            } else {
                context.fail(ar.cause());
            }
        });
    }

    @Test
    public void testFindUsers(TestContext context) {
        final Async async = context.async();
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("searchBy", "username");
        jsonObject.put("inputValue", "pol");
        vertx.eventBus().send("mongo.find.user", jsonObject, msg -> {
            JsonObject result = new JsonObject((String) msg.result().body());
            assertThat("pol", is(result.getString("username")));
            async.complete();
        });
    }

}