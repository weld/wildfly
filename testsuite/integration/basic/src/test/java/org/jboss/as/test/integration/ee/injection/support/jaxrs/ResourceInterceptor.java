package org.jboss.as.test.integration.ee.injection.support.jaxrs;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptorBinding;

@Interceptor
@Priority(900)
@ComponentInterceptorBinding
public class ResourceInterceptor {

    @AroundInvoke
    public Object intercept(final InvocationContext invocationContext) throws Exception {
        if (invocationContext.getMethod().getName().equals("getMessage")) {
            return invocationContext.proceed() + " World";
        }
        return invocationContext.proceed();
    }

}