package com.loadtestgo.script.engine.internal.rhino;

import com.google.common.collect.Sets;
import org.mozilla.javascript.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Sandbox Rhino script access to only our allowed functions/classes.
 *
 * The ClassShutter is used for only allowing direct access to a fixed set of
 * Java classes.
 *
 * Each object is also wrapped so that the methods on Object are not exposed.
 * This makes the class look cleaner to the JavaScript user if they iterate
 * the properties.  Perhaps more importantly it denies access to the class
 * object as well, as this can be used to create arbitrary classes.
 */
public class RhinoContextFactory extends ContextFactory {
    private boolean sandboxed = false;

    /**
     * Disable access to these methods for all Java objects
     */
    private static final Set<String> DENY_CLASS_MEMBERS = Sets.newHashSet(
        "class", "clone", "equals", "finalize", "getClass", "hashCode",
        "notify", "notifyAll", "wait");

    /**
     * Allow JavaScript direct access to these class interfaces
     */
    private static final Set<String> ALLOW_CLASSES = Sets.newHashSet(

        // Allow scripts to manipulate basic types
        Object.class.getName(),
        Boolean.class.getName(),
        Double.class.getName(),
        Float.class.getName(),
        Integer.class.getName(),
        Long.class.getName(),
        Short.class.getName(),
        String.class.getName(),

        // Allow scripts to catch and use Rhino Exceptions
        EcmaError.class.getName(),
        EvaluatorException.class.getName(),
        JavaScriptException.class.getName(),
        WrappedException.class.getName(),
        RhinoException.class.getName(),

        // Select parts of java.util
        ArrayList.class.getName(),
        HashMap.class.getName(),
        Date.class.getName(),

        // Allow access to our APIs
        "com.loadtestgo.script.api.*",
        "com.loadtestgo.api.*",
        "com.loadtestgo.script.engine.internal.api.*");

    @Override
    protected Context makeContext() {
        Context context = super.makeContext();

        // Add a Rhino ClassShutter to disable access to most classes
        if (sandboxed) {
            context.setClassShutter(new ClassShutter() {
                @Override
                public boolean visibleToScripts(String fullClassName) {
                    if (ALLOW_CLASSES.contains(fullClassName)) {
                        return true;
                    }

                    for (int dot = fullClassName.length(); (dot = fullClassName.lastIndexOf('.', dot - 1)) >= 0; ) {
                        if (ALLOW_CLASSES.contains(fullClassName.substring(0, dot + 1) + "*")) {
                            return true;
                        }
                    }

                    return false;
                }
            });
        }

        // Control how Java objects are accessed from the JavaScript engine.

        WrapFactory wrapFactory = new WrapFactory() {
            @Override
            public Object wrap(Context cx, Scriptable scope, Object javaObject, Class staticType) {
                // Deny reflective access up front.  This should not be triggered due
                // to getter filtering, but let's be paranoid.
                if (sandboxed) {
                    if (javaObject != null) {
                        if (javaObject instanceof Class) {
                            return Context.getUndefinedValue();
                        } else if (javaObject instanceof ClassLoader) {
                            return Context.getUndefinedValue();
                        } else {
                            Package packageObj = javaObject.getClass().getPackage();
                            if (packageObj != null) {
                                if ("java.lang.reflect".equals(packageObj.getName())) {
                                    return Context.getUndefinedValue();
                                }
                            }
                        }
                    }
                }

                // Make Java arrays behave like native JavaScript arrays.
                // This breaks EQ, but is better than the alternative.
                if (javaObject instanceof Object[]) {
                    Object[] javaArray = (Object[]) javaObject;
                    int n = javaArray.length;
                    Object[] wrappedElements = new Object[n];
                    Class<?> compType = javaArray.getClass().getComponentType();

                    for (int i = n; --i >= 0;) {
                        wrappedElements[i] = wrap(cx, scope, javaArray[i], compType);
                    }

                    NativeArray jsArray = new NativeArray(wrappedElements);
                    jsArray.setPrototype(ScriptableObject.getClassPrototype(scope, "Array"));
                    jsArray.setParentScope(scope);
                    return jsArray;
                }

                return super.wrap(cx, scope, javaObject, staticType);
            }

            @Override
            public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
                return new NativeJavaObject(scope, javaObject, staticType) {
                    @SuppressWarnings("unchecked")
                    protected void initMembers() {
                        super.initMembers();
                        // Remove member functions from Java that we don't like
                        // to show up in list of properties on the JavaScript object
                        try {
                            Class c = Class.forName("org.mozilla.javascript.JavaMembers");
                            Field field = c.getDeclaredField("members");
                            field.setAccessible(true);

                            // Remove members we want to hide
                            Map<String,Object> members = (Map<String,Object>)field.get(this.members);
                            for (String name : DENY_CLASS_MEMBERS) {
                                members.remove(name);
                            }
                        } catch (ClassNotFoundException e) {
                            // Ignore
                        } catch (NoSuchFieldException e) {
                            // Ignore
                        } catch (IllegalAccessException e) {
                            // Ignore
                        }
                    }

                    @Override
                    public Object get(String name, Scriptable start) {
                        // Deny access to base class members that allow access
                        // to the class loader.
                        if (DENY_CLASS_MEMBERS.contains(name)) {
                            return NOT_FOUND;
                        }

                        return super.get(name, start);
                    }
                };
            }
        };

        wrapFactory.setJavaPrimitiveWrap(true);
        context.setWrapFactory(wrapFactory);

        // Run as ES6, only a few ES6 features are available right now
        context.setLanguageVersion(Context.VERSION_ES6);

        return context;
    }

    @Override
    public boolean hasFeature(Context c, int feature) {
        switch (feature) {
            case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
                return false;
            case Context.FEATURE_E4X:
                return false;
            case Context.FEATURE_TO_STRING_AS_SOURCE:
                return true;
            default:
                return super.hasFeature(c, feature);
        }
    }

    public void setSandboxed(boolean sandboxed) {
        this.sandboxed = sandboxed;
    }
}
