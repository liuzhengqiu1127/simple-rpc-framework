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
package com.github.liyue2008.rpc.server;

import com.github.liyue2008.rpc.NameService;
import com.github.liyue2008.rpc.RpcAccessPoint;
import com.github.liyue2008.rpc.hello.HelloService;
import com.github.liyue2008.rpc.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URI;

/**
 * @author LiYue
 * Date: 2019/9/20
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static void main(String [] args) throws Exception {

        String serviceName = HelloService.class.getCanonicalName();
//        File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
//        File file = new File(tmpDirFile, "simple_rpc_name_service.data");
        URI nameServiceUri = URI.create("jdbc:mysql://localhost:3306/mytest?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT");
        System.setProperty("nameservice.jdbc.username", "root");
        System.setProperty("nameservice.jdbc.password", "*******");
        HelloService helloService = new HelloServiceImpl();
        logger.info("创建并启动RpcAccessPoint...");
        try(RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class);//依赖于接口而不依赖于实现，类似@AutoWrite
            Closeable ignored = rpcAccessPoint.startServer()){ //Netty服务端启动
            NameService nameService = rpcAccessPoint.getNameService(nameServiceUri);//获取注册中心，同时进行连接，并创建了注册表
            assert nameService != null;
            logger.info("向RpcAccessPoint注册{}服务...", serviceName);
            URI uri = rpcAccessPoint.addServiceProvider(helloService, HelloService.class); //绑定服务名和服务实例，返回服务URI
            logger.info("服务名: {}, 向NameService注册...", serviceName);
            nameService.registerService(serviceName, uri);//注册服务名和URI的关系
            logger.info("开始提供服务，按任何键退出.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            logger.info("Bye!");
        }
    }

}
