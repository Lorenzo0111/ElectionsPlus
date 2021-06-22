/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (c) Lorenzo0111
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.lorenzo0111.elections.cache;

import me.lorenzo0111.elections.api.objects.Cache;

import java.util.HashMap;
import java.util.Map;

public class MapCache<String,V> implements Cache<String,V> {
    private final Map<String,V> map = new HashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void reset() {
        map.clear();
    }

    @Override
    public void add(String key, V value) {
        map.put(key,value);
    }

    @Override
    public boolean remove(String key, V value) {
        return map.remove(key,value);
    }

    @Override
    public V remove(String key) {
        return map.remove(key);
    }

    @Override
    public V get(String key) {
        return map.get(key);
    }

    @Override
    public Map<String, V> map() {
        return map;
    }

}
