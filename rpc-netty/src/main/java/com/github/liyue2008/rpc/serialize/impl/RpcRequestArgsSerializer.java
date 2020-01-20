package com.github.liyue2008.rpc.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.liyue2008.rpc.client.stubs.RpcRequestArgs;
import com.github.liyue2008.rpc.serialize.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RpcRequestArgsSerializer implements Serializer<RpcRequestArgs> {
    @Override
    public int size(RpcRequestArgs entry) {
        return Integer.BYTES + JSON.toJSONString(entry).getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void serialize(RpcRequestArgs entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
        byte[] entries = JSON.toJSONString(entry).getBytes(StandardCharsets.UTF_8);
        buffer.putInt(entries.length);
        buffer.put(entries);
    }

    @Override
    public RpcRequestArgs parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
        byte[] entries = new byte[buffer.getInt()];
        buffer.get(entries);
        JSONObject jsonObject = JSON.parseObject(new String(entries,StandardCharsets.UTF_8));
        List<String> argClasses = jsonObject.getObject("argClasses",List.class);
        List args = jsonObject.getObject("args",List.class);
        List<Class<?>> classes = argClasses.stream().map(clazz ->{
            try{
                return Class.forName(clazz);
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        List<Object> arg = IntStream.range(0,args.size())
                .mapToObj(index -> {
                    Object object = args.get(index);
                    if (object instanceof String) return object;
                    else return JSON.parseObject(JSON.toJSONString(object), classes.get(index));
                }).collect(Collectors.toList());
        RpcRequestArgs requestArgs = new RpcRequestArgs(arg.size());
        requestArgs.setArgClasses(classes);
        requestArgs.setArgs(arg);
        return requestArgs;
    }

    @Override
    public byte type() {
        return Types.TYPE_RPC_REQUEST_ARGS;
    }

    @Override
    public Class<RpcRequestArgs> getSerializeClass() {
        return RpcRequestArgs.class;
    }
}
