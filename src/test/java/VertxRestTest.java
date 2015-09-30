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
import org.politrons.VertxRest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(VertxUnitRunner.class)
public class VertxRestTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(VertxRest.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testDeploying(TestContext context){
        Async async = context.async();
        vertx.deployVerticle(new VertxRest(), ar -> {
            if (ar.succeeded()) {
                async.complete();
                assertTrue(true);
            } else {
                context.fail(ar.cause());
                assertTrue(false);
            }
        });
    }

    @Test
    public void restGetAll() {
        createHttpClient().get("/login.html", resp -> {
            System.out.println("Request response:" + resp.statusCode());
            assertEquals(200, resp.statusCode());
            assertTrue(true);
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


}