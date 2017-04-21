/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.weld.services.bootstrap;

import static java.security.AccessController.doPrivileged;
import static org.jboss.as.weld.util.PrivilegedActions.of;

import java.security.Principal;
import java.security.PrivilegedAction;

import org.jboss.as.security.service.SimpleSecurityManager;
import org.jboss.as.weld.ServiceNames;
import org.jboss.as.weld.logging.WeldLogger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.weld.security.spi.SecurityServices;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.manager.WildFlySecurityManager;

public class WeldSecurityServices implements Service<WeldSecurityServices>, SecurityServices {

    public static final ServiceName SERVICE_NAME = ServiceNames.WELD_SECURITY_SERVICES_SERVICE_NAME;

    private final InjectedValue<SimpleSecurityManager> securityManagerValue = new InjectedValue<SimpleSecurityManager>();

    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public WeldSecurityServices getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public Principal getPrincipal() {
        SecurityDomain elytronDomain = SecurityDomain.getCurrent();
        if(elytronDomain != null) {
            return elytronDomain.getCurrentSecurityIdentity().getPrincipal();
        }

        final SimpleSecurityManager securityManager = securityManagerValue.getOptionalValue();
        if (securityManager == null)
            throw WeldLogger.ROOT_LOGGER.securityNotEnabled();
        return securityManager.getCallerPrincipal();
    }

    @Override
    public void cleanup() {
    }

    public InjectedValue<SimpleSecurityManager> getSecurityManagerValue() {
        return securityManagerValue;
    }

    @Override
    public org.jboss.weld.security.spi.SecurityContext getSecurityContext() {
        SecurityContext ctx;
        if (WildFlySecurityManager.isChecking()) {
            ctx = doPrivileged(of(SecurityContextAssociation::getSecurityContext));
        } else {
            ctx = SecurityContextAssociation.getSecurityContext();
        }
        return new WeldSecurityContext(ctx);
    }

    static class WeldSecurityContext implements org.jboss.weld.security.spi.SecurityContext, PrivilegedAction<Void> {

        private final SecurityContext ctx;

        WeldSecurityContext(SecurityContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void associate() {
            if (WildFlySecurityManager.isChecking()) {
                doPrivileged(of(this::run));
            } else {
                run();
            }
        }

        @Override
        public void dissociate() {
            if (WildFlySecurityManager.isChecking()) {
                doPrivileged(of(SecurityContextAssociation::clearSecurityContext));
            } else {
                SecurityContextAssociation.clearSecurityContext();
            }
        }

        @Override
        public void close() {
        }

        @Override
        public Void run() {
            SecurityContextAssociation.setSecurityContext(ctx);
            return null;
        }
    }
}
