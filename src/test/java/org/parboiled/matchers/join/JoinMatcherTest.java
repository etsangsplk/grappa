package org.parboiled.matchers.join;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.matchers.EmptyMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.OptionalMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.parboiled.util.MatcherAssert.assertMatcher;
import static org.testng.Assert.fail;

public final class JoinMatcherTest<V>
{
    /*
     * We need this one for the "special case" generation
     */
    private static final Matcher SPECIAL = mock(Matcher.class);

    private BaseParser<V> parser;
    private Matcher joined;
    private Matcher joining;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void initRules()
    {
        parser = (BaseParser<V>) mock(BaseParser.class);
        when(parser.toRule(any()))
            .thenAnswer(new Answer<Object>()
            {
                @Override
                public Object answer(final InvocationOnMock invocation)
                {
                    return invocation.getArguments()[0];
                }
            });
        joined = mock(Matcher.class);
        joining = mock(Matcher.class);
    }

    @Test
    public void rangeMustNotBeNull()
    {
        final String expected = "range must not be null";
        try {
            new JoinMatcherBootstrap<V, BaseParser<V>>(parser, joined)
                .using(joining).range(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            final String actual = e.getMessage();
            assertThat(actual).overridingErrorMessage(
                "Unexpected exception message!\nExpected: %s\nActual  : %s\n",
                expected, actual
            ).isEqualTo(expected);
        }
    }

    @Test
    public void rangeMustNotBeEmptyAfterIntersection()
    {
        final Range<Integer> range = Range.lessThan(0);
        final String expected = "illegal range " + range
            + ": should not be empty after intersection with "
            + Range.atLeast(0);
        try {
            new JoinMatcherBootstrap<V, BaseParser<V>>(parser, joined)
                .using(joining).range(range);
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            final String actual = e.getMessage();
            assertThat(actual).overridingErrorMessage(
                "Unexpected exception message!\nExpected: %s\nActual  : %s\n",
                expected, actual
            ).isEqualTo(expected);
        }
    }

    @DataProvider
    public Iterator<Object[]> getSpecialRanges()
    {
        final List<Object[]> list = Lists.newArrayList();

        Range<Integer> range;
        Matcher matcher;

        range = Range.singleton(0);
        matcher = new EmptyMatcher();
        list.add(new Object[] { range, matcher });

        range = Range.singleton(1);
        matcher = SPECIAL;
        list.add(new Object[] { range, matcher });

        range = Range.atMost(1);
        matcher = new OptionalMatcher(SPECIAL);
        list.add(new Object[] { range, matcher });

        return list.iterator();
    }

    @Test(dataProvider = "getSpecialRanges")
    public void specialRangesGenerateSpecialMatchers(final Range<Integer> range,
        final Matcher expected)
    {
        final Rule rule = JoinMatcherBootstrap.create(parser, SPECIAL)
            .using(joining).range(range);
        final Matcher actual = (Matcher) rule;

        final Class<? extends Matcher> expectedClass = expected.getClass();
        assertMatcher(actual).overridingErrorMessage(
            "Wrong class! Expected %s, got %s",
            expectedClass.getCanonicalName(),
            actual.getClass().getCanonicalName()
        ).isExactlyInstanceOf(expectedClass);

        // FIXME: hack...
        if (expectedClass == OptionalMatcher.class) {
            final Matcher actualChild = actual.getChildren().get(0);
            final Matcher expectedChild = expected.getChildren().get(0);
            assertThat(actualChild).overridingErrorMessage(
                "Child is not what is expected! Got %s, expected %s",
                actualChild, expectedChild
            ).isSameAs(expectedChild);
        }
    }
}