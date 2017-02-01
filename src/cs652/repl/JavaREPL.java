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

public class JavaREPL {

	public static void main(String[] args) throws IOException {

			exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		String tmpDirPath  = createTempDirectory1();
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;
		String className = "Interp_";
        URL tmpURL = new File(tmpDirPath).toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[]{tmpURL});


		try
		{
			while (true) {
				System.out.print("> ");
				String java = reader.getNestedString();

				if((java != null) && java.startsWith("print "))
				{
					StringBuffer str = new StringBuffer();

					String expr = java.substring(6);
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

					java = str.toString();
				}

				if (java != null) {
					boolean declarnCheck = isDeclaration(tmpDirPath,java);

					if (classNumber == 0) {
						String classNum = Integer.toString(classNumber);
						if (declarnCheck) {
							String content = getCode(className + classNum, null, java, null);
							writeFile(tmpDirPath, className + classNum, content);
							String errormsg = compile(tmpDirPath,className + classNum);
							if (errormsg.equals(""))
								exec(loader, className + classNum, "exec");
							else
								System.err.println(errormsg);

						} else {
							String content = getCode(className + classNum, null, null, java);
							writeFile(tmpDirPath, className + classNum, content);
							String errormsg = compile(tmpDirPath,className + classNum);
							if (errormsg.equals(""))
								exec(loader, className + classNum, "exec");
							else
								System.err.println(errormsg);

						}
					} else {
						String classNum = Integer.toString(classNumber);
						String superClassNum = Integer.toString(classNumber - 1);
						if (declarnCheck) {
							String content = getCode(className + classNum, className + superClassNum, java, null);
							writeFile(tmpDirPath, className + classNum, content);
							String errormsg = compile(tmpDirPath,className + classNum);
							if (errormsg.equals(""))
								exec(loader, className + classNum, "exec");
							else
								System.err.println(errormsg);
						} else {
							String content = getCode(className + classNum, className + superClassNum, null, java);
							writeFile(tmpDirPath, className + classNum, content);
							String errormsg = compile(tmpDirPath,className + classNum);
							if (errormsg.equals(""))
								exec(loader, className + classNum, "exec");
							else
								System.err.println(errormsg);
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

	public static String getCode(String className, String extendSuper, String def, String stat)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("import java.io.*;"+"\n");
		builder.append("import java.util.*;"+"\n");
		builder.append("\n\n");

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

	public static boolean isDeclaration(String dir,String line) throws Exception
	{
		String content = getCode("Bogus",null,line,null);
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

	public static String compile(String dir,String fileName) throws IOException
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
		StringBuffer errormsg = new StringBuffer();
		if (!success) {
			List<Diagnostic<? extends JavaFileObject>> diagnosticsErrors = diagnostics.getDiagnostics();
			int errorSize =  diagnosticsErrors.size();
			int count = 1;

			for (Diagnostic<? extends JavaFileObject> diagnosticError : diagnosticsErrors) {
				// read error details from the diagnostic object
				errormsg.append("line ");
				errormsg.append(diagnosticError.getLineNumber());
				errormsg.append(": ");
				errormsg.append(diagnosticError.getMessage(null));
				if(count != errorSize)
					errormsg.append("\n");

				count++;
			}

		}
		fileManager.close();
		return errormsg.toString();
	}

	public static void createTempDirectory() throws IOException
	{
		File dir = new File("tmp");

		if(!dir.exists())
			dir.mkdir();
	}

	public static String createTempDirectory1() throws IOException
	{
		Path tempDir = Files.createTempDirectory("tmp_");
		return tempDir.toString();
	}

	public static void exec(ClassLoader loader, String className, String methodName) throws Exception
	{
        Class cl = loader.loadClass(className);
		Method method = cl.getMethod(methodName);
		method.invoke(null);
	}
}
