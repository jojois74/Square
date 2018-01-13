package box.gift.interpolation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * THE BIGGEST FAILURE OF THE ENTIRE JAVA LANGUAGE.
 * It doesn't take a genius to understand that the annotation processor APIs are messed up.
 * There are so many different class types:
 * Element, ElementType, TypeMirror, TypeKind, ElementKind, DeclaredType, Name
 * Elements, Types, TypeUtilities, typeUtils (instance), elementUtils (instance)
 * and probably more that I am forgetting.
 * They all convert between each other in mystical and confusing ways, (or are used to do so)
 * creating a huge tangled mess, and leading to almost unintelligible statements
 * resulting from a plethora of not only utility methods but also casts
 * and other cryptic function calls, just to get what we want.
 * Even worse, there are usually a number of different options depending on
 * exactly what object types we go through, and the result of each call is surprising.
 * Therefore,
 *     TODO: make a unified API that makes sense, if continuing to do much annotation processing.
 */

//@SupportedAnnotationTypes("box.gift.interpolation.Interpolate") // Bug: these annotations don't work? Override methods instead.
//@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InterpolationProcessor extends AbstractProcessor
{
    private Class<InterpolateSource> interpolationSourceAnnotation = InterpolateSource.class;
    private Class<InterpolateTarget> interpolationTargetAnnotation = InterpolateTarget.class;
    private TypeMirror interpolatableTypeMirror;
    private Types typeUtilities;
    private Elements elementUtilities;

    // To serialize
    private List<Object> serialize = null;
    private SourceForger sourceFile = null;
    private HashMap<String, SourceForger.Method> saveMethods = null;
    private HashMap<String, SourceForger.Method> recallMethods = null;

    private SourceForger.Class sourceClass = null;
    private List<String> ids = new LinkedList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);

        typeUtilities = processingEnv.getTypeUtils();
        elementUtilities = processingEnv.getElementUtils();
        interpolatableTypeMirror = typeUtilities.erasure(elementUtilities.getTypeElement(Interpolatable.class.getName()).asType());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        try
        {
            // If this is not the first modules processed, load up the old source file and continue writing.
            ArrayList read = readSource();
            if (read != null)
            {
                sourceFile = (SourceForger) read.get(0);
                saveMethods = (HashMap<String, SourceForger.Method>) read.get(1);
                recallMethods = (HashMap<String, SourceForger.Method>) read.get(2);
            }
            if (sourceFile == null)
            {
                sourceFile = new SourceForger();
                sourceFile.setPackage("box.gift.interpolation.generated");
                sourceFile.imports("java.util.List");
                sourceClass = sourceFile.makeClass().setAccess("public").setName("Interpolator");
            }
            else
            {
                sourceClass = sourceFile.getClasses().get(0);
            }
            if (saveMethods == null)
            {
                saveMethods = new HashMap<>();
                recallMethods = new HashMap<>();
            }
            serialize = new ArrayList<>(4);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Interpolation source/save functions
        Set<? extends Element> sourcesOfInterpolation = roundEnv.getElementsAnnotatedWith(interpolationSourceAnnotation);
        // There are no source annotated elements
        if (sourcesOfInterpolation.size() == 0)
        {
            return true;
        }

        for (Element element : sourcesOfInterpolation)
        {

            boolean isInterpolatable = false;
            TypeElement clas = elementUtilities.getTypeElement(element.asType().toString());
            if (clas != null)
            {
                for (TypeMirror interf : clas.getInterfaces())
                {
                    if (typeUtilities.isAssignable(interf, interpolatableTypeMirror))
                    {
                        isInterpolatable = true;
                        break;
                    }
                }
            }

            boolean isDouble = element.asType().equals(typeUtilities.getPrimitiveType(TypeKind.DOUBLE));

            if (element.getKind().isField())
            {
                if (isDouble || isInterpolatable)
                {
                    Element enclosingClass = element.getEnclosingElement();
                    if (enclosingClass.getKind().isClass())
                    {
                        createSaveMethod(enclosingClass);
                        saveMethods.get(enclosingClass.toString()).addLine("in.add(entity." + element.getSimpleName() + ");");
                        ids.add(element.getAnnotation(interpolationSourceAnnotation).id());

                        // Propagate up
                        Element superElement = typeUtilities.asElement(getSuperTypeMirror(enclosingClass));
                        if (!superElement.getSimpleName().toString().equals(Object.class.getSimpleName()))
                        {
                            createSaveMethod(superElement);
                            saveMethods.get(enclosingClass.toString()).addLine("saveInterpolatables(in, (" + superElement.toString() + ") element);");
                        }
                    }
                    else
                    {
                        throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a field encolsed by a class!");
                    }
                }
                else
                {
                    throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a double or an " + interpolatableTypeMirror + "!");
                }
            }
            else
            {
                throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a class field!");
            }
        }

        // Interpolation target/recall functions
        Set<? extends Element> targetsOfInterpolation = roundEnv.getElementsAnnotatedWith(interpolationTargetAnnotation);
        // There are no target annotated elements
        if (targetsOfInterpolation.size() == 0)
        {
            return true;
        }

        while (ids.size() > 0)
        {
            for (Element element : targetsOfInterpolation)
            {
                boolean isInterpolatable = false;
                TypeElement clas = elementUtilities.getTypeElement(element.asType().toString());
                if (clas != null)
                {
                    for (TypeMirror interf : clas.getInterfaces())
                    {
                        if (typeUtilities.isAssignable(interf, interpolatableTypeMirror))
                        {
                            isInterpolatable = true;
                            break;
                        }
                    }
                }

                boolean isDouble = element.asType().equals(typeUtilities.getPrimitiveType(TypeKind.DOUBLE));

                if (element.getKind().isField())
                {
                    if (isDouble || isInterpolatable)
                    {
                        Element enclosingClass = element.getEnclosingElement();
                        if (enclosingClass.getKind().isClass())
                        {
                            if (ids.contains(element.getAnnotation(interpolationTargetAnnotation).id()))
                            {
                                if (element.getAnnotation(interpolationTargetAnnotation).id().equals(ids.get(0)))
                                {
                                    ids.remove(0);
                                }
                                else
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                throw new IllegalStateException("No interpolation source found for target: " + element.getAnnotation(interpolationTargetAnnotation).id());
                            }
                            createRecallMethod(enclosingClass);
                            String cast = " (double) ";
                            if (!isDouble)
                            {
                                cast = " (" + element.asType().toString() + ") ";
                            }
                            recallMethods.get(enclosingClass.toString()).addLine("entity." + element.getSimpleName() + " =" + cast + "out.remove(0);");

                            // Propagate up
                            Element superElement = typeUtilities.asElement(getSuperTypeMirror(enclosingClass));
                            if (!superElement.getSimpleName().toString().equals(Object.class.getSimpleName()))
                            {
                                createRecallMethod(superElement);
                                recallMethods.get(enclosingClass.toString()).addLine("recallInterpolatables(out, (" + superElement.toString() + ") element);");
                            }
                        }
                        else
                        {
                            throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a field encolsed by a class!");
                        }
                    }
                    else
                    {
                        throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a double or an " + interpolatableTypeMirror + "!");
                    }
                }
                else
                {
                    throw new IllegalStateException(interpolationSourceAnnotation.getSimpleName() + " may only be applied to a class field!");
                }
            }
        }

//        SourceForger.Class interpolator = null;
//        if (sourceFile.classes.size() >= 1)
//        {
//            interpolator = sourceFile.classes.get(0);
//        }
//        else
//        {
//            interpolator = sourceFile.makeClass("/*pack*/ class Interpolator");
//        }
//
//        //System.out.println(sourceFile);
//
//        Set<? extends Element> classes = roundEnv.getRootElements();
//        if (classes.size() == 0)
//        {
//            System.out.println("No classes found! Abort!");
//            return true;
//        }
//
//        // Put Interpolatables.
//        SourceForger.Method putInterpolatables = null;
//        LinkedHashSet<String> ids = new LinkedHashSet<>();
//
//        for(Element clasz : classes)
//        {
//            String className = clasz.getSimpleName().toString();
//
//            boolean addedMethod = false;
//            //System.out.println("ROOT NAME: " + className);
//            for (Element element : clasz.getEnclosedElements())
//            {
//                InterpolateSource annotationFrom = element.getAnnotation(InterpolateSource.class);
//                if (annotationFrom == null)
//                {
//                    continue;
//                }
//
//                if (!addedMethod)
//                {
//                    addedMethod = true;
//                    putInterpolatables = interpolator.makeMethod("public static void putInterpolatables(" + className + " entity, List<Object> in)");
//                    sourceFile.imports(clasz.asType().toString());
//                }
//
//                //TODO: error checking here!
//
//                String id = annotationFrom.id();
//                String fieldName = element.getSimpleName().toString();
//                putInterpolatables.addLine("in.add(entity." + fieldName + ");");
//                ids.add(id);
//            }
//
//            //processingEnv.getElementUtils().;
//            // Chain up the superclasses
//            /*
//            TypeMirror currentClass = ((TypeElement) clasz).getSuperclass();
//            do
//            {
//                System.out.println(currentClass.toString());
//                DeclaredType declared = (DeclaredType) currentClass;
//                TypeElement supertypeElement = (TypeElement) declared.asElement();
//                currentClass = supertypeElement.getSuperclass();
//            }
//            while (!currentClass.toString().equals(Object.class.getCanonicalName()));*/
//            //Class currentClass = clasz.asType().getKind().dir
//        }
//
//        //System.out.println(ids);
//        if (ids.size() == 0)
//        {
//            System.out.println("No annotations found! No need to process this file.");
//            return true;
//        }
//
//        // Get Interpolatables.
//        SourceForger.Method getInterpolatables = null;
//        List<String> assignments = new ArrayList<>(ids.size());
//        for (int i = 0; i < ids.size(); i++)
//        {
//            assignments.add("");
//        }
//
//        for(Element clasz : classes)
//        {
//            String className = clasz.getSimpleName().toString();
//
//            boolean addedMethod = false;
//            //System.out.println("ROOT NAME: " + className);
//
//            for (Element element : clasz.getEnclosedElements())
//            {
//                InterpolateTarget annotationTo = element.getAnnotation(InterpolateTarget.class);
//                if (annotationTo == null)
//                {
//                    continue;
//                }
//
//                if (!addedMethod)
//                {
//                    addedMethod = true;
//                    getInterpolatables = interpolator.makeMethod("public static void getInterpolatables(" + className + " entity, List<Object> out)");
//                }
//
//                //TODO: error checking here!
//
//                String id = annotationTo.id();
//                String fieldName = element.getSimpleName().toString();
//
//                int i = 0;
//                for (String str : ids)
//                {
//                    if (str.equals(id)) break;
//                    i++;
//                }
//
//                StringBuilder assignmentBuilder = new StringBuilder("entity." + fieldName + " = ");
//
//                String typeName = element.asType().toString();
//
//                // Cast
//                if (!typeName.equals("double"))
//                {
//                    assignmentBuilder.append("(").append(typeName).append(")");
//                }
//                else
//                {
//                    assignmentBuilder.append("(double)");
//                }
//
//                assignmentBuilder.append(" out.remove(0);");
//
//                assignments.add(i, assignmentBuilder.toString());
//            }
//            if (addedMethod)
//            {
//                for (String assignment : assignments)
//                {
//                    if (!assignment.equals(""))
//                    {
//                        getInterpolatables.addLine(assignment);
//                    }
//                }
//            }
//        }
        //System.out.println(assignments);

        // Generate source file.
        final String fileName = "box.gift.interpolationprocessor.generated.Interpolator";
        try
        {/*
            Filer filer = processingEnv.getFiler();
            JavaFileObject source = filer.createSourceFile(fileName);

            Writer writer = source.openWriter();
            writer.write(sourceFile.toString());
            writer.flush();
            writer.close();*/

            // Save stuff in case process runs again by a different module
            serialize.add(sourceFile);
            serialize.add(saveMethods);
            serialize.add(recallMethods);
            FileOutputStream fout1 = new FileOutputStream("interpolation/build/generated/source/apt/debug/box/gift/interpolation/generated/tempSource.txt");
            ObjectOutputStream oos1 = new ObjectOutputStream(fout1);
            oos1.writeObject(serialize);
            oos1.flush();
            oos1.close();
            fout1.flush();
            fout1.close();

            // Write source file
            String module = "gameutils";
            File dir = new File(module + "/build/generated/source/apt/debug/box/gift/interpolation/generated/");
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            PrintWriter writer = new PrintWriter(module + "/build/generated/source/apt/debug/box/gift/interpolation/generated/Interpolator.java", "UTF-8");
            writer.write(sourceFile.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return true;
    }

    private void createSaveMethod(Element enclosingClass)
    {
        if (!saveMethods.containsKey(enclosingClass.toString()))
        {
            saveMethods.put(enclosingClass.toString(), sourceClass.makeMethod()
                    .setAccess("public static").setType("void").setName("saveInterpolatables")
                    .addParameter("List<java.lang.Object> in")
                    .addParameter(enclosingClass.toString() + " entity"));
            //sourceFile.imports(enclosingClass.toString()); //No need to import if using fully qualified name
        }
    }

    private void createRecallMethod(Element enclosingClass)
    {
        if (!recallMethods.containsKey(enclosingClass.toString()))
        {
            recallMethods.put(enclosingClass.toString(), sourceClass.makeMethod()
                    .setAccess("public static").setType("void").setName("recallInterpolatables")
                    .addParameter("List<java.lang.Object> out")
                    .addParameter(enclosingClass.toString() + " entity"));
            //sourceFile.imports(enclosingClass.toString()); //No need to import if using fully qualified name
        }
    }

    private TypeMirror getSuperTypeMirror(TypeMirror target)
    {
        return  ((TypeElement) ((DeclaredType) (target)).asElement()).getSuperclass();
    }

    private TypeMirror getSuperTypeMirror(Element target)
    {
        TypeMirror currentType = target.asType();
        return getSuperTypeMirror(currentType);
    }

    private boolean descendsFrom(Element target, Element ancestor)
    {
        return descendsFrom(target, ancestor.asType());
    }

    private boolean descendsFrom(Element target, TypeMirror ancestor)
    {
        TypeMirror type = target.asType();
        do
        {
            type = getSuperTypeMirror(type);
            if (type.toString().equals(ancestor.toString()))
            {
                return true;
            }
        }
        while (!type.toString().equals(Object.class.getName()));
        return false;
    }

    private ArrayList readSource() throws IOException
    {
        FileInputStream fout1;
        try
        {
            fout1 = new FileInputStream("interpolation/build/generated/source/apt/debug/box/gift/interpolation/generated/tempSource.txt");
        } catch (FileNotFoundException e)
        {
            return null;
        }
        ObjectInputStream oos1 = new ObjectInputStream(fout1);
        ArrayList read = null;
        try
        {
            read = (ArrayList) oos1.readObject();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        oos1.close();
        fout1.close();
        return read;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> types = new HashSet<>(); // What type of set is best here? I assume it doesn't really matter....
        types.add(interpolationSourceAnnotation.getCanonicalName());
        types.add(InterpolateTarget.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

}
