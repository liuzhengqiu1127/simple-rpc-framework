package com.github.liyue2008.rpc.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.liyue2008.rpc.serialize.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class JSONSerializer implements Serializer<JSONObject> {

    @Override
    public int size(JSONObject entry) {
        return Integer.BYTES + entry.size();
    }

    @Override
    public void serialize(JSONObject entry, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
        int len = entry.size();
        byte[] resultBytes = entry.toJSONString().getBytes(StandardCharsets.UTF_8);
        buffer.putInt(len);
        buffer.put(resultBytes);
    }

    @Override
    public JSONObject parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
        int len = buffer.getInt();
        byte[] result = new byte[len];
        buffer.get(result);
        return JSON.parseObject(new String(result,StandardCharsets.UTF_8));
    }

    @Override
    public byte type() {
        return Types.TYPE_JSON_OBJECT;
    }

    @Override
    public Class<JSONObject> getSerializeClass() {
        return JSONObject.class;
    }
}
