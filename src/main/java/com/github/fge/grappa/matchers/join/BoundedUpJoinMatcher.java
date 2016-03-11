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

package com.github.fge.grappa.matchers.join;

import com.github.fge.grappa.rules.Rule;

/*
 * A joining matcher with a maximum number of matches to perform
 */
public final class BoundedUpJoinMatcher
    extends JoinMatcher
{
    private final int maxCycles;

    public BoundedUpJoinMatcher(final Rule joined, final Rule joining,
        final int maxCycles)
    {
        super(joined, joining);
        this.maxCycles = maxCycles;
    }

    @Override
    protected boolean runAgain(final int cycles)
    {
        return cycles < maxCycles;
    }

    @Override
    protected boolean enoughCycles(final int cycles)
    {
        return true;
    }
}
