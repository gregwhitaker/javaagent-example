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
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.stream.Collectors;

final class PropertyAgent {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Executing the premain in Agent");
        
        
        final Properties properties = new Properties();
        try (final InputStream stream = PropertyAgent.class.getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        new AgentBuilder.Default()
            .type(new AgentBuilder.RawMatcher() {
                @Override
                public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
                    System.out.println("checking..." + typeDescription.getName());
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
                }
            })
            .transform((builder, typeDescription, classLoader, module) -> {
                
                /*git a
                typeDescription
                    .getDeclaredFields()
                    .stream()
                    .filter(inDefinedShape ->
                                  inDefinedShape
                                      .getDeclaredAnnotations()
                                      .parallelStream()
                                      .map(AnnotationDescription::getAnnotationType)
                                      .anyMatch(typeDefinitions -> typeDefinitions.isAssignableTo(Property.class)))
                    .forEach(inDefinedShape -> {
                        inDefinedShape
                            .getType()
                            .getDeclaredAnnotations()
                            .stream()
                            .map(annotationDescription -> {
                                annotationDescription.getValue()
                            })
                        builder
                            .field(ElementMatchers.named(inDefinedShape.getName()))
                            
                    });*/
                    
                return builder;
            })
            .installOn(inst);
        
        System.out.println("Agent premain executed!");
    }
    
}