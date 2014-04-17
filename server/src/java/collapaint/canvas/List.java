/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint.canvas;

import collapaint.DB;
import collapaint.transact.Action;
import com.sun.xml.bind.StringInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hamba v7
 */
@WebServlet(name = "list", urlPatterns = {"/list"})
public class List extends HttpServlet {

    static class ListJCode {

        //--- request ---
        static final String USER_ID = "uid";
        //--- reply ---
        static final String CANVAS_OWNED = "own";
        static final String CANVAS_OLD = "old";
        static final String CANVAS_NEW = "new";
        static final String CANVAS_ID = "i";
        static final String NAME = "n";
        static final String WIDTH = "w";
        static final String HEIGHT = "h";
        static final String OWNER_ID = "o";
        static final String OWNER_NAME = "on";
        static final String ERROR = "error";
    }

    Connection connection;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); //To change body of generated methods, choose Tools | Templates.
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(DB.URL, DB.USERNAME,
                    DB.PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static final String OWN_QUERY1 = "select * from " + DB.TABLE_CANVAS + " where " + DB.CANVAS.COL_OWNER + " = '";
    static final String OWN_QUERY2 = "';";

    static class OwnedQu {

        static final int CANVAS_ID = 1;
        static final int CANVAS_NAME = 2;
        static final int CANVAS_WIDTH = 3;
        static final int CANVAS_HEIGHT = 4;
    }

    static final String PAR_QUERY1 = "select p." + DB.PARTICIPATION.COL_CANVAS + ", p." + DB.PARTICIPATION.COL_STATUS + ", c." + DB.CANVAS.COL_NAME + ", c." + DB.CANVAS.COL_WIDTH + ", c." + DB.CANVAS.COL_HEIGHT + ", c." + DB.CANVAS.COL_OWNER + ", u." + DB.USER.COL_NAME + " from " + DB.TABLE_PARTICIPATION + " p, " + DB.TABLE_CANVAS + " c, " + DB.TABLE_USER + " u where p." + DB.PARTICIPATION.COL_USER + " = '";
    static final String PAR_QUERY2 = "' and c." + DB.CANVAS.COL_ID + " = p." + DB.PARTICIPATION.COL_CANVAS + " and u." + DB.USER.COL_ID + " = c." + DB.CANVAS.COL_OWNER + ";";

    static class PartiQu {

        static final int CANVAS_ID = 1;
        static final int STATUS = 2;
        static final int CANVAS_NAME = 3;
        static final int CANVAS_WIDTH = 4;
        static final int CANVAS_HEIGHT = 5;
        static final int OWNER_ID = 6;
        static final int OWNER_NAME = 7;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param is inputstream
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(InputStream is,
            HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            JsonObjectBuilder reply = Json.createObjectBuilder();
            JsonObject request = Json.createReader(is).readObject();
            int userID = request.getInt(ListJCode.USER_ID);

            try (Statement statement = connection.createStatement()) {
                //---------------- CANVAS OWNED --------------------
                String ownQuery = OWN_QUERY1 + userID + OWN_QUERY2;
                ResultSet result = statement.executeQuery(ownQuery);
                JsonArrayBuilder ownList = Json.createArrayBuilder();
                while (result.next()) {
                    JsonObjectBuilder canvas = Json.createObjectBuilder();
                    canvas.add(ListJCode.CANVAS_ID, result.
                            getInt(OwnedQu.CANVAS_ID));
                    canvas.add(ListJCode.NAME, result.
                            getString(OwnedQu.CANVAS_NAME));
                    canvas.add(ListJCode.WIDTH, result.
                            getInt(OwnedQu.CANVAS_WIDTH));
                    canvas.add(ListJCode.HEIGHT, result.
                            getInt(OwnedQu.CANVAS_HEIGHT));
                    ownList.add(canvas);
                }
                reply.add(ListJCode.CANVAS_OWNED, ownList);

                String parQuery = PAR_QUERY1 + userID + PAR_QUERY2;
                result = statement.executeQuery(parQuery);

                JsonArrayBuilder oldList = Json.createArrayBuilder();
                JsonArrayBuilder newList = Json.createArrayBuilder();
                while (result.next()) {
                    JsonObjectBuilder canvas = Json.createObjectBuilder();
                    canvas.add(ListJCode.CANVAS_ID, result.
                            getInt(PartiQu.CANVAS_ID));
                    canvas.add(ListJCode.NAME, result.
                            getString(PartiQu.CANVAS_NAME));
                    canvas.add(ListJCode.WIDTH, result.
                            getInt(PartiQu.CANVAS_WIDTH));
                    canvas.add(ListJCode.HEIGHT, result.
                            getInt(PartiQu.CANVAS_HEIGHT));
                    canvas.add(ListJCode.OWNER_ID, result.
                            getInt(PartiQu.OWNER_ID));
                    canvas.add(ListJCode.OWNER_NAME, result.
                            getString(PartiQu.OWNER_NAME));
                    if (result.getString(PartiQu.STATUS).equals("n"))
                        newList.add(canvas);
                    else
                        oldList.add(canvas);
                }

                reply.add(ListJCode.CANVAS_NEW, newList);
                reply.add(ListJCode.CANVAS_OLD, oldList);
            } catch (Exception ex) {
                reply.add(ListJCode.ERROR, ex.getMessage());
            }
            out.println(reply.build().toString());
        }
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
        processRequest(new StringInputStream(request.
                getParameter("json")), response);
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
        processRequest(request.getInputStream(), response);
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
