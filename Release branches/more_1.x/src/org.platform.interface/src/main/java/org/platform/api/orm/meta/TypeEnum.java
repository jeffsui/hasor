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
package org.platform.api.orm.meta;
/**
 * 
 * @version : 2013-1-27
 * @author ������ (zyc@byshell.org)
 */
public enum TypeEnum {
    /**�ַ���*/
    TString("string"),
    /**������*/
    TFloat("float"),
    /**����*/
    TInteger("int"),
    /**˫���ȸ�����*/
    TDouble("double"),
    /**˫��������*/
    TLong("long"),
    /**����ֵ*/
    TBoolean("boolean"),
    /**ʱ����������*/
    TDatetime("datetime"),
    /**ID������*/
    TUUID("uuid"),
    /**�ֽ�����*/
    TBtye("byte"),
    /**JSON����*/
    TJson("json"),
    /**�ֽ�����*/
    TBytes("bytes");
    //
    //
    //
    private String value = null;
    TypeEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}