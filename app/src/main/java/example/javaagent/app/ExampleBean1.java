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

import example.javaagent.core.Property;

public class ExampleBean1 {

    @Property("example.javaagent.prop1")
    public String stringProp;

    @Property("example.javaagent.prop2")
    public Integer intProp;

    @Property("example.javaagent.prop3")
    public Long longProp;

    @Property("example.javaagent.prop4")
    public Double doubleProp;

    @Property("example.javaagent.prop5")
    public Float floatProp;

    @Property("example.javaagent.prop6")
    public boolean booleanProp;

    public String nonAnnotatedProp = "No annotation";

}
