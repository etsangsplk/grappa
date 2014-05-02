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

package org.parboiled.transform.process;

import com.google.common.base.Preconditions;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.parboiled.transform.ParserClassNode;
import org.parboiled.transform.RuleMethod;
import org.parboiled.transform.Types;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

/**
 * Adds automatic labelling code before the return instruction.
 */
public class LabellingGenerator implements RuleMethodProcessor {

    @Override
    public boolean appliesTo(final ParserClassNode classNode, final RuleMethod method) {
        Preconditions.checkNotNull(classNode, "classNode");
        Preconditions.checkNotNull(method, "method");
        return !method.hasDontLabelAnnotation();
    }

    @Override
    public void process(final ParserClassNode classNode, final RuleMethod method) throws Exception {
        Preconditions.checkNotNull(classNode, "classNode");
        Preconditions.checkNotNull(method, "method");
        Preconditions.checkState(!method.isSuperMethod()); // super methods
        // have flag
        // moved to the overriding method

        final InsnList instructions = method.instructions;

        AbstractInsnNode ret = instructions.getLast();
        while (ret.getOpcode() != ARETURN) {
            ret = ret.getPrevious();
        }

        final LabelNode isNullLabel = new LabelNode();
        // stack: <rule>
        instructions.insertBefore(ret, new InsnNode(DUP));
        // stack: <rule> :: <rule>
        instructions.insertBefore(ret, new JumpInsnNode(IFNULL, isNullLabel));
        // stack: <rule>
        instructions.insertBefore(ret, new LdcInsnNode(getLabelText(method)));
        // stack: <rule> :: <labelText>
        instructions.insertBefore(ret, new MethodInsnNode(INVOKEINTERFACE,
            Types.RULE.getInternalName(), "label",
            "(Ljava/lang/String;)" + Types.RULE_DESC, true));
        // stack: <rule>
        instructions.insertBefore(ret, isNullLabel);
        // stack: <rule>
    }

    public String getLabelText(final RuleMethod method) {
        if (method.visibleAnnotations != null) {
            for (final Object annotationObj : method.visibleAnnotations) {
                final AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(Types.LABEL_DESC) && annotation.values != null) {
                    Preconditions.checkState("value".equals(annotation.values
                        .get(0)));
                    final String labelValue = (String) annotation.values.get(1);
                    return labelValue.isEmpty() ? method.name : labelValue;
                }
            }
        }
        return method.name;
    }

}