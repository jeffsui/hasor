/*
 * Copyright 2008-2009 the original ������(zyc@hasor.net).
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
package org.more.classcode;
/**
 * 
 * @version : 2013-5-24
 * @author ������ (zyc@hasor.net)
 */
public class ClassCodeRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2163944328191987917L;
    public ClassCodeRuntimeException(String string, Throwable error) {
        super(string);
    }
    public ClassCodeRuntimeException(String string) {
        super(string);
    }
    public ClassCodeRuntimeException(Throwable error) {
        super(error);
    }
}