package cs652.repl;

import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JavaREPL class which gets the user input,sends it for processing to
 * Nested Reader class and make any changes required to input based on requirements
 * and interprets the code using java compiler.
 * Starter code for this class is taken from the starter kit of this project provided
 * by Prof.Terrence Parr
 * @bhargavi
 */
public class JavaREPL {

	/**
	 * Main method which accepts the user input from console and sends it to
	 * exec method
	 * @param args user input string
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
			exec(new InputStreamReader(System.in));
	}

	/**
	 * Method which takes care of sending input for processing,compilation and
	 * execution steps sequentially to implement java interpreter by inheriting the
	 * statements typed previously
	 * @param r reader object of input string
	 * @throws IOException
	 */
	public static void exec(Reader r) throws IOException
	{
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		String tmpDirPath  = createTempDirectory();
        URL tmpURL = new File(tmpDirPath).toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[]{tmpURL});

		int classNumber = 0;
		String prefixClassName = "Interp_";
		try
		{
			while (true)
			{
				System.out.print("> ");
				String input = reader.getNestedString();
				if((input != null) && input.startsWith("print "))
				{
					input = replacePrintStatement(input);

				}

				if (input != null)
				{
					boolean isDeclrn = isDeclaration(tmpDirPath,input);

					if (classNumber == 0)
					{
						String classNum = Integer.toString(classNumber);
						String className = prefixClassName + classNum;
						if (isDeclrn)
						{
							boolean isStmt = false;
							getCode_WriteToFile_Compile_Execute(tmpDirPath,loader,className,null,input,
									isDeclrn,isStmt);
						}
						else
						{
							boolean isStmt = true;
							getCode_WriteToFile_Compile_Execute(tmpDirPath,loader,className,null,input,
									isDeclrn,isStmt);
						}
					}
					else
					{
						String classNum = Integer.toString(classNumber);
						String superClassNum = Integer.toString(classNumber - 1);
						String className = prefixClassName + classNum;
						String superClassName = prefixClassName + superClassNum;
						if (isDeclrn)
						{
							boolean isStmt = false;
							getCode_WriteToFile_Compile_Execute(tmpDirPath,loader,className,superClassName,input,
									isDeclrn,isStmt);
						}
						else
						{
							boolean isStmt = true;
							getCode_WriteToFile_Compile_Execute(tmpDirPath,loader,className,superClassName,input,
									isDeclrn,isStmt);
						}
					}
					classNumber = classNumber + 1;
				}
				else
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Method written to take care of replacing input statements with
	 * "print expr;" string to "System.out.println(expr);"
	 * @param input input string
	 * @return string with replacement
	 */
	private static String replacePrintStatement(String input)
	{
		StringBuffer str = new StringBuffer();
		String expr = input.substring(6);
		int expLen = expr.length();
		str.append("System.out.println");
		str.append("(");
		if(expr.endsWith(";"))
		{
			str.append(expr.substring(0,expLen-1));
			str.append(")");
			str.append(";");
		}
		else
		{
			str.append(expr);
			str.append(")");
		}

		return str.toString();
	}

	/**
	 * Method which takes care of compilation and execution steps based on user input if
	 * it is a declaration or statement and sequential execution of steps
	 * @param tmpDirPath directory path where all the .java files and .class files are created
	 * @param loader ClassLoader object
	 * @param className className
	 * @param superClassName superClassName
	 * @param input inputString
	 * @param isDeclrn true if it is declaration or false
	 * @param isStmt true if it is statement or false
	 * @throws Exception
	 */
	private static void getCode_WriteToFile_Compile_Execute(String tmpDirPath,ClassLoader loader,String className,String superClassName,
									  String input,boolean isDeclrn,boolean isStmt) throws Exception
	{
		// There is way too much cut-and-paste here. You should be able to factor this out into a single call to getCode and one to write()
		String content = getCode(className,superClassName,input,isDeclrn,isStmt);
		writeFile(tmpDirPath, className, content);
		String errorMsg = compile(tmpDirPath, className);
		if (errorMsg.equals(""))
			exec(loader, className, "exec");
		else
			System.err.println(errorMsg);
	}

	/**
	 * Method which returns code with java template attached to given input string
	 * based on given requirements
	 * @param className className
	 * @param extendSuper superClassName
	 * @param def input as a definition
	 * @param stat input as a statement
	 * @return java code including input statement
	 */
	private static String getCode(String className, String extendSuper,String input, boolean def, boolean stat)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("import java.io.*;"+"\n");
		builder.append("import java.util.*;"+"\n");
		builder.append("\n\n");

		if(extendSuper == null)
		{
			builder.append("public class "+ className);
			builder.append(" {\n");
			if(def)
			{
				builder.append("public static " + input+"\n");
				builder.append("public static void exec()");
				builder.append("{\n");
				builder.append("}\n");
			}
			else if(stat)
			{
				builder.append("public static void exec()");
				builder.append(" {\n");
				builder.append(input+"\n");
				builder.append("}\n");
			}
			builder.append("}");
		}
		else
		{
			builder.append("public class "+ className);
			builder.append(" extends "+extendSuper);
			builder.append(" {\n");
			if(def)
			{
				builder.append("public static " + input +"\n");
				builder.append("public static void exec()");
				builder.append("{\n");
				builder.append("}\n");
			}
			else if(stat)
			{
				builder.append("public static void exec()");
				builder.append(" {\n");
				builder.append(input +"\n");
				builder.append("}\n");
			}
			builder.append("}");
		}

		return builder.toString();
	}

	/**
	 * Method which writes the given content(java code) to fileName in the
	 * provided directory
	 * @param dir directory path
	 * @param fileName fileName
	 * @param content java code in string format
	 * @throws Exception
	 */
	private static void writeFile(String dir, String fileName, String content) throws Exception
	{
		PrintWriter writer = new PrintWriter(dir + "/" + fileName+".java","UTF-8");
		writer.print(content);
		writer.flush();
		writer.close();
	}

	/**
	 * Method which checks if the given input string is a statement or declaration
	 * Idea and code of this method has been taken from the documentation and links
	 * provided in documentation by Prof.Terrence Parr
	 * @param dir directory path where the dummy file can be created to test
	 * @param line input string
	 * @return returns true if the input string is declaration or false
	 * @throws Exception
	 */
	private static boolean isDeclaration(String dir,String line) throws Exception
	{
		String content = getCode("Bogus",null,line,true,false);
		writeFile(dir,"Bogus",content);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(dir+"/Bogus.java"));
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null,
				null, compilationUnits);
		task.parse();
		fileManager.close();
		return diagnostics.getDiagnostics().size() == 0;
	}

	/**
	 * Method which compiles the java file which has been created and returns the error messages
	 * if the file compiles properly return string is null or else it is filled with error message
	 * from compiler.
	 * @param dir path to directory where .java and .class files are located
	 * @param fileName name of file which needs to be compiled
	 * @return returns string with error message
	 * @throws IOException
	 */
	private static String compile(String dir,String fileName) throws IOException
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(dir +"/" + fileName + ".java"));
        List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")+
                ":"+dir));
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
                optionList,null, compilationUnits);

		boolean success = task.call();
		StringBuffer errorMsg = new StringBuffer();
		if (!success)
		{
			List<Diagnostic<? extends JavaFileObject>> diagnosticsErrors = diagnostics.getDiagnostics();
			int errorSize =  diagnosticsErrors.size();
			int count = 1;

			for (Diagnostic<? extends JavaFileObject> diagnosticError : diagnosticsErrors) {

				errorMsg.append("line ");
				errorMsg.append(diagnosticError.getLineNumber());
				errorMsg.append(": ");
				errorMsg.append(diagnosticError.getMessage(null));
				if(count != errorSize)
					errorMsg.append("\n");

				count++;
			}

		}
		fileManager.close();
		return errorMsg.toString();
	}

	/**
	 * Method which creates temporary directory in the default location of temporary files
	 * created by application
	 * The idea to use this method is taken from link provided by Prof.Terrence Parr in documentation
	 * for Project
	 * @return string representation of path where temporary directory has been created
	 * @throws IOException
	 */
	private static String createTempDirectory() throws IOException
	{
		Path tempDir = Files.createTempDirectory("tmp_");
		return tempDir.toString();
	}

	/**
	 * Method which is used to load class files and execute the exec() method
	 * to show the output
	 * This idea has been taken from lab-reflection which was done in the class
	 * @param loader object of ClassLoader class
	 * @param className className which needs to be loaded
	 * @param methodName methodName which needs to be executed
	 * @throws Exception
	 */
	private static void exec(ClassLoader loader, String className, String methodName) throws Exception
	{
        Class cl = loader.loadClass(className);
		Method method = cl.getMethod(methodName);
		method.invoke(null);
	}
}
