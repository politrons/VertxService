/**
 * Created by pabloperezgarcia on 27/9/15.
 */


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.politrons.VertxRest;

import static org.junit.Assert.assertEquals;

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
    public void restGetAll() {
        createHttpClient().get("/login.html", resp -> {
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


}