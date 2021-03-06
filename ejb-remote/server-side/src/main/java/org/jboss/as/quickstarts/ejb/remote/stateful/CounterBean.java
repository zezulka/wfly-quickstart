/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.as.quickstarts.ejb.remote.stateful;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

@Stateful
@Remote(RemoteCounter.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class CounterBean implements RemoteCounter {

    private int count = 0;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void increment() {
        Span s = GlobalTracer.get().buildSpan("CounterBean/increment").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            dbSave(new DummyEntity(this.count++));
        } finally {
            s.finish();
        }
    }

    @Override
    public void decrement() {
        Span s = GlobalTracer.get().buildSpan("CounterBean/decrement").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            dbSave(new DummyEntity(this.count--));
        } finally {
            s.finish();
        }
    }

    @Override
    public int getCount() {
        Span s = GlobalTracer.get().buildSpan("CounterBean/getCount").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            return this.count;
        } finally {
            s.finish();
        }
    }

    private void dbSave(DummyEntity quickstartEntity) {
        if (quickstartEntity.isTransient()) {
            em.persist(quickstartEntity);
        } else {
            em.merge(quickstartEntity);
        }
    }
}
