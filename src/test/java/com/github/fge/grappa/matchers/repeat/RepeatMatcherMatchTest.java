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

package com.github.fge.grappa.matchers.repeat;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.parsers.EventBusParser;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.util.MatcherContextBuilder;
import com.github.fge.grappa.util.SimpleMatchHandler;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class RepeatMatcherMatchTest
{
    private static final String INPUT1 = Strings.repeat("ab", 5) + 'c';

    static class TestJoinParser
        extends EventBusParser<Object>
    {
        protected final RepeatMatcherBuilder<Object> builder = repeat("ab");
    }

    private final TestJoinParser parser
        = Grappa.createParser(TestJoinParser.class);

    private final MatcherContextBuilder builder = new MatcherContextBuilder();

    @DataProvider
    public Iterator<Object[]> getInput1MatchData()
    {
        final List<Object[]> list = Lists.newArrayList();

        Range<Integer> range;
        int index;
        boolean match;

        range = Range.singleton(3);
        index = 6;
        match = true;
        list.add(new Object[] { range, index, match });

        range = Range.singleton(6);
        index = 10;
        match = false;
        list.add(new Object[] { range, index, match });

        range = Range.atMost(4);
        index = 8;
        match = true;
        list.add(new Object[] { range, index, match });

        range = Range.atLeast(2);
        index = 10;
        match = true;
        list.add(new Object[] { range, index, match });

        range = Range.closed(2, 9);
        index = 10;
        match = true;
        list.add(new Object[] { range, index, match });

        range = Range.closed(2, 4);
        index = 8;
        match = true;
        list.add(new Object[] { range, index, match });

        return list.iterator();
    }

    @Test(dataProvider = "getInput1MatchData")
    public void repeatMatcherTest(final Range<Integer> range,
        final int index, final boolean match)
    {
        final Matcher matcher = (Matcher) parser.builder.range(range);
        final MatcherContext<Object> context = builder.withInput(INPUT1)
            .withMatcher(matcher).build();

        final boolean actualMatch
            = SimpleMatchHandler.INSTANCE.match(context);

        assertThat(actualMatch).as("match/no match is correct")
            .isEqualTo(match);
        assertThat(context.getCurrentIndex()).as("index is correct after match")
            .isEqualTo(index);
    }
}
