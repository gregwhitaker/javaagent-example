package example.javaagent;

import example.javaagent.core.Property;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class PropertyTransformer implements ClassFileTransformer {

    private final Properties props;

    public PropertyTransformer(final Properties props) {
        this.props = props;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] bytes)
            throws IllegalClassFormatException {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
    
            ClassNode cn = new ClassNode();
            reader.accept(cn, 0);
    
            List<FieldNode> propertyFieldNodes = getPropertyFields(cn);
            if (!propertyFieldNodes.isEmpty()) {
                if (!isClassTransformed(cn)) {
                    for (FieldNode fn : getPropertyFields(cn)) {
                        transformField(fn);
                    }
            
                    // cn.visibleAnnotations.add(new AnnotationNode(Type.getDescriptor(PropertyInstrumented.class)));
            
                    return writer.toByteArray();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return bytes;
    }

    private FieldNode transformField(FieldNode fn) {

        AnnotationNode an = getAnnotation(fn, Property.class);

        String propertyName = (String) getAnnotationAttribute(an, "value");
        String propertyValue = props.getProperty(propertyName);

        Type type = Type.getType(fn.desc);

        switch (type.getSort()) {
            case Type.BOOLEAN:
                fn.value = Boolean.valueOf(propertyValue);
                break;
            case Type.BYTE:
                fn.value = Byte.valueOf(propertyValue);
                break;
            case Type.CHAR:
                fn.value = propertyValue.charAt(0);
                break;
            case Type.SHORT:
                fn.value = Short.valueOf(propertyValue);
                break;
            case Type.INT:
                fn.value = Integer.valueOf(propertyValue);
                break;
            case Type.LONG:
                fn.value = Long.valueOf(propertyValue);
                break;
            case Type.FLOAT:
                fn.value = Float.valueOf(propertyValue);
                break;
            case Type.DOUBLE:
                fn.value = Double.valueOf(propertyValue);
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                fn.value = propertyValue;
                break;
            default:
                throw new RuntimeException("Unable to find type for " + propertyValue);
        }

        fn.visitEnd();

        return fn;
    }

    @SuppressWarnings("unchecked")
    private List<FieldNode> getPropertyFields(ClassNode cn) {
        List<FieldNode> fieldNodes = new ArrayList<>();
        for (FieldNode fn : (List<FieldNode>) cn.fields) {
            if (hasAnnotation(fn, Property.class)) {
                fieldNodes.add(fn);
            }
        }

        return fieldNodes;
    }

    @SuppressWarnings("unchecked")
    private boolean hasAnnotation(ClassNode cn, Class<? extends Annotation> annotationClazz) {
        if (cn.visibleAnnotations != null) {
            String desc = Type.getDescriptor(annotationClazz);
            for (AnnotationNode an : (List<AnnotationNode>) cn.visibleAnnotations) {
                if (desc.equals(an.desc)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean hasAnnotation(FieldNode fn, Class<? extends Annotation> annotationClazz) {
        if (fn.visibleAnnotations != null) {
            String desc = Type.getDescriptor(annotationClazz);
            for (AnnotationNode an : (List<AnnotationNode>) fn.visibleAnnotations) {
                if (desc.equals(an.desc)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private AnnotationNode getAnnotation(FieldNode fn, Class<? extends Annotation> annotationClazz) {
        if (fn.visibleAnnotations != null) {
            String desc = Type.getDescriptor(annotationClazz);
            for (AnnotationNode an : (List<AnnotationNode>) fn.visibleAnnotations) {
                if (desc.equals(an.desc)) {
                    return an;
                }
            }
        }

        return null;
    }

    private Object getAnnotationAttribute(AnnotationNode an, String name) {
        Object value = null;
        if (an.values != null) {
            for (int i = 0; i < an.values.size(); i += 2) {
                if (name.equals(an.values.get(i))) {
                    value = an.values.get(i + 1);
                }
            }
        }

        return value;
    }

    private boolean isClassTransformed(ClassNode cn) {
        return hasAnnotation(cn, PropertyInstrumented.class);
    }

}
