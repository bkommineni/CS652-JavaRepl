package cs652.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

/**
 * Created by bharu on 1/26/17.
 */
public class NestedReader {

    StringBuilder buf = new StringBuilder();
    BufferedReader input;
    int c;

    public NestedReader(BufferedReader input) {
        this.input = input;
    }
    public String getNestedString() throws IOException {

        Stack<Character> nestedChars = new Stack();
        c = 0;

        while (true) {
            if(c != -1) {
                char check = (char) c;

                switch (check) {
                    case '{':
                        nestedChars.push('}');
                        consume();
                        break;
                    case '[':
                        nestedChars.push(']');
                        consume();
                        break;
                    case '(':
                        nestedChars.push(')');
                        consume();
                        break;
                    case '}':
                        if(!nestedChars.empty()) {
                            if (nestedChars.pop() != '}') {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n') {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            } else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case ']':
                        if(!nestedChars.empty()) {
                            if (nestedChars.pop() != ']') {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n') {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            } else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case ')':
                        if(!nestedChars.empty()) {
                            if (nestedChars.pop() != ')') {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n') {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            } else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case '\n':
                        if (nestedChars.empty()) {
                            String returnStr = buf.toString();
                            buf = new StringBuilder();
                            return returnStr;
                        } else
                            consume();
                        break;
                    case '/':
                        int nextChar = input.read();
                        if (nextChar == '/') {
                            int ch;
                            StringBuilder builder = new StringBuilder();
                            while ((c = input.read()) != '\n') {
                                builder.append((char) c);
                            }
                            if (nestedChars.empty()) {
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            } else
                                consume();
                        } else {
                            buf.append(nextChar);
                            consume();
                        }
                        break;
                    default:
                        consume();
                        break;
                }
            }
            else
                return null;
        }
    }
    void consume() throws IOException {

        if(c == 0)
        {
            c = input.read();
        }
        else
        {
            buf.append((char) c);
            c = input.read();
        }
    }
}
