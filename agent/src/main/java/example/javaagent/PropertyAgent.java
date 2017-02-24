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
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.Throw;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("example.javaagent"))
                .setScanners(new TypeAnnotationsScanner(),
                        new FieldAnnotationsScanner()));

        final Map<Class<?>, Set<Field>> fieldMap = new HashMap<>();
        final Set<Field> fields = reflections.getFieldsAnnotatedWith(Property.class);

        fields.forEach(f -> {
            Class<?> clazz = f.getDeclaringClass();
            if (!fieldMap.containsKey(clazz)) {
                Set<Field> clazzFields = new HashSet<>();
                fields.add(f);
                fieldMap.put(clazz, clazzFields);
            } else {
                fieldMap.get(clazz).add(f);
            }
        });
        
        try {
    
            fieldMap
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    final Class<?> clazz = entry.getKey();
                    final Set<Field> value = entry.getValue();
            
                    return value
                               .stream()
                               .map(field -> {
                        System.out.println("class " + clazz.getName() + " - field - " + field.getName());
                        
                        //return field;*/
                    
                                   Property annotation = field
                                                             .getAnnotation(Property.class);
                                   Object o = properties
                                                  .get(annotation.value());
                    
                    
                                   Class<?> type = field
                                                       .getType();
                    
                                   new AgentBuilder
                                           .Default()
                                       .type(ElementMatchers.named(clazz.getName()))
                                       .transform((builder, typeDescription, classLoader, module) -> {
                                           DynamicType.Builder.FieldDefinition.Valuable<?> field1 = builder
                                                                                                        .field(ElementMatchers.is(field));
                            
                                           if (type.isAssignableFrom(Integer.class)) {
                                               field1.value((int) o);
                                           } else if (type.isAssignableFrom(Long.class)) {
                                               field1.value((long) o);
                                           } else if (type.isAssignableFrom(Double.class)) {
                                               field1.value((double) o);
                                           } else if (type.isAssignableFrom(Boolean.class)) {
                                               field1.value((boolean) o);
                                           } else if (type.isAssignableFrom(Float.class)) {
                                               field1.value((float) o);
                                           } else if (type.isAssignableFrom(String.class)) {
                                               field1.value((String) o);
                                           }
                            
                                           return builder;
                                       })
                                       .installOn(inst);
                    
                    
                                   return field;
                               });
            
                })
                .collect(Collectors.toList());
    
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        
        /*
        ClassReloadingStrategy classReloadingStrategy = ClassReloadingStrategy
                                                   .fromInstalledAgent();
new ByteBuddy()
  .redefine(Foo.class)
  .method(named("getHello"))
  .intercept(FixedValue.value("Byte Buddy!"))
  .make()
  .load(Foo.class.getClassLoader(), classReloadingStrategy);
assertThat(foo.getHello(), is("Byte Buddy!"));
classReloadingStrategy.reset(Foo.class);
assertThat(foo.getHello(), is("Hello World"));
        
        
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
         */
        
        /*
        
        
        inst.addTransformer((classLoader, s, clazz, protectionDomain, bytes) -> {
            if (fieldMap.containsKey(clazz)) {
                try {
                    ClassPool cp = ClassPool.getDefault();
                    CtClass cc = cp.get(clazz.getName());

                    fieldMap.get(clazz).forEach(f -> {
                        try {
                            CtField ctField = cc.getField(f.getName());
                        } catch (NotFoundException e) {
                            throw new RuntimeException("Error load-time weaving properties for class '" + clazz.getName() + "'");
                        }
                    });

                    byte[] byteCode = cc.toBytecode();
                    cc.detach();
                    return byteCode;
                } catch (Exception e) {
                    throw new RuntimeException("Error load-time weaving properties for class '" + clazz.getName() + "'");
                }
            }

            return null;
        });*/
        

        System.out.println("Agent premain executed!");
    }

}