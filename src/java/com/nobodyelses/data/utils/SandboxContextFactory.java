package com.nobodyelses.data.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

public class SandboxContextFactory extends ContextFactory {
    final SandboxShutter shutter;

    public SandboxContextFactory(final SandboxShutter shutter) {
        this.shutter = shutter;
    }

    @Override
    protected void observeInstructionCount(final Context cx, final int instructionCount) {
        final TimerContext timerContext = (TimerContext) cx;

        final long expires = timerContext.getExpires();
        if (expires == 0) return;

        final long startTime = timerContext.getStartTime();
        final long currentTime = System.currentTimeMillis();
        final long time = currentTime - startTime;
        if (time > expires) {
            throw new Error("Script execution timer expired.");
        }
    }

    @Override
    protected Context makeContext() {
        final TimerContext cx = new TimerContext();
        cx.setInstructionObserverThreshold(10000);

        cx.setWrapFactory(new SandboxWrapFactory());
        cx.setClassShutter(new ClassShutter() {
            private final Map<String, Boolean> nameToAccepted = new HashMap<String, Boolean>();

            @Override
            public boolean visibleToScripts(final String name) {
                final Boolean granted = this.nameToAccepted.get(name);

                if (granted != null) {
                    return granted.booleanValue();
                }

                Class<?> staticType;
                try {
                    staticType = Class.forName(name);
                } catch (final Exception exc) {
                    this.nameToAccepted.put(name, Boolean.FALSE);
                    return false;
                }

                final boolean grant = shutter.allowClassAccess(staticType);
                this.nameToAccepted.put(name, Boolean.valueOf(grant));
                return grant;
            }
        });
        return cx;
    }

    class SandboxWrapFactory extends WrapFactory {
        @Override
        public Scriptable wrapNewObject(final Context cx, final Scriptable scope, final Object obj) {
            this.ensureReplacedClass(scope, obj, null);

            return super.wrapNewObject(cx, scope, obj);
        }

        @Override
        public Object wrap(final Context cx, final Scriptable scope, final Object obj, final Class<?> staticType) {
            this.ensureReplacedClass(scope, obj, staticType);

            return super.wrap(cx, scope, obj, staticType);
        }

        @Override
        public Scriptable wrapAsJavaObject(final Context cx, final Scriptable scope, final Object javaObject, final Class<?> staticType) {
            final Class<?> type = this.ensureReplacedClass(scope, javaObject, staticType);

            return new NativeJavaObject(scope, javaObject, staticType) {
                private final Map<String, Boolean> instanceMethodToAllowed = new HashMap<String, Boolean>();

                @Override
                public Object get(final String name, final Scriptable scope) {
                    final Object wrapped = super.get(name, scope);

                    if (wrapped instanceof BaseFunction) {
                        final String id = type.getName() + "." + name;
                        Boolean allowed = this.instanceMethodToAllowed.get(id);

                        if (allowed == null) {
                            final boolean allow = shutter.allowMethodAccess(type, javaObject, name);
                            this.instanceMethodToAllowed.put(id, allowed = Boolean.valueOf(allow));
                        }

                        if (!allowed.booleanValue()) {
                            return NOT_FOUND;
                        }
                    } else {
                        // NativeJavaObject + only boxed primitive types?
                        if (!shutter.allowFieldAccess(type, javaObject, name)) {
                            return NOT_FOUND;
                        }
                    }

                    return wrapped;
                }
            };
        }

        public Scriptable wrapJavaClass(final Context cx, final Scriptable scope, @SuppressWarnings("rawtypes") final Class javaClass) {
            final Class<?> type = this.ensureReplacedClass(scope, null, javaClass);

            return new NativeJavaClass(scope, javaClass) {
                private final Map<String, Boolean> staticMethodToAllowed = new HashMap<String, Boolean>();

                @Override
                public Object get(final String name, final Scriptable scope) {
                    final Object wrapped = super.get(name, scope);

                    System.out.println(wrapped.getClass());
                    if (wrapped instanceof BaseFunction) {
                        final String id = type.getName() + "." + name;
                        Boolean allowed = this.staticMethodToAllowed.get(id);

                        if (allowed == null) {
                            final boolean allow = shutter.allowStaticMethodAccess(type, name);
                            this.staticMethodToAllowed.put(id, allowed = Boolean.valueOf(allow));
                        }

                        if (!allowed.booleanValue()) {
                            return NOT_FOUND;
                        }
                    } else {
                        // NativeJavaObject + only boxed primitive types?
                        if (!shutter.allowStaticFieldAccess(type, name)) {
                            return NOT_FOUND;
                        }
                    }

                    return wrapped;
                }

            };
        }

        private final Set<Class<?>> replacedClasses = new HashSet<Class<?>>();

        private Class<?> ensureReplacedClass(final Scriptable scope, final Object obj, final Class<?> staticType) {
            final Class<?> type = (staticType == null && obj != null) ? obj.getClass() : staticType;

            if (!type.isPrimitive() && !type.getName().startsWith("java.") && this.replacedClasses.add(type)) {
                this.replaceJavaNativeClass(type, scope);
            }

            return type;
        }

        private void replaceJavaNativeClass(final Class<?> type, final Scriptable scope) {
            Object clazz = Context.jsToJava(ScriptableObject.getProperty(scope, "Packages"), Object.class);
            Object holder = null;
            for (final String part : type.getName().split("\\.")) {
                holder = clazz;
                clazz = ScriptableObject.getProperty((Scriptable) clazz, part);
            }

            final NativeJavaClass nativeClass = new NativeJavaClass(scope, type) {
                @Override
                public Object get(final String name, final Scriptable start) {
                    final Object wrapped = super.get(name, start);

                    if (wrapped instanceof BaseFunction) {
                        if (!shutter.allowStaticMethodAccess(type, name)) {
                            return NOT_FOUND;
                        }
                    } else {
                        // NativeJavaObject + only boxed primitive types?
                        if (!shutter.allowStaticFieldAccess(type, name)) {
                            return NOT_FOUND;
                        }
                    }

                    return wrapped;
                }
            };

            ScriptableObject.putProperty((Scriptable) holder, type.getSimpleName(), nativeClass);
            ScriptableObject.putProperty(scope, type.getSimpleName(), nativeClass);
        }
    }
}