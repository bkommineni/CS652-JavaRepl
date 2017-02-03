package cs652.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

/**
 * NestedReader class to handle the input statements
 * having nested parentheses,braces,brackets,and ignoring the
 * input after comments symbol from java and handling end of file and
 * line terminator characters.
 * Starter code for this class is taken from the documentation of this project
 * provided by Prof.Terrence Parr
 * @bhargavi
 */
public class NestedReader {

    private StringBuilder buf = new StringBuilder();
    private BufferedReader input;
    private int c;

    /**
     * Method which gets the standard input from console from
     * JavaREPL class to process and accept the nested strings
     * with parentheses etc.
     * @param input input string
     */
    public NestedReader(BufferedReader input) {
        this.input = input;
    }

    /**
     * Method which handles the input processing based on the
     * input characters.
     * @return returns the processed input string based on requirements.
     * @throws IOException
     */
    public String getNestedString() throws IOException {

        Stack<Character> nestedChars = new Stack();
        c = 0;

        while (true)
        {
            if(c != -1)
            {
                char check = (char) c;

                switch (check)
                {
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
                        if(!nestedChars.empty())
                        {
                            if (nestedChars.pop() != '}')
                            {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n') {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            }
                            else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case ']':
                        if(!nestedChars.empty())
                        {
                            if (nestedChars.pop() != ']')
                            {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n')
                                {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            }
                            else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case ')':
                        if(!nestedChars.empty())
                        {
                            if (nestedChars.pop() != ')')
                            {
                                buf.append((char) c);
                                while ((c = input.read()) != '\n')
                                {
                                    buf.append((char) c);
                                }
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            }
                            else
                                consume();
                        }
                        else
                            consume();
                        break;
                    case '\"':
                        buf.append((char) c);
                        while ((c = input.read()) != '\"')
                        {
                            buf.append((char) c);
                        }
                        consume();
                        break;
                    case '\'':
                        buf.append((char) c);
                        while ((c = input.read()) != '\'')
                        {
                            buf.append((char) c);
                        }
                        consume();
                        break;
                    case '\n':
                        if (nestedChars.empty())
                        {
                            String returnStr = buf.toString();
                            buf = new StringBuilder();
                            return returnStr;
                        }
                        else
                            consume();
                        break;
                    case '/':
                        int nextChar = input.read();
                        if (nextChar == '/')
                        {
                            StringBuilder builder = new StringBuilder();
                            while ((c = input.read()) != '\n')
                            {
                                builder.append((char) c);
                            }
                            if (nestedChars.empty())
                            {
                                String returnStr = buf.toString();
                                buf = new StringBuilder();
                                return returnStr;
                            }
                            else
                                consume();
                        }
                        else
                        {
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
    private void consume() throws IOException {

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
