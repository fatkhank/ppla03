/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint.auth;

import collapaint.DB;
import collapaint.transact.Action;
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
@WebServlet(name = "user", urlPatterns = {"/user"})
public class User extends HttpServlet {

    static class UserJCode {

        static final String COLLA_ID = "id";
        static final String ACCOUNT_ID = "acid";
        static final String NAME = "name";
        //--- reply ---
        static final String ERROR = "error";
        static final String STATUS = "status";
        static final int NEW = 3;
        static final int EXIST = 9;
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

    static final String SELECT_QUERY1 = "select " + DB.USER.COL_ID + " from " + DB.TABLE_USER + " where " + DB.USER.COL_ACCOUNT_ID + "='";
    static final String SELECT_QUERY2 = "';";

    static final String INSERT_QUERY1 = "insert into " + DB.TABLE_USER + "(" + DB.USER.COL_ACCOUNT_ID + "," + DB.USER.COL_NAME + ") values('";
    static final String INSERT_QUERY2 = "','";
    static final String INSERT_QUERY3 = "');";

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

            try (Statement statement = connection.createStatement()) {
                JsonObject request = Json.createReader(is).readObject();
                String accID = request.getString(UserJCode.ACCOUNT_ID);
                String name = request.getString(UserJCode.NAME);
                String selectQu = SELECT_QUERY1 + accID + SELECT_QUERY2;
                ResultSet selectResult = statement.executeQuery(selectQu);
                if (selectResult.next()) {
                    reply.add(UserJCode.STATUS, UserJCode.EXIST);
                    reply.add(UserJCode.COLLA_ID, selectResult.getInt(1));
                } else {
                    String insertQuery = INSERT_QUERY1 + accID + INSERT_QUERY2 + name + INSERT_QUERY3;
                    statement.
                            execute(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ResultSet keys = statement.getGeneratedKeys();
                    if (keys.next())
                        reply.add(UserJCode.COLLA_ID, keys.getInt(1));
                    reply.add(UserJCode.STATUS, UserJCode.NEW);
                }

                reply.add(UserJCode.NAME, name);
            } catch (Exception ex) {
                reply.add(UserJCode.ERROR, "error");
            }

            out.println(reply.build().toString());
        }
    }

    public static void check(String id, String nickname) {

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
//        processRequest(new com.sun.xml.bind.StringInputStream(request.
//                getParameter("json")), response);
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
