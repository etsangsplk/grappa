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

package com.github.fge.grappa.transform.process;

import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Action;
import com.github.fge.grappa.rules.Rule;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import com.github.fge.grappa.transform.base.InstructionGraphNode;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static com.github.fge.grappa.misc.AsmUtils.isBooleanValueOfZ;

/**
 * Makes all implicit action expressions in a rule method explicit.
 */
public final class ImplicitActionsConverter
    implements RuleMethodProcessor
{
    private final Set<InstructionGraphNode> covered
        = new HashSet<>();
    private RuleMethod method;

    @Override
    public boolean appliesTo(@Nonnull final ParserClassNode classNode,
        @Nonnull final RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return method.containsImplicitActions();
    }

    @Override
    public void process(@Nonnull final ParserClassNode classNode,
        @Nonnull final RuleMethod method)
        throws Exception
    {
        this.method = Objects.requireNonNull(method, "method");
        // TODO: why? Tests pass if I comment the line below; something missing?
        covered.clear();
        walkNode(method.getReturnInstructionNode());
        method.setContainsImplicitActions(false);
    }

    private void walkNode(final InstructionGraphNode node)
    {
        if (covered.contains(node))
            return;

        covered.add(node);

        if (isImplicitAction(node)) {
            replaceWithActionWrapper(node);
            method.setContainsExplicitActions(true);
            return;
        }

        if (node.isActionRoot())
            return;

        node.getPredecessors().forEach(this::walkNode);
    }

    private void replaceWithActionWrapper(final InstructionGraphNode node)
    {
        final MethodInsnNode insn = createActionWrappingInsn();
        method.instructions.set(node.getInstruction(), insn);
        node.setIsActionRoot();
        node.setInstruction(insn);
    }

    private boolean isImplicitAction(final InstructionGraphNode node)
    {
        // an implicit action must be a call to Boolean.valueOf(boolean)
        if (!isBooleanValueOfZ(node.getInstruction()))
            return false;

        // it must have exactly one other instruction that depends on it
        final List<InstructionGraphNode> dependents = getDependents(node);
        if (dependents.size() != 1)
            return false;

        // this dependent instruction must be rule method call
        final InstructionGraphNode dependent = dependents.get(0);
        return isObjectArgumentToRuleCreatingMethodCall(node, dependent)
            || isStoredIntoObjectArray(dependent);
    }

    private static boolean isObjectArgumentToRuleCreatingMethodCall(
        final InstructionGraphNode node, final InstructionGraphNode dependent)
    {
        // is the single dependent a method call ?
        final AbstractInsnNode insn = dependent.getInstruction();
        if (insn.getType() != AbstractInsnNode.METHOD_INSN)
            return false;

        // Does this method call return a Rule ?
        final MethodInsnNode methodNode = (MethodInsnNode) insn;
        if (!Type.getType(Rule.class).equals(Type.getReturnType(
            methodNode.desc)))
            return false;

        // Does the result of the Boolean.valueOf(boolean) call correspond to
        // an Object parameter ?
        final Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
        final int argIndex = getArgumentIndex(dependent, node);

        Preconditions.checkState(argIndex < argTypes.length);

        final String typeName = argTypes[argIndex].getInternalName();
        return CodegenUtils.p(Object.class).equals(typeName);
    }

    private boolean isStoredIntoObjectArray(final InstructionGraphNode node)
    {
        // is the single dependent an AASTORE instruction ?
        final AbstractInsnNode insn = node.getInstruction();
        if (insn.getOpcode() != AASTORE)
            return false;

        // Does this instruction store into an array of Object ?
        final List<InstructionGraphNode> dependents = getDependents(node);

        // an AASTORE instruction should have exactly one dependent
        Preconditions.checkState(dependents.size() == 1);

        final AbstractInsnNode newArrayInsn
            = dependents.get(0).getInstruction();
        // which should be a n ANEWARRAY instruction
        Preconditions.checkState(newArrayInsn.getOpcode() == ANEWARRAY);

        final String desc = ((TypeInsnNode) newArrayInsn).desc;
        return CodegenUtils.p(Object.class).equals(desc);
    }

    private static int getArgumentIndex(final InstructionGraphNode callNode,
        final InstructionGraphNode predecessor)
    {
        final int startIndex
            = callNode.getInstruction().getOpcode() == INVOKESTATIC ? 0 : 1;

        InstructionGraphNode argumentNode;

        for (int i = startIndex; i < callNode.getPredecessors().size(); i++) {
            argumentNode = callNode.getPredecessors().get(i);
            if (predecessor.equals(argumentNode))
                return i - startIndex;
        }

        throw new IllegalStateException();
    }

    private List<InstructionGraphNode> getDependents(
        final InstructionGraphNode predecessor)
    {
        final List<InstructionGraphNode> dependents = Lists.newArrayList();

        for (final InstructionGraphNode node: method.getGraphNodes())
            if (node.getPredecessors().contains(predecessor))
                dependents.add(node);

        return dependents;
    }

    private static MethodInsnNode createActionWrappingInsn()
    {
        return new MethodInsnNode(INVOKESTATIC,
            CodegenUtils.p(BaseParser.class), "ACTION",
            CodegenUtils.sig(Action.class, boolean.class), false);
    }
}
