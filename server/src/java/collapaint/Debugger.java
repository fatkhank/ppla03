/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint;

import java.io.PrintWriter;

/**
 *
 * @author hamba v7
 */
public class Debugger {

    public static void trace(Exception ex, PrintWriter out) {
        StackTraceElement[] ste = ex.getStackTrace();
        out.println("<pre>-------------------------------");
        out.println(ex);
        for (StackTraceElement ste1 : ste) {
            out.println(ste1.toString());
        }
        out.println("-------------------------------</pre>");
    }
}
