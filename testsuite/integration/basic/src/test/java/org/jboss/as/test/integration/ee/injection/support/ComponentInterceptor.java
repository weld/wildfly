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
package org.jboss.as.test.integration.ee.injection.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Priority(Interceptor.Priority.APPLICATION + 10)
@Interceptor
@ComponentInterceptorBinding
public class ComponentInterceptor {

    private static final List<Interception> interceptions = Collections.synchronizedList(new ArrayList<Interception>());

    @AroundInvoke
    public Object alwaysReturnThis(InvocationContext ctx) throws Exception {
        interceptions.add(new Interception(ctx.getMethod().getName(), ctx.getTarget().getClass().getName()));
        return ctx.proceed();
    }

    public static void resetInterceptions() {
        interceptions.clear();
    }

    public static List<Interception> getInterceptions() {
        return interceptions;
    }

    public static class Interception {

        private final String methodName;

        private final String targetClassName;

        public Interception(String methodName, String targetClassName) {
            super();
            this.methodName = methodName;
            this.targetClassName = targetClassName;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getTargetClassName() {
            return targetClassName;
        }

    }

}
