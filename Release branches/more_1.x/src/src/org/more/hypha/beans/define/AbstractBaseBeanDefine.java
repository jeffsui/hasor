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
package org.more.hypha.beans.define;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.more.core.error.RepeateException;
import org.more.hypha.AbstractBeanDefine;
import org.more.hypha.AbstractMethodDefine;
import org.more.hypha.commons.define.AbstractDefine;
/**
 * TemplateBeanDefine类用于定义一个bean的模板。
 * @version 2010-9-15
 * @author 赵永春 (zyc@byshell.org)
 */
public abstract class AbstractBaseBeanDefine extends AbstractDefine<AbstractBeanDefine> implements AbstractBeanDefine {
    private String                                id            = null;                                       //id
    private String                                name          = null;                                       //名称
    private String                                logicPackage  = null;                                       //逻辑包
    private String                                iocEngine     = "Ioc";                                      //生成Bean所使用的生成器名
    private boolean                               boolAbstract  = false;                                      //抽象标志
    private boolean                               boolSingleton = false;                                      //单态标志
    private boolean                               boolLazyInit  = true;                                       //延迟装载标志
    private boolean                               boolCheckType = true;                                       //是否要求强制类型检查
    private String                                description   = null;                                       //描述信息
    /**获取工厂bean。*/
    private AbstractBeanDefine                    factoryBean   = null;
    private AbstractMethodDefine                  factoryMethod = null;
    //创建工厂方法描述
    //
    private ArrayList<ConstructorDefine>          initParams    = new ArrayList<ConstructorDefine>();         //初始化参数
    private List<String>                          propertyNames = new ArrayList<String>();
    private HashMap<String, PropertyDefine>       propertys     = new LinkedHashMap<String, PropertyDefine>(); //属性
    private List<String>                          methodNames   = new ArrayList<String>();
    private HashMap<String, AbstractMethodDefine> methods       = new HashMap<String, AbstractMethodDefine>(); //方法
    private String                                initMethod    = null;                                       //初始化方法
    private String                                destroyMethod = null;                                       //销毁方法
    private AbstractBaseBeanDefine                useTemplate   = null;                                       //应用的模板
    //-------------------------------------------------------------
    /**返回bean的唯一编号，如果没有指定id属性则id值将是fullName属性值。*/
    public String getID() {
        if (this.id == null)
            this.id = this.getFullName();
        return this.id;
    };
    /**返回bean的名称，如果指定了package属性那么name的值可以出现重复。*/
    public String getName() {
        return this.name;
    };
    /**获取Bean的逻辑包定义，这个包定义与类的实际所处包不同。它表现为一个外在的逻辑管理形式。*/
    public String getPackage() {
        return this.logicPackage;
    };
    public String getFullName() {
        if (this.logicPackage != null)
            return this.logicPackage + "." + this.name;
        else
            return this.getName();
    }
    /**属性注入所使用的注入方式，Fact，Ioc，User*/
    public String getIocEngine() {
        return this.iocEngine;
    }
    /**设置注入所使用的注入方式，Fact，Ioc，User*/
    public void setIocEngine(String iocEngine) {
        this.iocEngine = iocEngine;
    }
    /**返回一个boolean值，表示类是否为一个抽象类。*/
    public boolean isAbstract() {
        return this.boolAbstract;
    };
    /**返回一个boolean值，表示这个bean是否为单态的。*/
    public boolean isSingleton() {
        return this.boolSingleton;
    };
    /**返回一个boolean值，表示这个bean是否为延迟装载的。*/
    public boolean isLazyInit() {
        return this.boolLazyInit;
    };
    /**是否要求强制类型检查*/
    public boolean isCheck() {
        return this.boolCheckType;
    };
    /**返回bean的描述信息。*/
    public String getDescription() {
        return this.description;
    };
    /**获取工厂bean。*/
    public AbstractBeanDefine factoryBean() {
        return this.factoryBean;
    };
    /**该方法与factoryName()方法是成对出现的，该方法表明目标方法的代理名称。*/
    public AbstractMethodDefine factoryMethod() {
        return this.factoryMethod;
    };
    /**获取方法的定义，如果当前定义中没有声明则自动到使用的模板中查找。依次类推直到模板返回为空。*/
    public AbstractMethodDefine getMethod(String name) {
        AbstractMethodDefine md = this.getDeclaredMethod(name);
        if (md == null && this.useTemplate != null)
            return this.useTemplate.getMethod(name);
        return md;
    };
    /**获取方法的定义，该方法只会在当前定义中查找。*/
    public AbstractMethodDefine getDeclaredMethod(String name) {
        return this.methods.get(name);
    };
    /**获取当前定义中可用的方法声明集合。*/
    public Collection<? extends AbstractMethodDefine> getMethods() {
        HashMap<String, AbstractMethodDefine> ms = new HashMap<String, AbstractMethodDefine>();
        ms.putAll(this.methods);
        if (this.useTemplate != null)
            for (AbstractMethodDefine m : this.useTemplate.getMethods())
                if (ms.containsKey(m.getName()) == false)
                    ms.put(m.getName(), m);
        return Collections.unmodifiableCollection((Collection<AbstractMethodDefine>) ms.values());
    };
    /**获取当前定义中声明的方法列表，返回的结果不包括使用的模板中的方法声明。*/
    public Collection<? extends AbstractMethodDefine> getDeclaredMethods() {
        return Collections.unmodifiableCollection((Collection<AbstractMethodDefine>) this.methods.values());
    };
    /**获取初始化方法名。*/
    public String getInitMethod() {
        return this.initMethod;
    };
    /**获取销毁方法名。*/
    public String getDestroyMethod() {
        return this.destroyMethod;
    };
    /**获取bean使用的模板。*/
    public AbstractBaseBeanDefine getUseTemplate() {
        return this.useTemplate;
    };
    /**
     * 该属性定义了当创建这个bean时候需要的启动参数。
     * 启动参数通常是指构造方法参数，对于工厂形式创建启动参数代表了工厂方法的参数列表。
     * 返回的集合是一个只读集合。
     */
    public Collection<ConstructorDefine> getInitParams() {
        return Collections.unmodifiableCollection((Collection<ConstructorDefine>) this.initParams);
    };
    /**返回bean的定义属性集合，返回的集合是一个只读集合。*/
    public Collection<PropertyDefine> getPropertys() {
        return Collections.unmodifiableCollection((Collection<PropertyDefine>) this.propertys.values());
    };
    /**返回具有特征的字符串。*/
    public String toString() {
        return this.getClass().getSimpleName() + "@" + this.hashCode() + " name=" + this.getName();
    };
    /**添加一个启动参数，被添加的启动参数会自动进行排序。*/
    public void addInitParam(ConstructorDefine constructorParam) {
        if (constructorParam.getIndex() == -1)
            constructorParam.setIndex(this.initParams.size());
        this.initParams.add(constructorParam);
        final AbstractBaseBeanDefine define = this;
        Collections.sort(this.initParams, new Comparator<ConstructorDefine>() {
            public int compare(ConstructorDefine arg0, ConstructorDefine arg1) {
                int cdefine_1 = arg0.getIndex();
                int cdefine_2 = arg1.getIndex();
                if (cdefine_1 > cdefine_2)
                    return 1;
                else if (cdefine_1 < cdefine_2)
                    return -1;
                else
                    throw new RepeateException(define + "[" + arg0 + "]与[" + arg1 + "]构造参数索引重复.");
            }
        });
    };
    /**添加一个属性。*/
    public void addProperty(PropertyDefine property) {
        this.propertyNames.add(property.getName());
        this.propertys.put(property.getName(), property);
    };
    /**添加一个方法描述。*/
    public void addMethod(MethodDefine method) {
        this.methodNames.add(method.getName());
        this.methods.put(method.getName(), method);
    };
    //-------------------------------------------------------------
    /**设置id。*/
    public void setId(String id) {
        this.id = id;
    }
    /**设置Bean名。*/
    public void setName(String name) {
        this.name = name;
    }
    /**设置逻辑包名称*/
    public void setLogicPackage(String logicPackage) {
        this.logicPackage = logicPackage;
    }
    /**设置描述信息。*/
    public void setDescription(String description) {
        this.description = description;
    }
    public void setFactoryBean(AbstractBeanDefine factoryBean) {
        this.factoryBean = factoryBean;
    }
    /**设置创建该Bean时使用的工厂bean的方法描述。*/
    public void setFactoryMethod(AbstractMethodDefine factoryMethod) {
        this.factoryMethod = factoryMethod;
    }
    /**设置该bean是否为一个抽象的。*/
    public void setBoolAbstract(boolean boolAbstract) {
        this.boolAbstract = boolAbstract;
    }
    /**设置该bean是否为一个单态的。*/
    public void setBoolSingleton(boolean boolSingleton) {
        this.boolSingleton = boolSingleton;
    }
    /**设置该bean是否为一个延迟初始化的。*/
    public void setBoolLazyInit(boolean boolLazyInit) {
        this.boolLazyInit = boolLazyInit;
    }
    /**设置是否执行强制检查。*/
    public void setBoolCheckType(boolean boolCheckType) {
        this.boolCheckType = boolCheckType;
    }
    /**设置bean初始化方法。*/
    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }
    /**设置bean销毁方法。*/
    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod = destroyMethod;
    }
    /**设置bean使用的模板。*/
    public void setUseTemplate(AbstractBaseBeanDefine useTemplate) {
        this.useTemplate = useTemplate;
    }
}