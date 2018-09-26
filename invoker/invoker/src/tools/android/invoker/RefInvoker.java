package tools.android.invoker;

import android.content.ActivityNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RefInvoker {

    private static final ClassLoader system = ClassLoader.getSystemClassLoader();
    private static final ClassLoader bootloader = system.getParent();
    private static final ClassLoader application = RefInvoker.class.getClassLoader();

    private static HashMap<String, Class> clazzCache = new HashMap<String, Class>();

    public static Class forName(String clazzName) throws ClassNotFoundException {
        Class clazz = clazzCache.get(clazzName);
        if (clazz == null) {
            clazz = Class.forName(clazzName);
            ClassLoader cl = clazz.getClassLoader();
            if (cl == system || cl == application || cl == bootloader) {
                clazzCache.put(clazzName, clazz);
            }
        }
        return clazz;
    }

    public static Object newInstance(String className, Class[] paramTypes, Object[] paramValues) {
        try {
            Class clazz = forName(className);
            Constructor constructor = clazz.getConstructor(paramTypes);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(paramValues);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object target, String className, String methodName, Class[] paramTypes,
                                      Object[] paramValues) {
        try {
            Class clazz = forName(className);
            return invokeMethod(target, clazz, methodName, paramTypes, paramValues);
        } catch (ClassNotFoundException e) {
            new ClassNotFoundException(e.getMessage());
        } catch (ActivityNotFoundException e) {
            new ActivityNotFoundException(e.getMessage());
        }
        return null;
    }

    public static Object invokeMethod(Object target, Class clazz, String methodName, Class[] paramTypes,
                                      Object[] paramValues) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(target, paramValues);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof ActivityNotFoundException) {
                throw new ActivityNotFoundException(e.getTargetException().getMessage());
            } else if (e.getTargetException() != null && e.getTargetException().getCause() instanceof ActivityNotFoundException) {
                throw new ActivityNotFoundException(e.getTargetException().getCause().getMessage());
            } else if (e.getCause() instanceof ActivityNotFoundException) {
                throw new ActivityNotFoundException(e.getCause().getMessage());
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getField(Object target, String className, String fieldName) {
        try {
            Class clazz = forName(className);
            return getField(target, clazz, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Object getField(Object target, Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // try supper for Miui, Miui has a class named MiuiPhoneWindow
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    @SuppressWarnings("rawtypes")
    public static void setField(Object target, String className, String fieldName, Object fieldValue) {
        try {
            Class clazz = forName(className);
            setField(target, clazz, fieldName, fieldValue);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setField(Object target, Class clazz, String fieldName, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, fieldValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            try {
                Field field = clazz.getSuperclass().getDeclaredField(fieldName);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(target, fieldValue);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Method findMethod(Object object, String methodName, Class[] paramClasses) {
        try {
            return object.getClass().getDeclaredMethod(methodName, paramClasses);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method findMethod(Object object, String methodName, Object[] params) {
        if (params == null) {
            try {
                return object.getClass().getDeclaredMethod(methodName, (Class[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            Method[] methods = object.getClass().getDeclaredMethods();
            boolean isFound = false;
            Method method = null;
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == params.length) {
                        isFound = true;
                        for (int i = 0; i < params.length; i++) {
                            if (!(types[i] == params[i].getClass() || (types[i].isPrimitive() && primitiveToWrapper(types[i]) == params[i].getClass()))) {
                                isFound = false;
                                break;
                            }
                        }
                        if (isFound) {
                            method = m;
                            break;
                        }
                    }
                }
            }
            return method;
        }
    }

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    public static ArrayList dumpAllInfo(String className) {
        try {
            Class clazz = Class.forName(className);
            return dumpAllInfo(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList dumpAllInfo(Class clazz) {
        ArrayList arrayList = new ArrayList();

        Log.i("clazz=" + clazz.getName());
        Log.i("Superclass=" + clazz.getSuperclass());

        Constructor[] ctors = clazz.getDeclaredConstructors();
        if (ctors != null) {
            Log.w("DeclaredConstructors--------------------" + ctors.length);
            for (Constructor c : ctors) {
                Log.i(c);
                arrayList.add(c);
            }
        }

        Constructor[] publicCtors = clazz.getConstructors();
        if (publicCtors != null) {
            Log.w("Constructors-------------------------" + publicCtors.length);
            for (Constructor c : publicCtors) {
                Log.i(c);
                arrayList.add(c);
            }
        }

        Method[] mtds = clazz.getDeclaredMethods();
        if (mtds != null) {
            Log.w("DeclaredMethods-------------------------" + mtds.length);
            for (Method m : mtds) {
                Log.i(m);
                arrayList.add(m);
            }
        }

        Method[] mts = clazz.getMethods();
        if (mts != null) {
            Log.w("Methods-------------------------" + mts.length);
            for (Method m : mts) {
                Log.i(m);
                arrayList.add(m);
            }
        }

        Class<?>[] inners = clazz.getDeclaredClasses();
        if (inners != null) {
            Log.w("DeclaredClasses-------------------------" + inners.length);
            for (Class c : inners) {
                Log.i(c.getName());
                arrayList.add(c.getName());
            }
        }

        Class<?>[] classes = clazz.getClasses();
        if (classes != null) {
            Log.w("classes-------------------------" + classes.length);
            for (Class c : classes) {
                Log.i(c.getName());
                arrayList.add(c.getName());
            }
        }

        Field[] dfields = clazz.getDeclaredFields();
        if (dfields != null) {
            Log.w("DeclaredFields-------------------------" + dfields.length);
            for (Field f : dfields) {
                Log.i(f);
                arrayList.add(f);
            }
        }

        Field[] fields = clazz.getFields();
        if (fields != null) {
            Log.w("Fields-------------------------" + fields.length);
            for (Field f : fields) {
                Log.i(f);
                arrayList.add(f);
            }
        }

        Annotation[] anns = clazz.getAnnotations();
        if (anns != null) {
            Log.w("Annotations-------------------------" + anns.length);
            for (Annotation an : anns) {
                Log.i(an);
                arrayList.add(an);
            }
        }
        return arrayList;
    }

    public static ArrayList dumpAllInfo(Object object) {
        Class clazz = object.getClass();
        return dumpAllInfo(clazz);
    }

    static class Log {

        static String TAG = "INVOKER";
        static final int stackLevel = 4;

        public interface LogHandler {
            void publish(String tag, int level, String message);
        }

        public static LogHandler DEFAULT_LOGHANDLER = new LogHandler() {
            @Override
            public void publish(String tag, int level, String message) {
                android.util.Log.println(level, tag, message);
            }
        };

        public static void i(String msg) {
            android.util.Log.i(TAG, msg);
        }

        public static void d(String msg) {
            android.util.Log.d(TAG, msg);
        }

        public static void w(String msg) {
            android.util.Log.w(TAG, msg);
        }

        public static void i(Object... msg) {
            printLog(android.util.Log.INFO, msg);
        }

        public static void d(Object... msg) {
            printLog(android.util.Log.DEBUG, msg);
        }

        public static void w(Object... msg) {
            printLog(android.util.Log.WARN, msg);
        }

        static void printLog(int level, Object... msg) {
            StringBuilder str = new StringBuilder();
            if (msg != null) {
                for (Object obj : msg) {
                    str.append("|").append(obj);
                }
                if (str.length() > 0) {
                    str.deleteCharAt(0);
                }
            } else {
                str.append("null");
            }
            try {
                StackTraceElement[] sts = Thread.currentThread().getStackTrace();
                StackTraceElement st = null;
                String tag = null;
                if (sts != null && sts.length > stackLevel) {
                    st = sts[stackLevel];
                    if (st != null) {
                        String fileName = st.getFileName();
                        tag = (fileName == null) ? "Unkown" : fileName.replace(".java", "");
                        str.insert(0, "|" + tag + "." + st.getMethodName() + "() line " + st.getLineNumber());
                    }
                }
                tag = (tag == null) ? TAG : (TAG + tag);
                while (str.length() > 0) {
                    DEFAULT_LOGHANDLER.publish(tag, level, str.substring(0, Math.min(2000, str.length())).toString());
                    str.delete(0, 2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}