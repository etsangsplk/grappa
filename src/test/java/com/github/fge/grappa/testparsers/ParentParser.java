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

package com.github.fge.grappa.testparsers;

import org.parboiled.BaseActions;
import org.parboiled.Rule;
import org.parboiled.annotations.Label;

public class ParentParser
    extends TestParser<Object>
{
    public static class Actions
        extends BaseActions<Object>
    {
        public boolean dummy()
        {
            return true;
        }
    }

    public final Actions actions = new Actions();

    @Override
    @Label("abcd")
    public Rule mainRule()
    {
        return sequence("ab", "cd", actions.dummy())
            .label("abcd");
    }
}