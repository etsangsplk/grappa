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

package com.github.fge.grappa.matchers.trie;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A builder for a {@link Trie}
 *
 * @since 1.0.0-beta.6
 */
public final class TrieBuilder
{
    int nrWords = 0;
    int maxLength = 0;
    final TrieNodeBuilder nodeBuilder
        = new TrieNodeBuilder();

    TrieBuilder()
    {
    }

    /**
     * Add one word to the trie
     *
     * @param word the word to add
     * @return this
     * @throws IllegalArgumentException word is empty
     */
    public TrieBuilder addWord(@Nonnull final String word)
    {
        Objects.requireNonNull(word);

        final int length = word.length();

        if (length == 0)
            throw new IllegalArgumentException("a trie cannot have empty "
                + "strings (use EMPTY instead)");
        nrWords++;
        maxLength = Math.max(maxLength, length);
        nodeBuilder.addWord(word);
        return this;
    }

    public Trie build()
    {
        return new Trie(this);
    }
}
