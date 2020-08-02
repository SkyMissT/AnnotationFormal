package com.miss.apt_library;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Vola on 2020/8/2.
 */
public class BindViewTool {

    /**
     *  实现 view 绑定
     */
    public static void bind(Activity activity) {

        Class clazz = activity.getClass();
        try {
            Log.e("aa", "----" + clazz.getName());
            Class bindClass = Class.forName(clazz.getName() + "_BindView");
            Method method = bindClass.getMethod("bind", activity.getClass());
            method.invoke(bindClass.newInstance(), activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
