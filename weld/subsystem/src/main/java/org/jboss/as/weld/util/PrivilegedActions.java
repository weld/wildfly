/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Inc., and individual contributors as indicated
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
package org.jboss.as.weld.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

/**
 * Utils for working with {@link PrivilegedAction}.
 *
 * @author Jozef Hartinger
 *
 */
public class PrivilegedActions {

    private PrivilegedActions() {
    }

    /**
     * Removes the ambiguity between {@link PrivilegedAction} and {@link PrivilegedExceptionAction} that normally occurs when a lambda is passed to
     * {@link AccessController#doPrivileged()}
     */
    public static <T> PrivilegedAction<T> of(PrivilegedAction<T> action) {
        return action;
    }

    public static PrivilegedAction<Void> of(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }
}
