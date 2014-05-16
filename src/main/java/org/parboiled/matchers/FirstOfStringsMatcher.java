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

package org.parboiled.matchers;

import com.github.parboiled1.grappa.annotations.WillBeFinal;
import com.github.parboiled1.grappa.annotations.WillBePrivate;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.GrammarException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A specialized FirstOfMatcher that handles FirstOf(string, string, ...) rules much faster that the regular
 * FirstOfMatcher. If fast string matching is enabled this matcher uses a prebuilt character tree to efficiently
 * determine whether the next input characters match the rule expression.
 */
// TODO: Trie?
@WillBeFinal(version = "1.1")
public class FirstOfStringsMatcher
    extends FirstOfMatcher
{

    // a node in the character tree
    @VisibleForTesting
    static class Record
    {
        final char[] chars; // the sub characters of this node
        final Record[] subs;
            // the sub records corresponding to the respective character
        final boolean complete;
            // flag indicating that the path up to this record also constitutes a valid match

        private Record(final char[] chars, final Record[] subs,
            final boolean complete)
        {
            this.chars = chars;
            this.subs = subs;
            this.complete = complete;
        }
    }

    private final Record root; // the root of the character tree
    @WillBePrivate(version = "1.1")
    public final char[][] strings;

    public FirstOfStringsMatcher(final Rule[] subRules, final char[][] strings)
    {
        super(Preconditions.checkNotNull(subRules, "subRules"));
        verify(strings);
        this.strings = strings;
        root = createRecord(0, strings);
    }

    @Override
    public <V> boolean match(final MatcherContext<V> context)
    {
        if (!context.fastStringMatching())
            return super.match(context);

        Record rec = root;
        int ix = context.getCurrentIndex();
        final InputBuffer buffer = context.getInputBuffer();
        char c = context.getCurrentChar();
        int endIx = -1;

loop:
        while (true) {
            final char[] chars = rec.chars;
            for (int i = 0; i < chars.length; i++) {
                if (c == chars[i]) {
                    ix++;
                    rec = rec.subs[i];
                    if (rec == null) {
                        // success, we complected a tree path to a leave
                        endIx = ix;
                        break loop;
                    }
                    if (rec.complete) {
                        // we completed a valid match path, but continue looking
                        // for a longer match
                        endIx = ix;
                    }
                    c = buffer.charAt(ix);
                    continue loop;
                }
            }
            // we checked all sub branches of the current node, none matched, so
            // we are done
            break;
        }

        if (endIx == -1)
            return false; // we matched no complete path, so fail

        context.advanceIndex(endIx - context.getCurrentIndex());
        context.createNode();
        return true;
    }

    @VisibleForTesting
    static Record createRecord(final int pos, final char[][] strings)
    {
        final Map<Character, Set<char[]>> map
            = new TreeMap<Character, Set<char[]>>();
        boolean complete = false;
        for (final char[] s : strings) {
            if (s.length == pos)
                complete = true;
            if (s.length <= pos)
                continue;
            final char c = s[pos];
            Set<char[]> charStrings = map.get(c);
            if (charStrings == null) {
                charStrings = new HashSet<char[]>();
                map.put(c, charStrings);
            }
            charStrings.add(s);
        }

        if (map.isEmpty())
            return null;

        final char[] chars = new char[map.size()];
        final Record[] subs = new Record[map.size()];
        int i = 0;
        for (final Map.Entry<Character, Set<char[]>> entry: map.entrySet()) {
            chars[i] = entry.getKey();
            subs[i++] = createRecord(pos + 1,
                entry.getValue().toArray(new char[entry.getValue().size()][]));
        }
        return new Record(chars, subs, complete);
    }

    // make sure that a string is no prefix of another string later in the array
    // this would cause the second string to never match without fast-string-matching,
    // but match in the fast implementation

    private static void verify(final char[][] strings)
    {
        final int length = strings.length;
        for (int i = 0; i < length; i++) {
            final char[] a = strings[i];
inner:
            for (int j = i + 1; j < length; j++) {
                final char[] b = strings[j];
                if (b.length < a.length)
                    continue;
                for (int k = 0; k < a.length; k++)
                    if (a[k] != b[k])
                        continue inner;

                final String sa = '"' + String.valueOf(a) + '"';
                final String sb = '"' + String.valueOf(b) + '"';
                final String msg = a.length == b.length
                    ? sa + " is specified twice in a FirstOf(String...)"
                    : sa + " is a prefix of " + sb
                        + " in a FirstOf(String...) and comes before "
                        + sb + ", which prevents " + sb + " from ever matching!"
                        + " You should reverse the order of the two "
                        + "alternatives.";
                throw new GrammarException(msg);
            }
        }
    }
}