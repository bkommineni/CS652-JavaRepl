package cs652.repl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;

public class JavaREPL {
	public static void main(String[] args) throws IOException {
		exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;
		while (true) {
			System.out.print("> ");
			String java = reader.getNestedString();
			System.out.println(java);
			// TODO
		}
	}
}
