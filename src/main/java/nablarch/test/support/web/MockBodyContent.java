package nablarch.test.support.web;

import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * @author T.Kawasaki
 */
public class MockBodyContent extends BodyContent {

    private final String body;
    public MockBodyContent(String body) {
        super(new MockPageContext.MockJspWriter());
        this.body = body;
    }

    @Override
    public Reader getReader() {
        return new StringReader(body);
    }

    @Override
    public String getString() {
        return body;
    }

    @Override
    public void writeOut(Writer writer) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void newLine() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(boolean b) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(char c) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(int i) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(long l) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(float v) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(double v) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(char[] chars) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(String s) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void print(Object o) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(boolean b) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(char c) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(int i) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(long l) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(float v) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(double v) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(char[] chars) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(String s) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void println(Object o) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clearBuffer() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRemaining() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
