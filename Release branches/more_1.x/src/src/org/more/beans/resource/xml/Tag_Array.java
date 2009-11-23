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
package org.more.beans.resource.xml;
import java.lang.reflect.Array;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamReader;
import org.more.NoDefinitionException;
import org.more.beans.info.BeanProp;
import org.more.beans.info.PropArray;
/**
 * 解析array标签。
 * <br/>Date : 2009-11-23
 * @author 赵永春
 */
@SuppressWarnings("unchecked")
class Tag_Array extends DoTagEvent {
    @Override
    public void doStartEvent(String xPath, XMLStreamReader xmlReader, ContextStack context) {
        PropArray array = new PropArray();
        int attCount = xmlReader.getAttributeCount();
        for (int i = 0; i < attCount; i++) {
            String key = xmlReader.getAttributeLocalName(i);
            String var = xmlReader.getAttributeValue(i);
            if (key.equals("type") == true)
                array.setPropType(var);
            else
                throw new NoDefinitionException("array标签出现未定义属性[" + key + "]");
        }
        context.context = array;
    }
    @Override
    public void doEndEvent(String xPath, XMLStreamReader xmlReader, ContextStack context) {
        ContextStack parent = context.getParent();
        //一、向父容器增加自身对象
        ArrayList elementList = (ArrayList) parent.get("tag_element");
        if (elementList == null) {
            elementList = new ArrayList();
            parent.put("tag_element", elementList);
        }
        elementList.add(context.context);
        //二、将自身的元素加入到自身的集合中。
        elementList = (ArrayList) context.get("tag_element");
        if (elementList != null) {
            Object[] objs = this.toArray(elementList, BeanProp.class);
            PropArray array = (PropArray) context.context;
            array.setArrayElements((BeanProp[]) objs);
            array.setLength(objs.length);
        }
    }
    private Object[] toArray(ArrayList al, Class<?> toType) {
        Object array = Array.newInstance(toType, al.size());
        for (int i = al.size() - 1; i >= 0; i--) {
            Object obj = al.get(i);
            Array.set(array, i, toType.cast(obj));
        }
        return (Object[]) array;
    }
}