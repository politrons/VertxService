package org.politrons.auth.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.politrons.auth.CreateUserHandler;
import org.politrons.auth.MongoAuth;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pabloperezgarcia on 27/9/15.
 */
public class CreateUserHandlerImpl implements CreateUserHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateUserHandler.class);

    private final MongoAuth authProvider;

    private String usernameParam;
    private String passwordParam;
    private String roleParam;
    private String returnURLParam;
    private String directLoggedInOKURL;


    public CreateUserHandlerImpl(MongoAuth authProvider, String usernameParam, String passwordParam,
                                 String roleParam, String returnURLParam, String directLoggedInOKURL) {
        this.authProvider = authProvider;
        this.usernameParam = usernameParam;
        this.passwordParam = passwordParam;
        this.roleParam = roleParam;
        this.returnURLParam = returnURLParam;
        this.directLoggedInOKURL = directLoggedInOKURL;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
            return;
        }
        if (!req.isExpectMultipart()) {
            throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
        }
        MultiMap params = req.formAttributes();
        String username = params.get(usernameParam);
        String password = params.get(passwordParam);
        String role = params.get(roleParam);
        if (username == null || password == null) {
            log.warn("No username or password provided in form - did you forget to include a BodyHandler?");
            context.fail(400);
            return;
        }
        authProvider.insertUser(username, password, Arrays.asList(role), getUserRights(role),
                getInsertUserAsyncResultHandler(context, req));
    }

    private Handler<AsyncResult<String>> getInsertUserAsyncResultHandler(final RoutingContext context, final HttpServerRequest req) {
        return res -> {
            if (res.succeeded()) {
                log.info("new user id " + res.result());
                doRedirect(req.response(), directLoggedInOKURL);
            } else {
                context.fail(403);  // Failed creation
            }
        };
    }

    private List<String> getUserRights(String role) {
        switch (role) {
            case "admin": {
                return Arrays.asList("write", "read", "delete");
            }
            case "user": {
                return Arrays.asList("write", "read");
            }
            case "guest": {
                return Arrays.asList("read");
            }
        }
        return null;
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader("location", url).setStatusCode(302).end();
    }

}
