/* $Id$ */

package ibis.io.jme;

import java.io.IOException;
import java.util.Hashtable;

/**
 * The <code>AlternativeTypeInfo</code> class maintains information about
 * a specific <code>Class</code>, such as a list of serializable fields, 
 * whether it has <code>readObject</code> and <code>writeObject</code>
 * methods, et cetera.
 *
 * The serializable fields are first ordered alphabetically, and then
 * by type, in the order: double, long, float, int, short, char, byte,
 * boolean, reference. This determines the order in which fields are
 * serialized.
 */
final class AlternativeTypeInfo extends IOProperties implements Constants {

    /**
     * Maintains all <code>AlternativeTypeInfo</code> structures in a
     * hashmap, to be accessed through their classname.
     */
    private static Hashtable alternativeTypes
            = new Hashtable();

    private static class ArrayWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            out.writeArray(ref, t.clazz, unshared);
        }
    }

    private static class IbisSerializableWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            super.writeHeader(out, ref, t, hashCode, unshared);
            ((JMESerializable) ref).generated_WriteObject(out);
        }
    }

    private static class ExternalizableWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            super.writeHeader(out, ref, t, hashCode, unshared);
            out.push_current_object(ref, 0);
            ((java.io.Externalizable) ref).writeExternal(
                    out.getJavaObjectOutputStream());
            out.pop_current_object();
        }
    }

    private static class StringWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            super.writeHeader(out, ref, t, hashCode, unshared);
            out.writeUTF((String) ref);
        }
    }

    private static class ClassWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            super.writeHeader(out, ref, t, hashCode, unshared);
            out.writeUTF((String) ref);
        }
    }

    private class EnumWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            super.writeHeader(out, ref, t, hashCode, unshared);
            out.writeUTF(((Enum) ref).name());
        }
    }

    private static class NotSerializableWriter extends IbisWriter {
        void writeObject(ObjectOutputStream out, Object ref,
                AlternativeTypeInfo t, int hashCode, boolean unshared)
                throws IOException {
            throw new NotSerializableException("Not serializable: " +
                    t.clazz.getName());
        }
    }

    private static class IbisSerializableReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            return t.gen.generated_newInstance(in);
        }
    }

    private static class ArrayReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            return in.readArray(t.clazz, typeHandle);
        }
    }

    private static class StringReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            String o = in.readUTF();
            in.addObjectToCycleCheck(o);
            return o;
        }
    }

    private static class ClassReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            String o = in.readUTF();
            Object obj = in.getClassFromName(o);
            in.addObjectToCycleCheck(obj);
            return obj;
        }
    }

    private static class EnumReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            String o = in.readUTF();
            Object obj;
            try {
                obj = Enum.valueOf((Class)t.clazz, o);
            } catch(Throwable e) {
                throw new IOException("Exception while reading enumeration"
                        + e);
            }
            in.addObjectToCycleCheck(obj);
            return obj;
        }
    }

    private static class ExternalizableReader extends IbisReader {
        Object readObject(ObjectInputStream in,
                AlternativeTypeInfo t, int typeHandle)
                throws IOException, ClassNotFoundException {
            Object obj;
            try {
                // Also calls parameter-less constructor
                obj = t.clazz.newInstance();
            } catch(Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Caught exception, now rethrow as ClassNotFound", e);
                }
                throw new ClassNotFoundException("Could not instantiate", e);
            }
            in.addObjectToCycleCheck(obj);
            in.push_current_object(obj, 0);
            ((Externalizable) obj).readExternal(
                    in.getJavaObjectInputStream());
            in.pop_current_object();
            return obj;
        }
    }

    /**
     * The <code>Class</code> structure of the class represented by this
     * <code>AlternativeTypeInfo</code> structure.
     */
    Class clazz;

    final IbisWriter writer;

    final IbisReader reader;

    /**
     * For each field, indicates whether the field is final.
     * This is significant for deserialization, because it determines the
     * way in which the field can be assigned to. The bytecode verifier
     * does not allow arbitraty assignments to final fields.
     */
    boolean[] fields_final;

    /** Number of <code>double</code> fields. */
    int double_count;

    /** Number of <code>long</code> fields. */
    int long_count;

    /** Number of <code>float</code> fields. */
    int float_count;

    /** Number of <code>int</code> fields. */
    int int_count;

    /** Number of <code>short</code> fields. */
    int short_count;

    /** Number of <code>char</code> fields. */
    int char_count;

    /** Number of <code>byte</code> fields. */
    int byte_count;

    /** Number of <code>boolean</code> fields. */
    int boolean_count;

    /** Number of <code>reference</code> fields. */
    int reference_count;

    /** Indicates whether the superclass is serializable. */
    boolean superSerializable;

    /** The <code>AlternativeTypeInfo</code> structure of the superclass. */

    AlternativeTypeInfo alternativeSuperInfo;

    /**
     * The "level" of a serializable class.
     * The "level" of a serializable class is computed as follows:
     * - if its superclass is serializable:
     *       the level of the superclass + 1.
     * - if its superclass is not serializable:
     *       1.
     */
    int level;

    /** serialPersistentFields of the class, if the class declares them. */
    java.io.ObjectStreamField[] serial_persistent_fields = null;

    /** Set if the class has a <code>readObject</code> method. */
    boolean hasReadObject;

    /** Set if the class has a <code>writeObject</code> method. */
    boolean hasWriteObject;

    /** Set if the class has a <code>writeReplace</code> method. */
    boolean hasReplace;

    /** Set if the class is Ibis serializable. */
    boolean isIbisSerializable = false;

    /** Set if the class is serializable. */
    boolean isSerializable = false;

    /** Set if the class is externalizable. */
    boolean isExternalizable = false;

    /** Set if the class represents an array. */
    boolean isArray = false;

    /** Set if the class represents a string. */
    boolean isString;

    /** Set if the class represents a class. */
    boolean isClass;

    /** Helper class for this class, generated by IOGenerator. */
    Generator gen;

    /**
     * Return the name of the class.
     *
     * @return the name of the class.
     */
    public String toString() {
        return clazz.getName();
    }

    /**
     * Try to create an object through the newInstance method of
     * ObjectStreamClass.
     * Return null if it fails for some reason.
     */
    Object newInstance() {
        // System.out.println("newInstance fails: no newInstance method");
        return null;
    }

    /**
     * Gets the <code>AlternativeTypeInfo</code> for class <code>type</code>.
     *
     * @param type the <code>Class</code> of the requested type.
     * @return the <code>AlternativeTypeInfo</code> structure for this type.
     */
    public static synchronized AlternativeTypeInfo getAlternativeTypeInfo(
            Class type) {
        AlternativeTypeInfo t = (AlternativeTypeInfo)alternativeTypes.get(type);

        if (t == null) {
            t = new AlternativeTypeInfo(type);
            alternativeTypes.put(type, t);
        }

        return t;
    }

    /**
     * Gets the <code>AlternativeTypeInfo</code> for class
     * <code>classname</code>.
     *
     * @param classname the name of the requested type.
     * @return the <code>AlternativeTypeInfo</code> structure for this type.
     */
    public static synchronized AlternativeTypeInfo getAlternativeTypeInfo(
            String classname) throws ClassNotFoundException {
        Class type = null;

        try {
            type = Class.forName(classname);
        } catch (ClassNotFoundException e) {
            type = Thread.currentThread().getContextClassLoader().loadClass(
                    classname);
        }

        return getAlternativeTypeInfo(type);
    }

    /**
     * Constructor is private. Use {@link #getAlternativeTypeInfo(Class)} to
     * obtain the <code>AlternativeTypeInfo</code> for a type.
     */
    private AlternativeTypeInfo(Class clazz) {

        this.clazz = clazz;

        try {
            /*
             Here we figure out what field the type contains, and which fields 
             we should write. We must also sort them by type and name to ensure
             that we read them correctly on the other side. We cache all of
             this so we only do it once for each type.
             */

            // TODO: Implement in some other way? getSerialPersistentFields();

            /* see if the supertype is serializable */
            Class superClass = clazz.getSuperclass();

            if (superClass != null) {
                if (java.io.Serializable.class.isAssignableFrom(superClass)) {
                    superSerializable = true;
                    alternativeSuperInfo = getAlternativeTypeInfo(superClass);
                    level = alternativeSuperInfo.level + 1;
                } else {
                    superSerializable = false;
                    level = 1;
                }
            }

            /* Now see if it has a writeObject/readObject. */
            /* TODO: How to handle ?
            writeObjectMethod = getMethod("writeObject",
                    new Class[] { ObjectOutputStream.class }, Void.TYPE);
            readObjectMethod = getMethod("readObject",
                    new Class[] { ObjectInputStream.class }, Void.TYPE);

            hasWriteObject = writeObjectMethod != null;
            hasReadObject = readObjectMethod != null;

            writeReplaceMethod = getMethod("writeReplace", new Class[0],
                    Object.class);

            readResolveMethod = getMethod("readResolve", new Class[0],
                    Object.class);

            hasReplace = writeReplaceMethod != null;
            */
            
            // Determines whether a class is Ibis-serializable.
            // We cannot use "instanceof ibis.io.Serializable", because that
            // would also return true if a parent class implements
            // ibis.io.Serializable, which is not good enough.

            Class[] intfs = clazz.getInterfaces();

            for (int i = 0; i < intfs.length; i++) {
                if (intfs[i].equals(ibis.io.Serializable.class)) {
                    isIbisSerializable = true;
                }
            }

            isSerializable = ibis.io.jme.JMESerializable.class.isAssignableFrom(clazz);

            isExternalizable = ibis.io.jme.Externalizable.class.isAssignableFrom(clazz);

            isArray = clazz.isArray();
            isString = (clazz == java.lang.String.class);
            isClass = (clazz == java.lang.Class.class);
            if (isArray || isString || isClass) {
                gen = null;
            } else {
                Class gen_class = null;
                String name = clazz.getName() + "_ibis_io_Generator";
                try {
                    gen_class = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    // The loading of the class failed.
                    // Maybe, Ibis was loaded using the primordial classloader
                    // and the needed class was not.
                    try {
                        gen_class = Thread.currentThread().getContextClassLoader()
                                .loadClass(name);
                    } catch (Exception e1) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Class " + name + " not found!");
                        }
                        gen = null;
                    }
                }
                if (gen_class != null) {
                    try {
                        gen = (Generator) gen_class.newInstance();
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Could not instantiate " + name);
                        }
                        gen = null;
                    }
                }
            }
        } catch (Exception e) {
            throw new SerializationError("Cannot initialize serialization "
                    + "info for " + clazz.getName(), e);
        }

        writer = createWriter();
        reader = createReader();
    }

    private IbisWriter createWriter() {
        if (isArray) {
            return new ArrayWriter();
        }
        if (isIbisSerializable) {
            return new IbisSerializableWriter();
        }
        if (isExternalizable) {
            return new ExternalizableWriter();
        }
        if (isString) {
            return new StringWriter();
        }
        if (isClass) {
            return new ClassWriter();
        }
        return new NotSerializableWriter();
    }

    private IbisReader createReader() {
        if (isArray) {
            return new ArrayReader();
        }
        if (gen != null) {
            return new IbisSerializableReader();
        }
        if (isExternalizable) {
            return new ExternalizableReader();
        }
        if (isString) {
            return new StringReader();
        }
        if (isClass) {
            return new ClassReader();
        }
        throw new SerializationError("Internal error: Could not find serialization for " + clazz.getName());
    }

    static Class getClass(String n) {
        Class c = null;
        try {
            c = Class.forName(n);
        } catch (ClassNotFoundException e) {
            throw new SerializationError(
                    "Internal error: could not load primitive array type " + n,
                    e);
        }
        return c;
    }
}
