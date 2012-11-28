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
package org.more.hypha.xml.tags.beans;
import org.more.core.xml.XmlStackDecorator;
import org.more.core.xml.stream.EndElementEvent;
import org.more.hypha.define.AbstractPropertyDefine;
import org.more.hypha.define.ValueMetaData;
import org.more.hypha.define.Collection_ValueMetaData;
import org.more.hypha.xml.XmlDefineResource;
/**
 * �����������Ԫ��Ϣ��ǩ�Ļ��࣬���࿼����ֵԪ��Ϣ���������ڶ�����һ��ֵԪ��Ϣ������֮�е������
 * @version 2010-9-19
 * @author ������ (zyc@byshell.org)
 */
public abstract class TagBeans_AbstractValueMetaDataDefine<T extends ValueMetaData> extends TagBeans_AbstractDefine<T> {
    /**����ֵԪ��Ϣ.*/
    public static final String ValueMetaDataDefine = "$more_Beans_ValueMetaDataDefine";
    /**����{@link TagBeans_AbstractValueMetaDataDefine}����*/
    public TagBeans_AbstractValueMetaDataDefine(XmlDefineResource configuration) {
        super(configuration);
    }
    /**���ԵĶ�������*/
    protected String getAttributeName() {
        return ValueMetaDataDefine;
    }
    /**ֻ�ڵ�ǰջ��Ѱ��*/
    protected boolean isSpanStack() {
        return false;
    }
    /**���𽫽���������ValueMetaData���õ�������*/
    public void endElement(XmlStackDecorator<Object> context, String xpath, EndElementEvent event) {
        //1.��ȡ��ǰԪ��Ϣ�������������ǰֵԪ��Ϣ�����Ƿ�������һ��ֵ��Ϣ����֮�»�Ҫ��ȡ����ֵԪ��Ϣ��������
        ValueMetaData currentMetaData = this.getDefine(context);
        ValueMetaData parentMetaData = (ValueMetaData) context.getParentStack().getAttribute(ValueMetaDataDefine);
        //
        if (parentMetaData != null && parentMetaData instanceof Collection_ValueMetaData)
            //���������˼�������ǰ������Ϣ��������һ��Collection_ValueMetaData֮����ô�����������Ϣ���ӵ���������С�
            //Map��ǩ֮���Բ���ע��key��var��ǩ����Ϊ���mapҲ��Collection_ValueMetaDataһ�֡�
            ((Collection_ValueMetaData<ValueMetaData>) parentMetaData).addObject(currentMetaData);
        else {
            AbstractPropertyDefine pdefine = (AbstractPropertyDefine) context.getAttribute(TagBeans_AbstractPropertyDefine.PropertyDefine);
            if (currentMetaData != null)
                /**����ֵ������ǩ������������ֵ���б�����������ǩ���ȵ�������*/
                pdefine.setValueMetaData(currentMetaData);
        }
        super.endElement(context, xpath, event);
    };
}