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

package example.javaagent.app;

public class Main {

    public static void main(String... args) {
        System.out.println("App is running...");
    
        ExampleBean example = new ExampleBean();
        ExampleBean1 example2 = new ExampleBean1();
        System.out.println("example.javaagent.prop1 = " + example.stringProp);
        System.out.println("example.javaagent.prop2 = " + example.intProp);
        System.out.println("example.javaagent.prop3 = " + example.longProp);
        System.out.println("example.javaagent.prop4 = " + example.doubleProp);
        System.out.println("example.javaagent.prop5 = " + example.floatProp);
        System.out.println("example.javaagent.prop6 = " + example.booleanProp);
        System.out.println("nonAnnotatedProperty = " + example.nonAnnotatedProp);
    }

}
