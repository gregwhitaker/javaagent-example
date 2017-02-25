package example.javaagent;

import org.objectweb.asm.*;

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
            if (className.contains("ExampleBean")) {
                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                
                ClassVisitor cv = new PropertyClassVisitor(cw, className);
                cr.accept(cv, 0);
                
                return cw.toByteArray();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return bytes;
    }
    
    public class FieldInfo {
        String name;
        String desc;
        String propertyName;
    
        public FieldInfo(String name, String desc, String propertyName) {
            this.name = name;
            this.desc = desc;
            this.propertyName = propertyName;
        }
    
        @Override
        public String toString() {
            return "FieldInfo{" +
                       "name='" + name + '\'' +
                       ", desc='" + desc + '\'' +
                       ", propertyName='" + propertyName + '\'' +
                       '}';
        }
    }
    
    public class PropertyClassVisitor extends ClassVisitor {
        private String className;
        
        private List<FieldInfo> fieldInfos;
        
        public PropertyClassVisitor(ClassVisitor cv, String pClassName) {
            super(Opcodes.ASM5, cv);
            
            this.className = pClassName;
            this.fieldInfos = new ArrayList<>();
            
            cv.visitAnnotation("Lexample/javaagent/PropertyInstrumented;", true);
        }
    
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            final String fieldName = name;
            final String fieldDesc = desc;
            return new FieldVisitor(Opcodes.ASM5, super.visitField(access, name, desc, signature, value)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.contains("Lexample/javaagent/core/Property;")) {
                        return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
                            @Override
                            public void visit(String name, Object value) {
                                super.visit(name, value);
    
                                FieldInfo fieldInfo = new FieldInfo(fieldName, fieldDesc, (String) value);
                                fieldInfos.add(fieldInfo);
                            }
                        };
                    } else {
                        return super.visitAnnotation(desc, visible);
                    }
                }
            };
            
        }
    
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            
            if (name.equals("<init>")) {
                mv = new MethodVisitor(Opcodes.ASM5, mv) {
                    @Override
                    public void visitCode() {
                        fieldInfos
                            .forEach(fi -> {
                                Type type = Type.getType(fi.desc);
                                String propertyValue = props.getProperty(fi.propertyName);
                                
                                Object value;
                                switch (type.getSort()) {
                                    case Type.BOOLEAN:
                                        value = Boolean.valueOf(propertyValue);
                                        break;
                                    case Type.BYTE:
                                        value = Byte.valueOf(propertyValue);
                                        break;
                                    case Type.CHAR:
                                        value = propertyValue.charAt(0);
                                        break;
                                    case Type.SHORT:
                                        value = Short.valueOf(propertyValue);
                                        break;
                                    case Type.INT:
                                        value = Integer.valueOf(propertyValue);
                                        break;
                                    case Type.LONG:
                                        value = Long.valueOf(propertyValue);
                                        break;
                                    case Type.FLOAT:
                                        value = Float.valueOf(propertyValue);
                                        break;
                                    case Type.DOUBLE:
                                        value = Double.valueOf(propertyValue);
                                        break;
                                    case Type.ARRAY:
                                    case Type.OBJECT:
                                        value = propertyValue;
                                        break;
                                    default:
                                        throw new RuntimeException("Unable to find type for " + fi.desc);
                                }
                                
                                addField(mv, value, fi.name, fi.desc);
                                
                            });
                    }
                };
            }
           
            
            return mv;
        }
        
        private void addField(MethodVisitor mv, Object value, String name, String desc) {
            Label ll = new Label();
            mv.visitLabel(ll);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitLdcInsn(value);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, name, desc);
        }
         
    }
}
