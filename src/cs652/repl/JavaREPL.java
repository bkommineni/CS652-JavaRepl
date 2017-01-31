package cs652.repl;

import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaREPL {

	public static void main(String[] args) throws IOException {

			exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;
		String className = "Interp_";
		createTempDirectory();
        URL tmpURL = new File("tmp").toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[]{tmpURL});


		try
		{
			while (true) {
				System.out.print("> ");
				String java = reader.getNestedString();

				boolean declarnCheck = isDeclaration(java);

				if (classNumber == 0) {
					String classNum = Integer.toString(classNumber);
					if (declarnCheck) {
						String content = getCode(className + classNum, null, java, null);
						writeFile("tmp", className + classNum, content);
						compile(className + classNum);

						exec(loader, className + classNum, "exec");

					}
					else {
						String content = getCode(className + classNum, null, null, java);
						writeFile("tmp", className + classNum, content);
						compile(className + classNum);

						exec(loader, className + classNum, "exec");

					}
				} else {
					String classNum = Integer.toString(classNumber);
					String superClassNum = Integer.toString(classNumber - 1);
					if (declarnCheck) {
						String content = getCode(className + classNum, className + superClassNum, java, null);
						writeFile("tmp", className + classNum, content);
						compile(className + classNum);

						exec(loader,className + classNum,"exec");
					}
					else {
						String content = getCode(className + classNum, className + superClassNum, null, java);
						writeFile("tmp", className + classNum, content);
						boolean successCheck = compile(className + classNum);

						exec(loader,className + classNum,"exec");
					}
				}
				classNumber = classNumber + 1;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getCode(String className, String extendSuper, String def, String stat)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("import java.io.*;"+"\n");
		builder.append("import java.util.*;"+"\n");

		if(extendSuper == null)
		{
			builder.append("public class "+ className);
			builder.append(" {\n");
			if(def != null)
			{
				builder.append("public static " + def+"\n");
				builder.append("public static void exec()");
				builder.append("{\n");
				builder.append("}\n");
			}
			else if(stat != null)
			{
				builder.append("public static void exec()");
				builder.append(" {\n");
				builder.append(stat+"\n");
				builder.append("}\n");
			}
			builder.append("}");
		}
		else
		{
			builder.append("public class "+ className);
			builder.append(" extends "+extendSuper);
			builder.append(" {\n");
			if(def != null)
			{
				builder.append("public static " + def +"\n");
				builder.append("public static void exec()");
				builder.append("{\n");
				builder.append("}\n");
			}
			else if(stat != null)
			{
				builder.append("public static void exec()");
				builder.append(" {\n");
				builder.append(stat +"\n");
				builder.append("}\n");
			}
			builder.append("}");
		}

		return builder.toString();
	}

	public static void writeFile(String dir, String fileName, String content) throws Exception
	{
		PrintWriter writer = new PrintWriter(dir + "/" + fileName+".java","UTF-8");
		writer.print(content);
		writer.flush();
		writer.close();
	}

	public static boolean isDeclaration(String line) throws Exception
	{
		String content = getCode("Bogus",null,line,null);
		writeFile("tmp","Bogus",content);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList("tmp/Bogus.java"));
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null,
				null, compilationUnits);
		task.parse();
		fileManager.close();
		return diagnostics.getDiagnostics().size() == 0;
	}

	public static boolean compile(String fileName) throws IOException
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList("tmp/" + fileName + ".java"));
        List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")+
                ":tmp"));
		JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics,
                optionList,null, compilationUnits);
		boolean success = task.call();
		if (!success) {
			List<Diagnostic<? extends JavaFileObject>> diagnosticsErrors = diagnostics.getDiagnostics();
			for (Diagnostic<? extends JavaFileObject> diagnosticError : diagnosticsErrors) {
				// read error details from the diagnostic object
				System.err.println(diagnosticError.getMessage(null));
			}
		}
		fileManager.close();
		return success;
	}

	public static void createTempDirectory() throws IOException
	{
		File dir = new File("tmp");

		if(!dir.exists())
			dir.mkdir();
	}

	public static void exec(ClassLoader loader, String className, String methodName) throws Exception
	{
		//Class cl = Class.forName(className,true,loader);
        Class cl = loader.loadClass(className);
		Method method = cl.getMethod(methodName);
		method.invoke(null);
	}
}
