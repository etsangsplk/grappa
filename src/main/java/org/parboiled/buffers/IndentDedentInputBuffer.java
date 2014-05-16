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

package org.parboiled.buffers;

import com.github.parboiled1.grappa.annotations.WillBeRemoved;
import com.google.common.base.Preconditions;
import org.parboiled.common.IntArrayStack;
import org.parboiled.errors.IllegalIndentationException;
import org.parboiled.support.Chars;
import org.parboiled.support.IndexRange;
import org.parboiled.support.Position;

/**
 * Special, immutable InputBuffer implementation for indentation based grammars.
 *
 * <p>This InputBuffer collapses all space and tab characters at the beginning
 * of a text line into either nothing (if the line has the same indentation
 * level as the previous line), a special {@link Chars#INDENT} character (if the
 * line has a greater indentation level than the previous line) or one or more
 * {@link Chars#DEDENT} characters (if the line has a lower indentation level
 * than the previous line).</p>
 *
 * <p>Blank lines (lines containing nothing but whitespace) are removed from
 * the input and the buffer can, optionally, remove line comments (i.e. comments
 * that start with a predefined character sequence and go to the end of the
 * line).</p>
 *
 * <p>This means that the highest index of this InputBuffer is probably smaller
 * than that of the original input text buffer, since all line indentations and
 * blank lines have been collapsed. However, the implementation will make sure
 * that {@link #getPosition(int)}, {@link #extract(int, int)}, etc. will work as
 * expected and always return the "correct" result from the underlying, original
 * input buffer.</p>
 *
 * <p>If the input contains illegal indentation the buffer throws an {@link
 * IllegalIndentationException} during construction.</p>
 *
 * @deprecated will have no replacement; the two characters used for indent and
 * "dedent" (respectively {@code «} and {@code »}) are of common use.
 */
@Deprecated
@WillBeRemoved(version = "1.1")
public final class IndentDedentInputBuffer
    implements InputBuffer
{
    private final DefaultInputBuffer origBuffer;
    private final DefaultInputBuffer convBuffer;

    private final int[] indexMap; // maps convBuffer indices to origBuffer indices
    private final boolean strict;
    private final boolean skipEmptyLines;

    /**
     * Creates a new IndentDedentInputBuffer around the given char array. Note
     * that for performance reasons the given char array is not defensively
     * copied.
     *
     * @param input the input text.
     * @param tabStop the number of characters in a tab stop.
     * @param lineCommentStart the string starting a line comment or null, if
     * line comments are not defined
     * @param strict signals whether the buffer should throw an {@link
     * IllegalIndentationException} on "semi-dedents", if false the buffer
     * silently accepts these
     * @throws IllegalIndentationException if the input contains illegal
     * indentations and the strict flag is set
     */
    public IndentDedentInputBuffer(final char[] input, final int tabStop,
        final String lineCommentStart, final boolean strict)
    {
        this(input, tabStop, lineCommentStart, strict, true);
    }

    /**
     * Creates a new IndentDedentInputBuffer around the given char array. Note
     * that for performance reasons the given char array is not defensively
     * copied.
     *
     * @param input the input text.
     * @param tabStop the number of characters in a tab stop.
     * @param lineCommentStart the string starting a line comment or null, if
     * line comments are not defined
     * @param strict signals whether the buffer should throw an {@link
     * IllegalIndentationException} on "semi-dedents", if false the buffer
     * silently accepts these
     * @param skipEmptyLines signals whether the buffer should swallow empty
     * lines
     * @throws IllegalIndentationException if the input contains illegal
     * indentations and the strict flag is set
     */
    public IndentDedentInputBuffer(final char[] input, final int tabStop,
        final String lineCommentStart, final boolean strict, final boolean skipEmptyLines)
    {
        this.strict = strict;
        this.skipEmptyLines = skipEmptyLines;
        Preconditions.checkArgument(tabStop > 0, "tabStop must be > 0");
        Preconditions.checkArgument(lineCommentStart == null
            || lineCommentStart.indexOf('\n') == -1,
            "lineCommentStart must not contain newlines");
        origBuffer = new DefaultInputBuffer(input);
        final BufferConverter converter = new BufferConverter(tabStop,
            lineCommentStart != null ? lineCommentStart.toCharArray() : null);
        convBuffer = new DefaultInputBuffer(converter.builder.getChars());
        indexMap = converter.builder.getIndexMap();
    }

    @Override
    public char charAt(final int index)
    {
        return convBuffer.charAt(index);
    }

    @Override
    public boolean test(final int index, final char[] characters)
    {
        return convBuffer.test(index, characters);
    }

    @Override
    public String extract(final int start, final int end)
    {
        return origBuffer.extract(map(start), map(end));
    }

    @Override
    public String extract(final IndexRange range)
    {
        return origBuffer.extract(map(range.start), map(range.end));
    }

    @Override
    public Position getPosition(final int index)
    {
        return origBuffer.getPosition(map(index));
    }

    @Override
    public int getOriginalIndex(final int index)
    {
        return map(index);
    }

    @Override
    public String extractLine(final int lineNumber)
    {
        return origBuffer.extractLine(lineNumber);
    }

    @Override
    public int getLineCount()
    {
        return origBuffer.getLineCount();
    }

    private int map(final int convIndex)
    {
        if (convIndex < 0)
            return indexMap[0];
        if (convIndex < indexMap.length)
            return indexMap[convIndex];
        if (indexMap.length == 0)
            return 1;
        return indexMap[indexMap.length - 1] + 1;
    }

    private final class BufferConverter
    {
        private final BufferBuilder builder = new BufferBuilder();
        private final int tabStop;
        private final char[] lineCommentStart;
        private final IntArrayStack previousLevels = new IntArrayStack();
        private int cursor = 0;
        private char currentChar;

        private BufferConverter(final int tabStop,
            final char[] lineCommentStart)
        {
            this.tabStop = tabStop;
            this.lineCommentStart = lineCommentStart;
            currentChar = origBuffer.charAt(0);
            build();
        }

        private void build()
        {
            previousLevels.push(0);

            // consume inital indent
            int currentLevel = skipIndent();

            // transform all other input
            while (currentChar != Chars.EOI) {
                final int commentChars = skipLineComment();
                if (currentChar != '\n' && currentChar != Chars.EOI) {
                    builder.append(currentChar);
                    advance();
                    continue;
                }

                // register newline
                builder.appendNewline(commentChars);
                advance();

                // consume line indent
                final int indent = skipIndent();

                // generate INDENTS/DEDENTS
                if (indent > currentLevel) {
                    previousLevels.push(currentLevel);
                    currentLevel = indent;
                    builder.append(Chars.INDENT);
                } else {
                    while (indent < currentLevel && indent <= previousLevels
                        .peek()) {
                        currentLevel = previousLevels.pop();
                        builder.append(Chars.DEDENT);
                    }
                    if (strict && indent < currentLevel) {
                        throw new IllegalIndentationException(origBuffer,
                            origBuffer.getPosition(cursor));
                    }
                }
            }

            // make sure to close all remaining indentation scopes
            if (previousLevels.size() > 1) {
                builder.append('\n');
                while (previousLevels.size() > 1) {
                    previousLevels.pop();
                    builder.append(Chars.DEDENT);
                }
            }
        }

        private int skipIndent()
        {
            int indent = 0;
loop:
            while (true) {
                switch (currentChar) {
                    case ' ':
                        indent++;
                        advance();
                        continue;
                    case '\t':
                        indent = (indent / tabStop + 1) * tabStop;
                        advance();
                        continue;
                    case '\n':
                        if (!skipEmptyLines)
                            builder.appendNewline(0);
                        indent = 0;
                        advance();
                        continue;
                    case Chars.EOI:
                        indent = 0;
                        break loop;
                    default:
                        if (skipLineComment() == 0)
                            break loop;
                }
            }
            return indent;
        }

        private void advance()
        {
            currentChar = origBuffer.charAt(++cursor);
        }

        private int skipLineComment()
        {
            if (lineCommentStart != null && origBuffer
                .test(cursor, lineCommentStart)) {
                final int start = cursor;
                while (currentChar != '\n' && currentChar != Chars.EOI) {
                    advance();
                }
                return cursor - start;
            }
            return 0;
        }

        private final class BufferBuilder
        {
            private final StringBuilder sb = new StringBuilder();
            private final IntArrayStack stack = new IntArrayStack();

            private void append(final char c)
            {
                stack.push(cursor);
                sb.append(c);
            }

            private void appendNewline(final int commentChars)
            {
                stack.push(cursor - commentChars);
                sb.append('\n');
            }

            private char[] getChars()
            {
                final char[] buffer = new char[sb.length()];
                sb.getChars(0, sb.length(), buffer, 0);
                return buffer;
            }

            private int[] getIndexMap()
            {
                return stack.toArray();
            }
        }
    }
}
