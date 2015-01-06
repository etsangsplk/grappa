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

package org.parboiled.errors;

import com.github.parboiled1.grappa.buffers.InputBuffer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.parboiled.common.Formatter;
import com.github.parboiled1.grappa.matchers.Matcher;
import org.parboiled.matchers.TestNotMatcher;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Position;

import javax.annotation.Nullable;
import java.util.List;

/**
 * General utility methods regarding parse errors.
 */
public final class ErrorUtils
{
    private static final Joiner ERROR_JOINER = Joiner.on("---\n");
    private static final Function<ParseError, String> ERROR_TO_STRING
        = new Function<ParseError, String>()
    {
        @Override
        public String apply(final ParseError input)
        {
            return printParseError(input);
        }
    };

    private ErrorUtils()
    {
    }

    /**
     * Finds the Matcher in the given failedMatcherPath whose label is best for
     * presentation in "expected" strings of parse error messages, given the
     * provided lastMatchPath.
     *
     * @param path the path to the failed matcher
     * @param errorIndex the start index of the respective parse error
     * @return the matcher whose label is best for presentation in "expected" strings
     */
    @Nullable
    static Matcher findProperLabelMatcher(final MatcherPath path,
        final int errorIndex)
    {
        try {
            return findProperLabelMatcher0(path, errorIndex);
        } catch (RuntimeException e) {
            if (e == REMOVE_THAT_DAMNIT)
                return null;
            throw e;
        }
    }

    // TODO: as the name says
    private static final RuntimeException REMOVE_THAT_DAMNIT
        = new RuntimeException()
        {
            @Override
            public synchronized Throwable fillInStackTrace()
            {
                return this;
            }
        };

    @Nullable
    private static Matcher findProperLabelMatcher0(final MatcherPath path,
        final int errorIndex)
    {
        Preconditions.checkNotNull(path, "path");
        final Matcher found = path.hasParent()
            ? findProperLabelMatcher0(path.getParent(), errorIndex)
            : null;
        if (found != null)
            return found;
        final Matcher m = path.getElement().getMatcher();
        if (m instanceof TestNotMatcher)
            throw REMOVE_THAT_DAMNIT; // TODO...
        if (path.getElement().getStartIndex() == errorIndex
            && m.hasCustomLabel())
            return m;
        return null;
    }

    /**
     * Pretty prints the parse errors of the given ParsingResult showing their location in the given input buffer.
     *
     * @param parsingResult the parsing result
     * @return the pretty print text
     */
    public static String printParseErrors(final ParsingResult<?> parsingResult)
    {
        Preconditions.checkNotNull(parsingResult, "parsingResult");
        return printParseErrors(parsingResult.getParseErrors());
    }

    /**
     * Pretty prints the given parse errors showing their location in the given input buffer.
     *
     * @param errors the parse errors
     * @return the pretty print text
     */
    public static String printParseErrors(final List<ParseError> errors)
    {
        // TODO: this should never be null to start with, track that down
        Preconditions.checkNotNull(errors, "errors");

        return ERROR_JOINER.join(Iterables.transform(errors, ERROR_TO_STRING));
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error the parse error
     * @return the pretty print text
     */
    // TODO: get rid of that method call in MatcherContext, no less :/
    public static String printParseError(final ParseError error)
    {
        Preconditions.checkNotNull(error, "error");
        return printParseError(error, new DefaultInvalidInputErrorFormatter());
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error the parse error
     * @param formatter the formatter for InvalidInputErrors
     * @return the pretty print text
     */
    public static String printParseError(final ParseError error,
        final Formatter<InvalidInputError> formatter)
    {
        Preconditions.checkNotNull(error, "error");
        Preconditions.checkNotNull(formatter, "formatter");
        String msg = error.getErrorMessage();
        if (msg == null)
            msg = error instanceof InvalidInputError
                ? formatter.format((InvalidInputError) error)
                : error.getClass().getSimpleName();
        return printErrorMessage("%s (line %s, pos %s):", msg,
            error.getStartIndex(), error.getEndIndex(), error.getInputBuffer());
    }

    /**
     * Prints an error message showing a location in the given InputBuffer.
     *
     * @param format the format string, must include three placeholders for a string
     * (the error message) and two integers (the error line / column respectively)
     * @param errorMessage the error message
     * @param errorIndex the error location as an index into the inputBuffer
     * @param inputBuffer the underlying InputBuffer
     * @return the error message including the relevant line from the underlying input plus location indicator
     */
    public static String printErrorMessage(final String format,
        final String errorMessage, final int errorIndex,
        final InputBuffer inputBuffer)
    {
        Preconditions.checkNotNull(inputBuffer, "inputBuffer");
        return printErrorMessage(format, errorMessage, errorIndex,
            errorIndex + 1, inputBuffer);
    }

    /**
     * Prints an error message showing a location in the given InputBuffer.
     *
     * @param format the format string, must include three placeholders for a string
     * (the error message) and two integers (the error line / column respectively)
     * @param errorMessage the error message
     * @param startIndex the start location of the error as an index into the inputBuffer
     * @param endIndex the end location of the error as an index into the inputBuffer
     * @param inputBuffer the underlying InputBuffer
     * @return the error message including the relevant line from the underlying input plus location indicators
     */
    public static String printErrorMessage(final String format,
        final String errorMessage, final int startIndex, final int endIndex,
        final InputBuffer inputBuffer)
    {
        Preconditions.checkNotNull(inputBuffer, "inputBuffer");
        Preconditions.checkArgument(startIndex <= endIndex);
        final Position pos = inputBuffer.getPosition(startIndex);
        final StringBuilder sb = new StringBuilder(String.format(format,
            errorMessage, pos.getLine(), pos.getColumn()));
        sb.append('\n');

        final String line = inputBuffer.extractLine(pos.getLine());
        sb.append(line);
        sb.append('\n');

        final int charCount = Math.max(
            Math.min(endIndex - startIndex,
                line.length() - pos.getColumn() + 2), 1);
        for (int i = 0; i < pos.getColumn() - 1; i++)
            sb.append(' ');
        for (int i = 0; i < charCount; i++)
            sb.append('^');
        sb.append("\n");

        return sb.toString();
    }
}
