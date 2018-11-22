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
package io.opentracing;

import java.io.Closeable;

/**
 * A {@link Scope} formalizes(正式、规范) the activation and deactivation of a {@link Span}, usually from a CPU standpoint(立场；观点).
 *
 * {@link Scope} 通常从CPU的角度来规范{@link Span}的激活和停用。
 *
 * <p>
 * Many times a {@link Span} will be extant(显著的) (in that {@link Span#finish()} has not been called) despite(尽管) being in a
 * non-runnable state from a CPU/scheduler standpoint. For instance, a {@link Span} representing(表示) the client side of an
 * RPC will be unfinished but blocked on IO while the RPC is still outstanding(出色的，未解决的,显著地). A {@link Scope} defines when a given
 * {@link Span} <em>is</em> scheduled and on the path.
 */
public interface Scope extends Closeable {
    /**
     * Mark the end of the active period for the current thread and {@link Scope},
     * updating the {@link ScopeManager#active()} in the process.
     *
     * 标记 当前线程和Scope的活动周期结束，更新进程中的{@link ScopeManager＃active（）}。
     *
     * <p>
     * NOTE: Calling {@link #close} more than once on a single {@link Scope} instance leads to undefined
     * behavior.
     *
     * 在单个{@link Scope}实例上多次调用{@link #close}会导致未定义的行为。
     *
     */
    @Override
    void close();

    /**
     * @return the {@link Span} that's been scoped by this {@link Scope}
     *
     * 由{@link Scope}限定的{@link Span}
     */
    Span span();
}
