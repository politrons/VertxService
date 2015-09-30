/**
 * Created by pabloperezgarcia on 27/9/15.
 */


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.politrons.mod.UserMongoWorker;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class VertxTest {

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
    public void restGetAll() {
        createHttpClient().get("/users",  resp -> {
            System.out.println("Request response:" + resp.statusCode());
            assertEquals(200, resp.statusCode());
//            resp.bodyHandler((Buffer data) -> {
//                System.out.println(data);
//            });
        }).end();
    }

    private HttpClient createHttpClient() {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultHost("localhost");
        httpClientOptions.setDefaultPort(8080);
        return vertx.createHttpClient(httpClientOptions);
    }


//    @Test
    public void testMyApplication(TestContext context) {


//        TestSuite suite = TestSuite.create("the_test_suite");
//
//        suite.test("my_test_case", context -> {
//
//            Async async1 = context.async();
//            HttpClient client = vertx.createHttpClient();
//            HttpClientRequest req = client.get(8080, "localhost", "/");
//            req.exceptionHandler(err -> context.fail(err.getMessage()));
//            req.handler(resp -> {
//                context.assertEquals(200, resp.statusCode());
//                async1.complete();
//            });
//            req.end();
//
//            Async async2 = context.async();
//            vertx.eventBus().consumer("the-address", msg -> {
//                async2.complete();
//            });
//        });



        final Async async = context.async();

        vertx.createHttpClient().getNow(8080, "localhost", "auth.find.user",
                response -> {
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("Hello"));
                        async.complete();
                    });
                });

        vertx.eventBus().consumer("auth.find.user", msg -> {
            System.out.println(msg);
            async.complete();
        });
    }
}