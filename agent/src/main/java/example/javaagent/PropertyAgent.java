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
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.Transformer;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.Set;

final class PropertyAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Executing the premain in Agent");

        final Properties properties = new Properties();
        try (final InputStream stream = PropertyAgent.class.getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("example.javaagent"))
                .setScanners(new TypeAnnotationsScanner(),
                        new FieldAnnotationsScanner()));

        final Set<Field> fields = reflections.getFieldsAnnotatedWith(Property.class);

        new AgentBuilder.Default()
                .type(ElementMatchers.any())
                .transform((builder, typeDescription, classLoader, module) -> {
                    return builder.field(ElementMatchers.anyOf(fields))
                            .transform((instrumentedType, target) -> {
                                System.out.println(target);
                                return null;
                            });
                })
                .installOn(inst);

//        inst.addTransformer((classLoader, s, clazz, protectionDomain, bytes) -> {
//            if (fieldMap.containsKey(clazz)) {
//                try {
//                    ClassPool cp = ClassPool.getDefault();
//                    CtClass cc = cp.get(clazz.getName());
//
//                    fieldMap.get(clazz).forEach(f -> {
//                        try {
//                            CtField ctField = cc.getField(f.getName());
//                        } catch (NotFoundException e) {
//                            throw new RuntimeException("Error load-time weaving properties for class '" + clazz.getName() + "'");
//                        }
//                    });
//
//                    byte[] byteCode = cc.toBytecode();
//                    cc.detach();
//                    return byteCode;
//                } catch (Exception e) {
//                    throw new RuntimeException("Error load-time weaving properties for class '" + clazz.getName() + "'");
//                }
//            }
//
//            return null;
//        });
    }

}
