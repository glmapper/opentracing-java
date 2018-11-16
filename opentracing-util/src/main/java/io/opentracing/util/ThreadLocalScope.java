/*
 * Copyright 2016-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.util;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * {@link ThreadLocalScope} is a simple {@link Scope} implementation that relies on Java's
 * thread-local storage primitive.
 *
 * ThreadLocalScope管理着单个线程中的所有的span
 *
 * 当前线程中的 ThreadLocalScope 管理着当前线程所有曾被激活还未释放的span
 *
 * Span 的构造完成后，必须要注册到ScopeManager中激活才能为之后的追踪构建正确的span之间的如parnet-child之类的关系
 *
 * @see ScopeManager
 */
public class ThreadLocalScope implements Scope {
    private final ThreadLocalScopeManager scopeManager;
    private final Span wrapped;
    private final boolean finishOnClose;
    private final ThreadLocalScope toRestore;

    /**
     * @param scopeManager  当前 ThreadLocalScopeManager 对象
     * @param wrapped       当前 span
     * @param finishOnClose 标注当 scope 结束时，span 是否应该被 finish 掉
     */
    ThreadLocalScope(ThreadLocalScopeManager scopeManager, Span wrapped, boolean finishOnClose) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.finishOnClose = finishOnClose;
        // 将之前活动的scope作为当前scope的属性toRestore来存储
        this.toRestore = scopeManager.tlsScope.get();
        // 将当前scope设置到scopeManager中作为当前线程最新的scope
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (scopeManager.tlsScope.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }
        // finishOnClose 作用是 当scope close时，要不要同时finish span
        if (finishOnClose) {
            wrapped.finish();
        }

        scopeManager.tlsScope.set(toRestore);
    }

    @Override
    public Span span() {
        return wrapped;
    }
}
