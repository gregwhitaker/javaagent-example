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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

final class PropertyAgent {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Executing the premain in Agent");

        final Properties properties = new Properties();
        try (final InputStream stream = PropertyAgent.class.getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        inst.addTransformer((classLoader, s, clazz, protectionDomain, bytes) -> {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            PropertyAnnotationVisitor visitor = new PropertyAnnotationVisitor(writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
        });
        
        System.out.println("Agent premain executed!");
    }
    
}