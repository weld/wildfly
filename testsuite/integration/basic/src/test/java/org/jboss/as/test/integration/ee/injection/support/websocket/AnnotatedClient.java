package org.jboss.as.test.integration.ee.injection.support.websocket;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.jboss.as.test.integration.ee.injection.support.Alpha;
import org.jboss.as.test.integration.ee.injection.support.Bravo;
import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptorBinding;

@ClientEndpoint
public class AnnotatedClient {

    public static boolean postConstructCalled = false;

    public static boolean injectionOK = false;

    private static final BlockingDeque<String> queue = new LinkedBlockingDeque<>();

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

    @OnOpen
    @ComponentInterceptorBinding
    public void open(final Session session) throws IOException {
        session.getBasicRemote().sendText("Hello");
    }

    @OnMessage
    @Interceptors(OnMessageClientInterceptor.class)
    public void message(final String message) {
        queue.add(message);
    }

    public static String getMessage() throws InterruptedException {
        return queue.poll(5, TimeUnit.SECONDS);
    }

    public static void reset() {
        queue.clear();
        postConstructCalled = false;
    }
}
