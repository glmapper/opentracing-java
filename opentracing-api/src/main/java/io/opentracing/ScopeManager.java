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

import io.opentracing.Tracer.SpanBuilder;

/**
 *
 * 该接口提供了将给定的span变为 活动的 span的功能以及获取当前 活动的 span/scope
 *
 * 在多线程环境下ScopeManager管理着各个线程的Scope，而每个线程中的Scope管理着该线程中的Span。
 * 这样当某个线程需要获取其线程中当前 活动的 span时，可以通过ScopeManager找到对应该线程的Scope，并从Scope中取出该线程 活动的 span
 *
 * 具体实现 ：ThreadLocalScopeManager 等
 *
 *
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances via
 * {@link ScopeManager#activate(Span, boolean)} and access to an active {@link Span}/{@link Scope}
 * via {@link ScopeManager#active()}.
 *
 * @see Scope
 * @see Tracer#scopeManager()
 */
public interface ScopeManager {

    /**
     * Make a {@link Span} instance active.
     *
     * @param span the {@link Span} that should become the {@link #active()}
     * @param finishSpanOnClose whether span should automatically be finished when {@link Scope#close()} is called
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     *
     * span注册到scopeManager是为了用于建立span间的如parent-child之类的关系，
     * 当方法嵌套调用 并去我们两个方法都想要进行追踪时，
     * 将span注册到scopeManager中是必须的，否则就只能将span作为方法的参数进行传递。
     *
     */
    Scope activate(Span span, boolean finishSpanOnClose);

    /**
     * Return the currently active {@link Scope} which can be used to access the currently active
     * {@link Scope#span()}.
     *
     * 返回当前 active 状态的{@link Scope} ，能够被用于访问当前 active 状态的 {@link Scope#span()}.
     *
     * <p>
     * If there is an {@link Scope non-null scope}, its wrapped {@link Span} becomes an implicit(隐式的) parent
     * (as {@link References#CHILD_OF} reference) of any
     * newly-created(新创建的) {@link Span} at {@link Tracer.SpanBuilder#startActive(boolean)} or {@link SpanBuilder#start()}
     * time rather than at {@link Tracer#buildSpan(String)} time.
     *
     * @return the {@link Scope active scope}, or null if none could be found.
     */
    Scope active();
}
