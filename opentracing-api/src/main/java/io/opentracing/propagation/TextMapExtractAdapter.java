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
package io.opentracing.propagation;

import io.opentracing.Tracer;
import java.util.Iterator;

import java.util.Map;

/**
 * A TextMap carrier for use with Tracer.extract() ONLY (it has no mutating(重载、变异) methods).
 *
 * Note that the TextMap interface can be made to wrap around arbitrary(任意) data types (not just Map&lt;String, String&gt;
 * as illustrated here(这里说明)).
 *
 * 只能提取，不能注入
 *
 * @see Tracer#extract(Format, Object)
 */
public final class TextMapExtractAdapter implements TextMap {
    private final Map<String,String> map;

    public TextMapExtractAdapter(final Map<String,String> map) {
        this.map = map;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TextMapExtractAdapter should only be used with Tracer.extract()");
    }
}
