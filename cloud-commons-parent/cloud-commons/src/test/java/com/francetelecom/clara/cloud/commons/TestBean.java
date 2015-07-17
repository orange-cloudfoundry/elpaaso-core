/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.commons;

/**
 * A sample bean to assert the JNDI properties injection with spring works OK
 */
public class TestBean {

    private String sampleProp1;
    private String sampleProp2;
    private String sampleProp3;
    private String sampleProp4;

    public String getSampleProp1() {
        return sampleProp1;
    }

    public void setSampleProp1(String sampleProp1) {
        this.sampleProp1 = sampleProp1;
    }

    public String getSampleProp2() {
        return sampleProp2;
    }

    public void setSampleProp2(String sampleProp2) {
        this.sampleProp2 = sampleProp2;
    }

    public String getSampleProp3() {
        return sampleProp3;
    }

    public void setSampleProp3(String sampleProp3) {
        this.sampleProp3 = sampleProp3;
    }

    public String getSampleProp4() {
        return sampleProp4;
    }

    public void setSampleProp4(String sampleProp4) {
        this.sampleProp4 = sampleProp4;
    }
}
