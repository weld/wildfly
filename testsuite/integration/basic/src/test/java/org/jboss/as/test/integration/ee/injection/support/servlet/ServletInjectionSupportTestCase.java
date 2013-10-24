/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.test.integration.ee.injection.support.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ee.injection.support.InjectionSupportTestCase;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunAsClient
@RunWith(Arquillian.class)
public class ServletInjectionSupportTestCase extends InjectionSupportTestCase {

    @Deployment
    public static WebArchive createTestArchive() {
        return createTestArchiveBase().addClasses(TestServlet.class);
    }

    @Test
    public void testFieldInjection() throws IOException, ExecutionException, TimeoutException {
        assertNotNull(doGetRequest("/TestServlet?mode=field"));
    }

    @Test
    public void testSetterInjection() throws IOException, ExecutionException, TimeoutException {
        assertNotNull(doGetRequest("/TestServlet?mode=method"));
    }

    @Test
    public void testInterceptor() throws IOException, ExecutionException, TimeoutException {
        // Servlet.service(ServletRequest, ServletResponse) must be intercepted
        assertEquals("0", doGetRequest("/TestServlet?mode=interceptorReset"));
        assertEquals("1", doGetRequest("/TestServlet?mode=interceptorVerify"));
    }

}
