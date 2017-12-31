package box.gift.interpolation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joseph on 12/26/2017.
 *
 * Utility class for creating a String representing a Java source file.
 * by composing it from its parts.
 */

public class SourceForger implements Serializable //TODO: finish documentation.
{
    private String mPackage;
    private List<Class> classes;
    private List<String> imports;

    /**
     * Creates a new SourceForger.
     */
    public SourceForger()
    {
        classes = new ArrayList<>(1);
        imports = new LinkedList<>();
    }

    public List<Class> getClasses()
    {
        return classes;
    }

    /**
     * Construct the file text.
     * @return the file.
     */
    public String toString()
    {
        StringBuilder source = new StringBuilder();

        // Package
        source.append("package ").append(mPackage).append(";\n\n");

        // Imports
        for (String mImport : imports)
        {
            source.append("import ").append(mImport).append(";\n");
        }
        source.append("\n");

        // Classes
        for (Class mClass : classes)
        {
            source.append(mClass.toString());
        }

        return source.toString();
    }

    /**
     * Set the package of this source file.
     * @param qualifiedName the fully qualified name of the package.
     */
    public void setPackage(String qualifiedName)
    {
        mPackage = qualifiedName;
    }

    /**
     * Adds an import line.
     * @param qualifiedName the fully qualified name of the import.
     */
    public void imports(String qualifiedName)
    {
        imports.add(qualifiedName);
    }

    /**
     * Create a new class.
     * @return the created class.
     */
    public Class makeClass()
    {
        Class mClass = new Class();
        classes.add(mClass);
        return mClass;
    }

    public static class Class implements Serializable
    {
        private String className;
        private String accessMod;
        private List<Method> methods;
        private List<String> fields;

        private Class()
        {
            methods = new ArrayList<>(7);
            fields = new LinkedList<>();
        }

        public Class setAccess(String access)
        {
            accessMod = access;
            return this;
        }

        public Class setName(String name)
        {
            className = name;
            return this;
        }

        public List<Method> getMethods()
        {
            return methods;
        }

        public String toString()
        {
            StringBuilder source = new StringBuilder();

            // Signature & Open
            source
                    .append(accessMod)
                    .append(" class ")
                    .append(className)
                    .append("\n{\n");

            // Fields
            for (String mField : fields)
            {
                source.append(mField).append(";\n");
            }
            source.append("\n");

            // Methods
            for (Method mMethod : methods)
            {
                source.append(mMethod.toString()).append("\n");
            }

            // Close
            source.append("}");

            return source.toString();
        }



        public Class addField(String signature)
        {
            fields.add(signature);
            return this;
        }

        public Method makeMethod()
        {
            Method mMethod = new Method();
            methods.add(mMethod);
            return mMethod;
        }
    }

    public static class Method implements Serializable
    {
        private String methodName;
        private String accessMod;
        private String returnType = "void";
        private List<String> lines;
        private List<String> params;

        private Method()
        {
            lines = new LinkedList<>();
            params = new ArrayList<>(3);
        }

        public Method setAccess(String access)
        {
            accessMod = access;
            return this;
        }

        public Method setName(String name)
        {
            methodName = name;
            return this;
        }

        public Method setType(String type)
        {
            returnType = type;
            return this;
        }

        public String toString()
        {
            StringBuilder source = new StringBuilder();

            // Signature & Open
            source
                    .append("\t")
                    .append(accessMod)
                    .append(" ").append(returnType)
                    .append(" ").append(methodName)
                    .append("(");
            for (int i = 0; i < params.size(); i++)
            {
                source.append(params.get(i));
                if (i != params.size() - 1)
                {
                    source.append(", ");
                }
            }
            source
                    .append(")\n\t{\n");

            // Lines
            for (String mLine : lines)
            {
                source.append("\t\t").append(mLine).append("\n");
            }

            // Close
            source.append("\t}\n");

            return source.toString();
        }

        public Method addParameter(String param)
        {
            params.add(param);
            return this;
        }

        public Method addLine(String line)
        {
            lines.add(line);
            return this;
        }
    }
}
