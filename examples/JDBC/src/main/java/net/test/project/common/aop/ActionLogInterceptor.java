/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.test.project.common.aop;
import javax.servlet.http.HttpServletRequest;
import net.hasor.core.ApiBinder;
import net.hasor.core.Hasor;
import net.hasor.core.plugin.HasorPlugin;
import net.hasor.core.plugin.Plugin;
import net.hasor.plugins.aop.matchers.AopMatchers;
import net.hasor.plugins.controller.Controller;
import net.hasor.plugins.controller.interceptor.ControllerInterceptor;
import net.hasor.plugins.controller.interceptor.ControllerInvocation;
/**
 * 全局 Action 调用日志记录 
 * @version : 2013-12-23
 * @author 赵永春(zyc@hasor.net)
 */
@Plugin
public class ActionLogInterceptor implements HasorPlugin {
    public void loadPlugin(ApiBinder apiBinder) {
        apiBinder.getGuiceBinder().bindInterceptor(AopMatchers.annotatedWith(Controller.class),//
                AopMatchers.any(), new AL_Interceptor());
    }
}
class AL_Interceptor extends ControllerInterceptor {
    public Object invoke(ControllerInvocation invocation) throws Throwable {
        try {
            HttpServletRequest reqest = invocation.getRequest();
            Hasor.logInfo("调用 Action :%s.", reqest.getRequestURI());
            return invocation.proceed();
        } catch (Exception e) {
            throw e;
        }
    }
}