package com.github.java2uml.core.parsing;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadcukandrej on 09.12.14.
 */
public class CreateUmlCode {
    public static final int ALL = 1;
    public static final int AGGREGATION = 4;
    public static final int COMPOSITION = 5;
    public static final int ASSOCIATION = 6;
    public static StringBuilder source;
    public static StringBuilder connections;
    public static StringBuilder aggregation;
    public static StringBuilder composition;
    public static StringBuilder association;
    private String fileUMLDiagramClasses;
    public final String UML_TEMPLATE = "uml_templates";
    public static List<String> classes;
    private static String projectName;
    private int typeConnections;

    public CreateUmlCode(String folder, int typeConnections) throws Exception {
        // Генерирование названия файла UML
        this.typeConnections = typeConnections;
        fileUMLDiagramClasses = "classes.plantuml";
        init(folder);
    }

    public void init(String path) throws Exception {
        classes = new ArrayList<String>();
        createListClasses(new File(path));
        source = new StringBuilder();
        connections = new StringBuilder();
        aggregation = new StringBuilder();
        composition = new StringBuilder();
        association = new StringBuilder();
        // текст в формате plantuml - начало сборки
        source.append("@startuml\n");

        // разбираем анализируемый проект
        readPackage(new File(path));

        source.append(connections);
        typeConnections(typeConnections);
        // конец сборки
        source.append("@enduml\n");

        // Запись в файл UML в папку uml_templates
        write(source.toString());
    }

    private void readPackage(File path) throws Exception {
        File[] folder = path.listFiles();

        for (int i = 0; i < folder.length; i++) {
            if (folder[i].isDirectory()) {
                System.out.println(folder[i].toString().contains(projectName + "/src") && getNamePackage(folder[i].toString()) != null);
                if(folder[i].toString().contains(projectName + "/src") && getNamePackage(folder[i].toString()) != null) {
                    source.append("namespace ");
                    source.append(getNamePackage(folder[i].toString()) + " {\n");

                }
                readPackage(folder[i]);
                if(folder[i].toString().contains(projectName + "/src") && getNamePackage(folder[i].toString()) != null)
                    source.append("}\n");
            }
            else if (folder[i].toString().toLowerCase().endsWith(".java")) {
                createCU(folder[i]);
            }
        }

    }

    public void createCU(File path) throws Exception {
        // creates an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(path);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
        /**
         *   Начало анализа кода
         */
        new UMLDiagramClasses(cu);
    }

    public static String setModifier(int mod) {
        switch (mod) {
            case Modifier.PUBLIC:
                return " +";
            case Modifier.PRIVATE:
                return (" -");
            case Modifier.PROTECTED:
                return (" #");
            case Modifier.STATIC:
                return (" {static}");
            case Modifier.PUBLIC | Modifier.STATIC:
                return (" +{static}");
            case Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL:
                return (" +{static}");
            case Modifier.PROTECTED | Modifier.STATIC:
                return (" #{static}");
            case Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL:
                return (" #{static}");
            case Modifier.PRIVATE | Modifier.STATIC:
                return (" -{static}");
            case Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL:
                return (" -{static}");
            case Modifier.PUBLIC | Modifier.FINAL:
                return (" +");
            case Modifier.PROTECTED | Modifier.FINAL:
                return (" #");
            case Modifier.PRIVATE | Modifier.FINAL:
                return (" -");
            case Modifier.ABSTRACT:
                return (" {abstract}");
            default:
                return "";
        }
    }

    public void write(String text) {
        //Определяем файл
        File folder = new File(UML_TEMPLATE).getAbsoluteFile();
        File file = new File(UML_TEMPLATE + "/" + fileUMLDiagramClasses).getAbsoluteFile();

        try {
            //проверяем, что если папка не существует то создаем ее
            if (!folder.exists()) {
                folder.mkdir();
            }
            //проверяем, что если файл не существует то создаем его
            if (!file.exists()) {
                file.createNewFile();
            }

            //PrintWriter обеспечит возможности записи в файл
            PrintWriter out = new PrintWriter(file.getAbsoluteFile());

            try {
                //Записываем текст в файл
                out.print(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String endAfterLastPoint(String string, String separator){
        String[] str = string.split(separator);
        return str.length > 1 ? str[str.length-1] : string;
    }

    private String getNamePackage(String path){
        String[] subString = path.split("src");
        String namePackage = subString.length > 1 ? subString[1].replace("/", ".").substring(1) : null;
        return namePackage;
    }

    private void createListClasses(File path){

        File[] folder = path.listFiles();

        for (int i = 0; i < folder.length; i++) {
            if (folder[i].isDirectory()) {
                createListClasses(folder[i]);
            }
            else if (folder[i].toString().toLowerCase().endsWith(".java")) {
                classes.add(getNameClass(folder[i].toString()));
            }
        }
    }

    private String getNameClass(String file){
        String[] subString = file.split("/");
        String className = subString.length > 1 ? subString[subString.length - 1].replace(".java", "") : null;
        return className;
    }
    
    private void typeConnections(int type){
        switch (type){
            case AGGREGATION:
                source.append(aggregation);
                break;
            case COMPOSITION:
                source.append(composition);
                break;
            case ASSOCIATION:
                source.append(association);
                break;
            default:
                source.append(aggregation);
                source.append(composition);
                source.append(association);
        }
        
    }
        
        
    
}
