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

package org.parboiled.parserunners;

import com.github.parboiled1.grappa.annotations.Dangerous;
import com.github.parboiled1.grappa.annotations.DoNotUse;
import com.github.parboiled1.grappa.buffers.InputBuffer;
import com.github.parboiled1.grappa.stack.ValueStack;
import org.parboiled.errors.ParseError;
import org.parboiled.support.ParsingResult;

import java.nio.CharBuffer;
import java.util.List;

/**
 * A ParseRunner performs the actual parsing run of a given parser rule on a
 * given input text.
 *
 * <p>Note: if you want to use a parser on a {@link String} input, use the
 * {@link #run(CharSequence)} method, since String implements
 * {@link CharSequence} (and so does {@link CharBuffer}; see also <a
 * href="https://github.com/fge/largetext">for large files</a>).</p>
 */
/*
 * TODO: separation of concerns
 *
 * The problem here is that a MatcherContext, as currently defined, mixes
 * pure parsing data (input buffer, getting data from it etc) and semantics
 * (the value stack, for instance).
 *
 * And to make matters worse, a MatcherContext is the argument of a Matcher's
 * .match() method :/ See also Matcher's .getSubcontext(), which is an eyesore.
 * Its result is only ever different for an ActionMatcher!!
 *
 * The matching process (probably the "controller" as defined by the book)
 * should probably be the one here to distinguish between the different "rule
 * types" that we have; for instance, the fact that a Rule is in fact an Action,
 * and that when in a Sequence, the first rule, if an Action, may not call
 * certain methods.
 *
 * Of course, there remains a more fundamental problem, in that at this moment
 * what is considered to be the "semantic output" of a parsing run is the top
 * value of a parser's ValueStack; this, in itself, is a mistake. If that is to
 * change, a lot of bytecode generation routines will have to change as well.
 * Great.
 */
public interface ParseRunner<V>
{

    /**
     * Initializes the parse runner with the given error list.
     *
     * <p><strong>Don't use it</strong>: this method is only really used
     * internally by parse runners but at this moment they don't use a proper
     * builder pattern.</p>
     *
     * @param parseErrors the error list to start off with
     * @return this instance
     */
    @DoNotUse
    @Dangerous
    ParseRunner<V> withParseErrors(List<ParseError> parseErrors);

    /**
     * Initializes the parse runner with the given ValueStack instance.
     *
     * <p><strong>Don't use it</strong>: this method is only really used
     * internally by parse runners but at this moment they don't use a proper
     * builder pattern.</p>
     *
     * @param valueStack the ValueStack to use
     * @return this instance
     */
    @DoNotUse
    @Dangerous
    ParseRunner<V> withValueStack(ValueStack<V> valueStack);

    /**
     * Performs the actual parse and creates a corresponding ParsingResult instance.
     *
     * @param input the input text to parse
     * @return the ParsingResult for the run
     */
    ParsingResult<V> run(CharSequence input);

    /**
     * Performs the actual parse and creates a corresponding ParsingResult instance.
     *
     * @param input the input text to parse
     * @return the ParsingResult for the run
     */
    ParsingResult<V> run(char[] input);

    /**
     * Performs the actual parse and creates a corresponding ParsingResult instance.
     *
     * @param inputBuffer the inputBuffer to use
     * @return the ParsingResult for the run
     */
    ParsingResult<V> run(InputBuffer inputBuffer);
}
