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
 *
 * Scope 接口的里面需要和 ScopeManager 对应来看，ScopeManager 解决的是 span 在线程中传递的问题。但是 ScopeManager 本身直接操作 span 又会显得有些不彻底。
 * 这个不彻底怎么理解呢。结合 SOFATracer 的实现，我的理解是：
 *
 * 1、SOFATracer中也是使用 ThreadLocal 的机制实现 span 在线程中传递的。ThreadLocal 中就是 set & get 。span 之间的父子关系以及当前 ThreadLocal 中应该存哪个 span 都需要我们自己在代码中来管理。
 *    这种方式完全 OK，但是如果对于一个标准/规范来说，如果只是定义一个这样的 ThreadLocal 完全是没有意义的。
 * 2、自己管理 ThreadLocal 中 span 的关系是一个复杂的过程，尤其是在链路较长的情况下。
 *
 * 基于上述两点，ot-api 没有采用直接在 ScopeManager 中set&get span 的操作方案。而是使用了Scope，对应的实现类是 ThreadLocalScope。好处在哪？
 * ThreadLocalScope 的设计使用了栈的思想，这个怎么理解呢？在一个线程中，每一个 span 的产生到结束，里面在嵌套 子span的产生到结束，这种嵌套关系可以很容器联想到栈的概念；
 * 这个过程很好理解，栈的操作，有进有出，一进一出就是一个 span 的生命周期。
 *
 * ThreadLocalScope 为span在线程中传递提供了新的设计思路，但是如果仅基于 span + ThreadLocal 来实现，是不可能的。
 *
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
