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
 * A simple {@link ScopeManager} implementation built on top of Java's thread-local storage primitive.
 *
 * 使用ThreadLocal<ThreadLocalScope>来存储不同线程的scope对象，在多线程环境下可以通过获取到当前线程的scope来获取当前线程的活动的 span。
 *
 * @see ThreadLocalScope
 */
public class ThreadLocalScopeManager implements ScopeManager {
    final ThreadLocal<ThreadLocalScope> tlsScope = new ThreadLocal<ThreadLocalScope>();

    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        // 调用 ThreadLocalScope 的构造方法，将传入的span激活为当前活动的 span
        return new ThreadLocalScope(this, span, finishOnClose);
    }

    @Override
    public Scope active() {
        return tlsScope.get();
    }
}
