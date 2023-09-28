package com.lue.rasp.hook.http;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.lue.rasp.config.HookConfig;
import com.lue.rasp.context.RequestContextHolder;
import com.lue.rasp.utils.InterfaceProxyUtils;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@MetaInfServices(Module.class)
@Information(id = "rasp-http-hook" , author = "1ue" , version = "0.0.6")
public class HttpHook implements Module, ModuleLifecycle {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    public void hookRequest() {

        new EventWatchBuilder(moduleEventWatcher)
                .onClass("javax.servlet.http.HttpServlet")
                .includeSubClasses()
                .onBehavior("service")
                .withParameterTypes(
                        "javax.servlet.http.HttpServletRequest",
                        "javax.servlet.http.HttpServletResponse"
                ).onWatch(new AdviceListener() {
                    @Override
                    protected void before(Advice advice) throws Throwable {
                        // 只关心顶层调用
                        if (!advice.isProcessTop()) {
                            return;
                        }
                        System.out.println("hook到HttpServlet的service方法");
                        // jvm-sandbox 是在独立的 ClassLoader 中运行的，因此需要做一层代理
                        HttpServletRequest request = InterfaceProxyUtils.puppet(HttpServletRequest.class, advice.getParameterArray()[0]);
                        HttpServletResponse response = InterfaceProxyUtils.puppet(HttpServletResponse.class, advice.getParameterArray()[1]);
//                        System.out.println(request);
//                        System.out.println(response);
                        // 添加请求上下文
                        RequestContextHolder.set(new RequestContextHolder.Context(request, response));
                        super.before(advice);
                    }

                    @Override
                    protected void afterReturning(Advice advice) {
                        // 只关心顶层调用
                        if (!advice.isProcessTop()) {
                            return;
                        }
                        // 移除请求上下文
                        System.out.println("移除请求上下文");
                        RequestContextHolder.remove();
                    }

                });

    }

    @Override
    public void onLoad() throws Throwable {

    }

    @Override
    public void onUnload() throws Throwable {
        System.out.println("rasp的HTTP-HOOK卸载！！！");
    }

    @Override
    public void onActive() throws Throwable {
        System.out.println("rasp的HTTP-HOOK激活！！！");
    }

    @Override
    public void onFrozen() throws Throwable {

    }

    @Override
    public void loadCompleted() {
        if (HookConfig.isEnable("http")) {
            hookRequest();
        }
        System.out.println("rasp的RCE-HOOK加载完成！！！");
    }
}
