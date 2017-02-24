package example.javaagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class PropertyAnnotationVisitor extends ClassVisitor {

    public PropertyAnnotationVisitor(ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }
}
