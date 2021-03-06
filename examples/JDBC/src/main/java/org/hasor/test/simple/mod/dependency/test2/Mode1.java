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
package org.hasor.test.simple.mod.dependency.test2;
import net.hasor.core.ApiBinder;
import net.hasor.core.ApiBinder.DependencySettings;
import net.hasor.core.AppContext;
import net.hasor.core.Module;
import net.hasor.core.context.AnnoModule;
/**
 * 
 * @version : 2013-7-27
 * @author 赵永春 (zyc@hasor.net)
 */
@AnnoModule()
public class Mode1 implements Module {
    public void init(ApiBinder apiBinder) {
        DependencySettings dep = apiBinder.dependency();
        /*弱依赖，目标模块即使没有成功启动也不影响当前模块*/
        dep.weak(Mode2.class);
        /*强依赖，当前模块的启动必须依靠目标模块*/
        dep.forced(Mode3.class);
        //
    }
    public void start(AppContext appContext) {
        //
        //        appContext.registerServices(....);
        //        appContext.registerServices(....);
    }
    public void stop(AppContext appContext) {
        //
        //        appContext.unRegisterServices(....);
        //        appContext.unRegisterServices(....);
    }
}