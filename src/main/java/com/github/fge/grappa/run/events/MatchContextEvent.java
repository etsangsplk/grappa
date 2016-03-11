/*
 * Copyright (C) 2015 Francis Galiegue <fgaliegue@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.run.events;

import com.github.fge.grappa.run.ParseRunnerListener;
import com.github.fge.grappa.run.context.MatcherContext;

/**
 * Base class for parsing events
 *
 * @param <V> parameter type of the parser's stack values
 *
 * @see ParseRunnerListener
 */
public abstract class MatchContextEvent<V>
{
    protected final MatcherContext<V> context;

    protected MatchContextEvent(final MatcherContext<V> context)
    {
        this.context = context;
    }

    public final MatcherContext<V> getContext()
    {
        return context;
    }
}
