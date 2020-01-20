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

import com.github.liyue2008.rpc.transport.Transport;
import com.itranswarp.compiler.JavaStringCompiler;


import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/9/27
 */
public class DynamicStubFactory implements StubFactory{
    private final static String STUB_CLASS_TEMPLATE =
            "package com.github.liyue2008.rpc.client.stubs;\n" +
                    "import com.github.liyue2008.rpc.serialize.SerializeSupport;\n" +
                    "import com.github.liyue2008.rpc.client.stubs.RpcRequestArgs;\n" +
                    "\n" +
                    "public class %s extends AbstractStub implements %s {\n" +
                    "\n" +
                    "%s" +
                    "}";
    private final static String STUB_METHOD_TEMPLATE =
            "    @Override\n" +
                    "    public %s %s(%s) {\n" +
                    "        RpcRequestArgs args = new RpcRequestArgs(%s);\n" +
                    "%s" +
                    "        return SerializeSupport.parse(\n" +
                    "                invokeRemote(\n" +
                    "                        new RpcRequest(\n" +
                    "                                \"%s\",\n" +
                    "                                \"%s\",\n" +
                    "                                SerializeSupport.serialize(args)\n" +
                    "                        )\n" +
                    "                )\n" +
                    "        );\n" +
                    "    }\n";
    private final static String STUB_ARG_TEMPLATE =
            "        if (%s != null) {\n" +
                    "            args.addClass(%s.class);\n" +
                    "            args.addObj(%s);\n" +
                    "        }\n";

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            // 填充模板
            String stubSimpleName = serviceClass.getSimpleName() + "Stub";
            String classFullName = serviceClass.getName();
            String stubFullName = "com.github.liyue2008.rpc.client.stubs." + stubSimpleName;

            StringBuilder methodTemplate = new StringBuilder();
            for (Method method : serviceClass.getMethods()){
                String methodName = method.getName();
                String methodReturnName = method.getReturnType().getName();
                Class<?>[] args = method.getParameterTypes();
                StringBuilder argsStr = new StringBuilder();
                StringBuilder argsTemplateStr = new StringBuilder();
                for (int i = 0; i < args.length; i++){
                    String argType = args[i].getName();
                    String argName = "arg"+i;
                    argsStr.append(argType).append(" ").append(argName);
                    if (i < args.length - 1){
                        argsStr.append(", ");
                    }
                    argsTemplateStr.append(String.format(STUB_ARG_TEMPLATE,argName,argType, argName));
                }
                methodTemplate.append(String.format(STUB_METHOD_TEMPLATE,methodReturnName,methodName,argsStr.toString(),
                        args.length, argsTemplateStr.toString(),classFullName, methodName));
            }
            String source = String.format(STUB_CLASS_TEMPLATE,stubSimpleName,classFullName,methodTemplate.toString());

            // 编译源代码
            JavaStringCompiler compiler = new JavaStringCompiler();
            Map<String, byte[]> results = compiler.compile(stubSimpleName + ".java", source);
            // 加载编译好的类
            Class<?> clazz = compiler.loadClass(stubFullName, results);

            // 把Transport赋值给桩
            ServiceStub stubInstance = (ServiceStub) clazz.newInstance();
            stubInstance.setTransport(transport);
            // 返回这个桩
            return (T) stubInstance;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
