/* $Id$ */

package ibis.io.jme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class SplitterException extends IOException {

    /** 
     * Generated
     */
    private static final long serialVersionUID = 9005051418523286737L;

    private Vector streams = new Vector();

    private Vector exceptions = new Vector();

    public SplitterException() {
        // empty constructor
    }

    public void add(OutputStream s, Exception e) {
        if (streams.contains(s)) {
            System.err.println("AAA, stream was already in splitter exception");
        }

        streams.add(s);
        exceptions.add(e);
    }

    public int count() {
        return streams.size();
    }

    public OutputStream[] getStreams() {
        return (OutputStream[])streams.toArray(new OutputStream[0]);
    }

    public Exception[] getExceptions() {
        return (Exception[])exceptions.toArray(new Exception[0]);
    }

    public OutputStream getStream(int pos) {
        return (OutputStream)streams.get(pos);
    }

    public Exception getException(int pos) {
        return (Exception)exceptions.get(pos);
    }

    public String toString() {
        String res = "got " + streams.size() + " exceptions: ";
        for (int i = 0; i < streams.size(); i++) {
            res += "   " + exceptions.get(i) + "\n";
        }

        return res;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace()
     */
    public void printStackTrace() {
        for (int i = 0; i < streams.size(); i++) {
            System.err.println("Exception: " + exceptions.get(i));
            ((Exception) exceptions.get(i)).printStackTrace();
        }
    }
}
