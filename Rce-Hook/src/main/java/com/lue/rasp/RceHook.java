package com.lue.rasp;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.ProcessController;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;

@MetaInfServices(Module.class)
@Information(id = "rasp-rce-hook" , author = "1ue" , version = "0.0.9")
public class RceHook implements Module, ModuleLifecycle{

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    
    public void checkRceCommand() {
        new EventWatchBuilder(moduleEventWatcher)
                .onClass(ProcessBuilder.class)
                .includeBootstrap()
                .onBehavior("start")
                .onWatch(new AdviceListener() {
                    @Override
                    protected void before(Advice advice) throws Throwable {
                        System.out.println("hook到ProcessBuilder的start方法");
                        // TODO 添加上下文
                        // 直接拦截
                        ProcessController.throwsImmediately(new RuntimeException("Block By RASP!!!"));
                        super.before(advice);
                    }
                });
    }

    @Override
    public void onLoad() throws Throwable {

    }

    @Override
    public void onUnload() throws Throwable {
        System.out.println("rasp的RCE-HOOK卸载！！！");
    }

    @Override
    public void onActive() throws Throwable {
        System.out.println("rasp的RCE-HOOK激活！！！");
    }

    @Override
    public void onFrozen() throws Throwable {

    }

    @Override
    public void loadCompleted() {
        checkRceCommand();
        System.out.println("安装rasp的RCE-HOOK完毕！！！");
    }

}
