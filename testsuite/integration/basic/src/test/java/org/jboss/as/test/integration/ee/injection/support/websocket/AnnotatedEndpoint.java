package org.jboss.as.test.integration.ee.injection.support.websocket;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.jboss.as.test.integration.ee.injection.support.Alpha;
import org.jboss.as.test.integration.ee.injection.support.Bravo;
import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptorBinding;

@ServerEndpoint("/websocket/{name}")
public class AnnotatedEndpoint {

    public static boolean postConstructCalled = false;

    public static boolean injectionOK = false;

    @Inject
    private Alpha alpha;

    @Inject
    public void setBravo(Bravo bravo) {
        injectionOK = (alpha != null) & (bravo != null);
    }

    @PostConstruct
    private void init() {
        postConstructCalled = true;
    }

    @OnMessage
    @ComponentInterceptorBinding
    public String message(String message, @PathParam("name") String name) {
        return message + " " + name;
    }

}
