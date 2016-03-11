/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

package com.github.fge.grappa.matchers;

import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.run.context.MatcherContext;

import static com.github.fge.grappa.support.Chars.escape;

/**
 * A {@link Matcher} matching a single character case-independently.
 *
 * <p>This is the matcher used by {@link BaseParser#ignoreCase(char)
 * ignoreCase()} (the single character version).</p>
 */
public final class CharIgnoreCaseMatcher
    extends AbstractMatcher
{
    private final char lowerBound;
    private final char upperBound;

    public CharIgnoreCaseMatcher(final char character)
    {
        super('\'' + escape(Character.toLowerCase(character))
            + '/' + escape(Character.toUpperCase(character)) + '\''
        );
        lowerBound = Character.toLowerCase(character);
        upperBound = Character.toUpperCase(character);
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    @Override
    public <V> boolean match(final MatcherContext<V> context)
    {
        final char c = context.getCurrentChar();
        if (c != lowerBound && c != upperBound)
            return false;
        context.advanceIndex(1);
        return true;
    }
}
