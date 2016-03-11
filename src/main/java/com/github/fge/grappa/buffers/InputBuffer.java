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

package com.github.fge.grappa.buffers;

import com.github.fge.grappa.support.Chars;
import com.github.fge.grappa.support.IndexRange;
import com.github.fge.grappa.support.Position;

/**
 * Abstraction of a simple character buffer holding the input text to be parsed.
 */
// TODO: it furiously resembles a CharSequence and should use that
public interface InputBuffer
{
    /**
     * Returns the character at the given index.
     *
     * <p>If the index is greater than, or equal to, the buffer's length, this
     * method returns {@link Chars#EOI}.</p>
     *
     * <p>Note that {@code EOI} is nothing else than U+FFFF; this may be legal
     * in certain inputs. It is recommended that you use {@link
     * #codePointAt(int)} instead.</p>
     *
     * @param index the index
     * @return the character at the given index or Chars.EOI.
     *
     * @throws IllegalArgumentException index is negative
     */
    char charAt(int index);

    /**
     * Returns the Unicode code point starting at a given index
     *
     * <p>If the index is greater than, or equal to, the buffer's length, this
     * method returns -1.</p>
     *
     * @param index the index
     * @return the code point at this index, or -1 if the end of input has been
     * reached
     *
     * @throws IllegalArgumentException index is negative
     */
    int codePointAt(int index);

    /**
     * Constructs a new {@link String} from all characters between the given
     * indices. Invalid indices are automatically adjusted to their respective
     * boundary.
     *
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return a String
     */
    String extract(int start, int end);

    /**
     * Constructs a new {@link String} from all character covered by the given
     * {@link IndexRange}
     *
     * @param range the range
     * @return a String
     */
    String extract(IndexRange range);

    /**
     * Returns the line and column number of the character with the given index
     * encapsulated in a {@link Position} object
     *
     * <p>Note that both line indices and column indices start at 1.</p>
     *
     * @param index the index of the character to get the line number of
     * @return the line number
     */
    Position getPosition(int index);

    /**
     * Constructs a new {@link String} containing all characters with the given
     * line number, except for the trailing newline
     *
     * @param lineNumber the line number to get
     * @return the string
     */
    String extractLine(int lineNumber);

    /**
     * Get the index range matching a given line number
     *
     * @param lineNumber the line number
     * @return the index range
     */
    IndexRange getLineRange(int lineNumber);

    /**
     * Returns the number of lines in the input buffer.
     *
     * @return number of lines in the input buffer.
     */
    int getLineCount();

    /**
     * Returns the number of characters in this input buffer
     *
     * <p>Note that here a "character" means a Java {@code char}, not a Unicode
     * code point.</p>
     *
     * @return see description
     */
    int length();
}
