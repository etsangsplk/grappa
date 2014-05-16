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

package org.parboiled.transform;

import com.github.parboiled1.grappa.annotations.VisibleForDocumentation;
import com.github.parboiled1.grappa.annotations.WillBeFinal;
import com.github.parboiled1.grappa.annotations.WillBePrivate;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

@VisibleForDocumentation
public abstract class BaseGroupClass
{
    @WillBePrivate(version = "1.1")
    public final String name;

    protected BaseGroupClass(final String name)
    {
        this.name = Preconditions.checkNotNull(name, "name");
    }

    @Override
    @Nonnull
    @WillBeFinal(version = "1.1")
    public String toString()
    {
        return name;
    }
}