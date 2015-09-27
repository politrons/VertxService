package org.politrons.auth;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.politrons.auth.impl.CreateUserHandlerImpl;

/**
 * Created by pabloperezgarcia on 27/9/15.
 */

public interface CreateUserHandler extends Handler<RoutingContext> {

    /**
     * The default value of the form attribute which will contain the username
     */
    String DEFAULT_USERNAME_PARAM = "username";

    /**
     * The default value of the form attribute which will contain the password
     */
    String DEFAULT_PASSWORD_PARAM = "password";

    /**
     * The default value of the form attribute which will contain the role
     */
    String DEFAULT_ROLE_PARAM = "role";

    /**
     * The default value of the form attribute which will contain the return url
     */
    String DEFAULT_RETURN_URL_PARAM = "return_url";

    /**
     * The default value of the form attribute which will contain the return url
     */
    String DEFAULT_DIRECT_CREATION_USER_OK_URL = "/login.html";

    /**
     * Create a handler
     *
     * @param authProvider the auth service to use
     * @return the handler
     */
    static CreateUserHandler create(MongoAuth authProvider) {
        return new CreateUserHandlerImpl(authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM,
                DEFAULT_ROLE_PARAM, DEFAULT_RETURN_URL_PARAM, DEFAULT_DIRECT_CREATION_USER_OK_URL);
    }
}


