package org.jboss.as.test.integration.ee.injection.support.websocket;

import java.net.URI;

import javax.naming.InitialContext;
import javax.websocket.server.ServerContainer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ee.injection.support.Alpha;
import org.jboss.as.test.integration.ee.injection.support.Bravo;
import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptor;
import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptorBinding;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matus Abaffy
 */
@RunWith(Arquillian.class)
public class WebSocketInjectionSupportTestCase {

    private static final String SERVER_CONTAINER_JNDI_NAME = "java:module/ServerContainer";

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap
                .create(WebArchive.class, "websocket.war")
                .addPackage(WebSocketInjectionSupportTestCase.class.getPackage())
                .addClasses(TestSuiteEnvironment.class, Alpha.class, Bravo.class, ComponentInterceptorBinding.class,
                        ComponentInterceptor.class);
    }

    // TODO merge both methods into one, or make them be run after common creation of endpoints

    @Test
    public void testWebSocketInjection() throws Exception {
        ServerContainer serverContainer = (ServerContainer) new InitialContext().lookup(SERVER_CONTAINER_JNDI_NAME);
        serverContainer.connectToServer(AnnotatedClient.class, new URI("ws", "", TestSuiteEnvironment.getServerAddress(), 8080,
                "/websocket/websocket/cruel", "", ""));
        Assert.assertTrue("Client endpoint's injection not correct.", AnnotatedClient.injectionOK);
        Assert.assertTrue("Server endpoint's injection not correct.", AnnotatedEndpoint.injectionOK);
    }

    @Test
    public void testWebSocketInterception() throws Exception {
        AnnotatedClient.reset();
        ComponentInterceptor.resetInterceptions();
        ServerContainer serverContainer = (ServerContainer) new InitialContext().lookup(SERVER_CONTAINER_JNDI_NAME);
        serverContainer.connectToServer(AnnotatedClient.class, new URI("ws", "", TestSuiteEnvironment.getServerAddress(), 8080,
                "/websocket/websocket/cruel", "", ""));
        Assert.assertTrue("PostConstruct method on client endpoint instance not called.", AnnotatedClient.postConstructCalled);
        Assert.assertTrue("PostConstruct method on server endpoint instance not called.", AnnotatedEndpoint.postConstructCalled);

        Assert.assertEquals(2, ComponentInterceptor.getInterceptions().size());
        Assert.assertEquals("open", ComponentInterceptor.getInterceptions().get(0).getMethodName());
        Assert.assertEquals("intercept", ComponentInterceptor.getInterceptions().get(1).getMethodName());
        Assert.assertEquals("Hello cruel World", AnnotatedClient.getMessage());
    }
}
