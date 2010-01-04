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
package org.more.submit.casing.spring;
import java.io.File;
import java.io.IOException;
import org.more.submit.ActionContext;
import org.more.submit.CasingBuild;
import org.more.submit.Config;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
/**
 * ClientSpringBuilder提供了beans软件包为容器的submit支撑环境，并且提供了一些常见创建方法。<br/>
 * 如果不指定配置文件名称ClientSpringBuilder会自动在当前路径下寻找名称为applicationContext.xml的配置文件。<br/>
 * init：<br/>
 * 参数beanFactory优先级：高，AbstractApplicationContext类型对象。<br/>
 * 参数configFile优先级：底，String类型对象。
 * <br/>Date : 2009-11-21
 * @author 赵永春
 */
public class ClientSpringBuilder extends CasingBuild {
    //========================================================================================Field
    protected AbstractApplicationContext springContext = null;
    //==================================================================================Constructor
    /**创建ClientSpringBuilder，同时初始化ClientSpringBuilder对象使用默认配置文件名为applicationContext.xml其文件保存在当前路径下。*/
    public ClientSpringBuilder() throws IOException {
        this.init(new File("applicationContext.xml"));
    }
    /**
     * 创建ClientSpringBuilder，但是通过isInit参数决定是否初始化ClientSpringBuilder对象。true表示创建并且初始化,false表示仅创建。<br/>
     * 使用默认配置文件名为applicationContext.xml其文件保存在当前路径下。
     */
    public ClientSpringBuilder(boolean isInit) throws IOException {
        if (isInit == true)
            this.init(new File("applicationContext.xml"));
    }
    /**创建ClientSpringBuilder，通过configFile参数来决定使用那个配置文件初始化ClientSpringBuilder对象。*/
    public ClientSpringBuilder(String configFile) throws IOException {
        this.init(new File(configFile));
    }
    /**创建ClientSpringBuilder，通过configFile参数来决定使用那个配置文件初始化ClientSpringBuilder对象。*/
    public ClientSpringBuilder(File configFile) throws IOException {
        this.init(configFile);
    }
    /**提供一个更高级的方式创建ClientSpringBuilder对象，该构造方法将使用指定的AbstractApplicationContext类型对象作为创建ActionContext而使用的数据源。*/
    public ClientSpringBuilder(AbstractApplicationContext springContext) {
        this.init(springContext);
    }
    //==========================================================================================Job
    protected void init(File configFile) throws IOException {
        if (configFile.exists() == false || configFile.canRead() == false)
            throw new IOException("配置文件[" + configFile.getAbsolutePath() + "]不存在，或者无法读取。");
        this.springContext = new FileSystemXmlApplicationContext(configFile.getAbsolutePath());
    }
    private void init(AbstractApplicationContext springContext) {
        if (springContext == null)
            throw new NullPointerException("springContext不能为空。");
        this.springContext = springContext;
    }
    /**该方法紧当使用ClientSpringBuilder构造方法创建对象时有效。*/
    @Override
    public void init(Config config) throws Exception {
        //参数beanFactory优先级：高
        //参数configFile优先级：底
        super.init(config);
        //检测beanFactory参数。
        Object beanFactory = config.getInitParameter("beanFactory");
        boolean noFactory = (beanFactory == null || beanFactory instanceof AbstractApplicationContext == false);//如果没有正确配置noFactory值为true；
        if (noFactory == false) {
            this.init((AbstractApplicationContext) beanFactory);
            return;
        }
        //检测configFile参数。
        Object configFile = config.getInitParameter("configFile");
        if (configFile != null)
            this.init(new File(configFile.toString()));
    }
    @Override
    protected ActionContext createActionContext() {
        return new SpringActionContext(this.springContext);
    }
}