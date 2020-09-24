/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.liyue2008.rpc.client;

import com.github.liyue2008.rpc.NameService;
import com.github.liyue2008.rpc.RpcAccessPoint;
import com.github.liyue2008.rpc.hello.HelloService;
import com.github.liyue2008.rpc.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * @author LiYue
 * Date: 2019/9/20
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String [] args) throws IOException {
        String serviceName = HelloService.class.getCanonicalName();
//        File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
//        File file = new File(tmpDirFile, "simple_rpc_name_service.data");
        URI nameServiceUri = URI.create("jdbc:mysql://localhost:3306/mytest?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT");
        System.setProperty("nameservice.jdbc.username", "root");
        System.setProperty("nameservice.jdbc.password", "******");
        String name = "Master MQ";
        try(RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) { // 获取启动服务实例
            NameService nameService = rpcAccessPoint.getNameService(nameServiceUri); // 获取注册中心
            assert nameService != null;
            URI uri = nameService.lookupService(serviceName); //根据服务名获取URI
            assert uri != null;
            logger.info("找到服务{}，提供者: {}.", serviceName, uri);
            HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class); //获取服务实例
            logger.info("请求服务, name: {}...", name);
            String response = helloService.hello(name); //实际进行调用
            logger.info("收到响应: {}.", response);
        }


    }
}
