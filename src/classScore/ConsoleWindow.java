package classScore;

/**
   @version 1.00 2000-06-07
   @author Cay Horstmann
 */

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
   A window that displays the bytes sent to System.out
   and System.err
 */
public class ConsoleWindow {
   public static void init() {
      JFrame frame = new JFrame();
      frame.setTitle("ConsoleWindow");
      final JTextArea output = new JTextArea();
      output.setEditable(false);
      frame.getContentPane().add(new JScrollPane(output));
      frame.setSize(WIDTH, HEIGHT);
      frame.setLocation(LEFT, TOP);
      frame.show();

      // define a PrintStream that sends its bytes to the
      // output text area
      PrintStream consoleStream = new PrintStream(new
                                                  OutputStream() {
         public void write(int b) {} // never called

         public void write(byte[] b, int off, int len) {
            output.append(new String(b, off, len));
         }
      });

      // set both System.out and System.err to that stream
      System.setOut(consoleStream);
      System.setErr(consoleStream);
   }

   public static final int WIDTH = 300;
   public static final int HEIGHT = 200;
   public static final int LEFT = 200;
   public static final int TOP = 200;
}
