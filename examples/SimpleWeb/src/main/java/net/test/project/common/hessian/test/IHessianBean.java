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
package net.test.project.common.hessian.test;
import net.test.project.common.hessian.HessianClient;
/**
 * 
 * @version : 2013-12-25
 * @author ������(zyc@hasor.net)
 */
@HessianClient("http://127.0.0.1:8082/testBean")
public interface IHessianBean {
    public long sayHello();
}