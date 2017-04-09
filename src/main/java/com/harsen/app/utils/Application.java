package com.harsen.app.utils;

import com.harsen.app.utils.annotation.AnnotationUtils;
import com.harsen.app.utils.annotation.Command;
import com.harsen.app.utils.annotation.Control;
import com.harsen.app.utils.annotation.Param;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author HarsenLin
 * @date 2017/4/9 16:23
 */
public class Application {
    /** 命令扫描器 */
    private Scanner sc = new Scanner(System.in);
    /** 执行类 */
    private Class execClz = null;
    /** 所有插件 */
    private Map<String, Class> controls;
    private String pluginRoot = "plugins";

    // 单例
    private static Application instance = null;
    private Application(){
        // 获取所有插件对象
        controls = AnnotationUtils.getControl(pluginRoot, true);
    }

    public static void main(String[] args) {
        instance = new Application();

        instance.help();
    }

    public void help(){
        StringBuilder sb = new StringBuilder();
        for(Method m : AnnotationUtils.getCommand(getClass())){
            Command methodAnn = m.getAnnotation(Command.class);
            sb.append(methodAnn.tip()).append("：").append(methodAnn.name());
            for (Annotation[] as : m.getParameterAnnotations()){
                for (Annotation a : as){
                    if(a instanceof Param){
                        Param p = (Param)a;
                        sb.append(" [").append(p.name()).append("：").append(p.tip()).append("]");
                    }
                }
            }
            sb.append("\t|\t");
        }
        sb.setLength(sb.length()-3);
        System.out.println(sb.toString());
        sb.setLength(0);

        if(null == execClz) return;
        for (Method m : AnnotationUtils.getCommand(execClz)){
            Command methodAnn = m.getAnnotation(Command.class);
            sb.append(methodAnn.tip()).append("：").append(methodAnn.name());
            for (Annotation[] as : m.getParameterAnnotations()){
                for (Annotation a : as){
                    if(a instanceof Param){
                        Param p = (Param)a;
                        sb.append(" [").append(p.name()).append("：").append(p.tip()).append("]");
                    }
                }
            }
            System.out.println(sb.toString());
            sb.setLength(0);
        }
    }

    /**
     * 查看所有插件
     */
    @Command(name = "ls", tip = "查看所有插件")
    public static void ls(){
        int rootNameLen = instance.pluginRoot.length() + 1;
        StringBuilder sb = new StringBuilder();
        for (Class c : instance.controls.values()){
            Control control = (Control)c.getAnnotation(Control.class);
            sb.append(control.tip()).append("：").append(c.getName().substring(rootNameLen)).append("\t|\t");
        }
        sb.setLength(sb.length() - 3);
        System.out.println(sb.toString());
    }

    /**
     * 执行命令
     * @param command 命令
     */
    private void execute(String command) throws IllegalAccessException, InvocationTargetException {
        // 基础命令
        for(Method m : AnnotationUtils.getCommand(Application.class)){
            if (m.getName().equals(command)){
                m.invoke(null, getArgs(m));
                return;
            }
        }
        // 插件方法
        if(null != execClz){
            for(Method m : AnnotationUtils.getCommand(execClz)){
                if (m.getName().equals(command)){
                    m.invoke(null, getArgs(m));
                    return;
                }
            }
        }
        System.out.println("找不到命令：" + command);
    }

    /**
     * 获取方法参数
     * @param m 执行方法
     * @return List
     */
    private Object[] getArgs(Method m){
        // 获取参数
        List<Object> args = new ArrayList<Object>();
        for (Class param : m.getParameterTypes()){
            if(param.isArray()){
                sc.useDelimiter("\n");
                args.add(sc.next().trim().split(" "));
                sc.reset();
            }else{
                args.add(sc.next());
            }
        }
        return args.toArray();
    }
}
