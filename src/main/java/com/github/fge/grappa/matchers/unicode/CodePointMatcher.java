/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
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

package com.github.fge.grappa.matchers.unicode;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.run.context.MatcherContext;

/**
 * A single Unicode code point matcher
 *
 * <p>This is the matcher used by {@link BaseParser#unicodeChar(int)
 * unicodeChar()}.</p>
 *
 * @see InputBuffer#codePointAt(int)
 */
public final class CodePointMatcher
    extends AbstractMatcher
{
    private final int codePoint;
    private final int codePointLength;

    public CodePointMatcher(final int codePoint)
    {
        super(String.format("U+%04X", codePoint));
        this.codePoint = codePoint;
        codePointLength = Character.charCount(codePoint);
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    @Override
    public <V> boolean match(final MatcherContext<V> context)
    {
        final int index = context.getCurrentIndex();
        final InputBuffer buffer = context.getInputBuffer();
        if (codePoint != buffer.codePointAt(index))
            return false;

        context.advanceIndex(codePointLength);
        return true;
    }
}
