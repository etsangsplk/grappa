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

package org.parboiled.support;

import com.github.parboiled1.grappa.annotations.ThrownExceptionsWillChange;
import com.github.parboiled1.grappa.annotations.WillBeRemoved;
import org.parboiled.errors.GrammarException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A ValueStack is a stack implementation for parser values. The current state
 * of the stack can be saved and restored with the methods {@link
 * #takeSnapshot()} and {@link #restoreSnapshot(Object)} ()}, whose
 * implementations should be super efficient since they are being used
 * extensively during a parsing run. A ValueStack also serves as an Iterable
 * over the current stack values (the values are being provided with the last
 * value (on top of the stack) first).
 *
 * @param <V> the type of the value objects
 */
public interface ValueStack<V>
    extends Iterable<V>
{

    /**
     * Determines whether the stack is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of elements currently on the stack.
     *
     * @return the number of elements
     */
    int size();

    /**
     * Clears all values.
     */
    void clear();

    /**
     * Returns an object representing the current state of the stack.
     *
     * @return an object representing the current state of the stack
     */
    @Nullable
    Object takeSnapshot();

    /**
     * Restores the stack state as previously returned by {@link
     * #takeSnapshot()}.
     *
     * @param snapshot a snapshot object previously returned by {@link
     * #takeSnapshot()}
     */
    void restoreSnapshot(@Nullable Object snapshot);

    /**
     * Pushes the given value onto the stack. Equivalent to push(0, value).
     *
     * @param value the value
     */
    void push(@Nullable V value);

    /**
     * Inserts the given value a given number of elements below the current top
     * of the stack.
     *
     * @param down the number of elements to skip before inserting the value (0
     * being equivalent to push(value))
     * @param value the value
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    void push(int down, @Nullable V value);

    /**
     * Pushes all given elements onto the stack (in the order as given).
     *
     * @param firstValue the first value
     * @param moreValues the other values
     */
    void pushAll(@Nullable V firstValue, @Nullable V... moreValues);

    /**
     * Pushes all given elements onto the stack (in the order as given).
     *
     * @param values the values
     */
    // TODO: overload of varargs! Doesn't really work well
    @Deprecated
    void pushAll(@Nonnull Iterable<V> values);

    /**
     * Removes the value at the top of the stack and returns it.
     *
     * @return the current top value
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    @Nullable
    V pop();

    /**
     * Removes the value the given number of elements below the top of the stack.
     *
     * @param down the number of elements to skip before removing the value (0
     * being equivalent to pop())
     * @return the value
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    @Nullable
    V pop(int down);

    /**
     * Returns the value at the top of the stack without removing it.
     *
     * @return the current top value
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    @Nullable
    V peek();

    /**
     * Returns the value the given number of elements below the top of the stack
     * without removing it.
     *
     * @param down the number of elements to skip (0 being equivalent to peek())
     * @return the value
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    @Nullable
    V peek(int down);

    /**
     * Replaces the current top value with the given value. Equivalent to
     * poke(0, value).
     *
     * @param value the value
     * @throws IllegalArgumentException if the stack is empty
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    void poke(@Nullable V value);

    /**
     * Replaces the element the given number of elements below the current top
     * of the stack.
     *
     * @param down the number of elements to skip before replacing the value (0
     * being equivalent to poke(value))
     * @param value the value to replace with
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    void poke(int down, @Nullable V value);

    /**
     * Duplicates the top value. Equivalent to push(peek()).
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    void dup();

    /**
     * Reverses the order of the top n stack values
     *
     * @param n the number of elements to reverse
     * @throws IllegalArgumentException {@code n} is less than 2
     * @throws IllegalStateException the stack does not contain at least n
     * elements
     */
    void swap(int n);

    /**
     * Swaps the top two stack values.
     *
     * @throws GrammarException if the stack does not contain at least two
     * elements
     */
    @ThrownExceptionsWillChange(version = "1.1",
        to = IllegalStateException.class)
    void swap();

    /**
     * Reverses the order of the top 3 stack values.
     *
     * @throws GrammarException if the stack does not contain at least 3
     * elements
     *
     * @deprecated use {@link #swap(int)} instead
     */
    @Deprecated
    @WillBeRemoved(version = "1.1")
    void swap3();

    /**
     * Reverses the order of the top 4 stack values.
     *
     * @throws GrammarException if the stack does not contain at least 4
     * elements
     *
     * @deprecated use {@link #swap(int)} instead
     */
    @Deprecated
    @WillBeRemoved(version = "1.1")
    void swap4();

    /**
     * Reverses the order of the top 5 stack values.
     *
     * @throws GrammarException if the stack does not contain at least 5
     * elements
     *
     * @deprecated use {@link #swap(int)} instead
     */
    @Deprecated
    @WillBeRemoved(version = "1.1")
    void swap5();

    /**
     * Reverses the order of the top 6 stack values.
     *
     * @throws GrammarException if the stack does not contain at least 6
     * elements
     *
     * @deprecated use {@link #swap(int)} instead
     */
    @Deprecated
    @WillBeRemoved(version = "1.1")
    void swap6();
}