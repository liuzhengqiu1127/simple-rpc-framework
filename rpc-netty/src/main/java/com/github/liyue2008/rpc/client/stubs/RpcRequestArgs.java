package com.github.liyue2008.rpc.client.stubs;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RpcRequestArgs {
    private List<Class<?>> argClasses;
    private List<Object> args;
    public RpcRequestArgs(int capacity){
        this.argClasses = new ArrayList<>(capacity);
        this.args = new ArrayList<>(capacity);
    }
    public void addClass(Class<?> clazz){
        this.argClasses.add(clazz);
    }
    public void addObj(Object object){
        this.args.add(object);
    }
}
