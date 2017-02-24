/*
 * Copyright 2017 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.javaagent;

import example.javaagent.core.Property;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.Throw;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

final class PropertyAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Executing the premain in Agent");
       
       new AgentBuilder.Default()
                .type(new AgentBuilder.RawMatcher() {
                    @Override
                    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
    
                        return typeDescription
                            .getDeclaredFields()
                            .parallelStream()
                            .anyMatch(inDefinedShape ->
                                          inDefinedShape
                                              .getDeclaredAnnotations()
                                              .parallelStream()
                                              .map(AnnotationDescription::getAnnotationType)
                                              .anyMatch(typeDefinitions -> typeDefinitions.isAssignableTo(Property.class))
                            );
                        
    /*
    
                        Arrays
                            .asList(classBeingRedefined.getFields())
                            .stream()
                            .filter(field -> field.getAnnotation(Property.class) != null)
                            .forEach(field -> {
                                System.out.println("fuck you byte buddy -> " + field.getName());
                            });
     */
    
                    }
                })
                .transform((builder, typeDescription, classLoader, module) -> {
                    System.out.println("SLAYER");
                    return builder;
                })
                .installOn(inst);
        
        System.out.println("Agent premain executed!");
    }

}