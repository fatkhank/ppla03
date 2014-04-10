/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint.transact;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

/**
 *
 * @author hamba v7
 */
@WebServlet(name = "image", urlPatterns = {"/image"})
public class Image extends HttpServlet {
    
    public static String res = "empty";

    final String url = "jdbc:mysql://localhost/collapaint";
    final String uname = "root";
    final String pwd = "";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println(res);
            
            
//            JsonObjectBuilder job = Json.createObjectBuilder();
//            job.add("lan", 2);
//            job.add("canvas_id", 2);
//
//            JsonArrayBuilder ojab = Json.createArrayBuilder();
//
//            ojab.add(Json.createObjectBuilder().add("id", "4").add("code", "r").
//                    add("geom", "geom1").add("style", "sty1").add("tran", "wo"));
//            ojab.add(Json.createObjectBuilder().add("id", "4").add("code", "c").
//                    add("geom", "geom2").add("style", "sty2").add("tran", "wo"));
//
////            job.add("object", ojab);
//
//            JsonArrayBuilder ajab = Json.createArrayBuilder();
//
//            ajab.add(Json.createObjectBuilder().add("id", 0).add("code", "w").
//                    add("param", "paramamama"));
//            ajab.add(Json.createObjectBuilder().add("id", 0).add("code", "d").
//                    add("param", ""));
//            ajab.add(Json.createObjectBuilder().add("id", 1).add("code", "w").
//                    add("param", "draw again"));
//
//  //          job.add("action", ajab);
//            
//            char cd = 'd';
//            job.add("char", cd);
//
//            out.println(job.build().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            out.write("error:" + ex.toString());
            Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
