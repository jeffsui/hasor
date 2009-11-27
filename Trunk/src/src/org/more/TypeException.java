/*
 * Copyright 2008-2009 the original author or authors.
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
package org.more;
/**
 * 类型错误，出现该异常通常是在操作某些数据时该类数据不支持某些操作或者类型转换发生异常。
 * Date : 2009-7-7
 * @author 赵永春
 */
public class TypeException extends RuntimeException {
    /**  */
    private static final long serialVersionUID = -6286611015368846627L;
    /**
     * 类型错误，出现该异常通常是在操作某些数据时该类数据不支持某些操作或者类型转换发生异常。
     * @param string 异常的描述信息
     */
    public TypeException(String string) {
        super(string);
    }
    /**
     * 类型错误，出现该异常通常是在操作某些数据时该类数据不支持某些操作或者类型转换发生异常。
     * @param error 承接的上一个异常对象。
     */
    public TypeException(Throwable error) {
        super(error);
    }
    /**
     * 类型错误，出现该异常通常是在操作某些数据时该类数据不支持某些操作或者类型转换发生异常。
     * @param string 异常的描述信息。
     * @param error 承接的上一个异常对象。
     */
    public TypeException(String string, Throwable error) {
        super(string, error);
    }
}
