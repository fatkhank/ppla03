package collapaint.canvas;

import collapaint.DB;
import collapaint.Debugger;
import collapaint.transact.Action;
import com.sun.xml.bind.StringInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
 * Mendapatkan daftar User yang berpartisipasi pada suatu kanvas.
 *
 * @author hamba v7
 */
@WebServlet(name = "member", urlPatterns = {"/member"})
public class Participants extends HttpServlet {

    static class ParJCode {

        //request
        static final String CANVAS_ID = "cid";
        //reply
        static final String USER_ID = "id";
        static final String USER_NAME = "name";
        static final String USER_LIST = "pars";
        static final String OWNER_ID = "oid";
        static final String OWNER_NAME = "oname";
        static final String ERROR = "Error";
        static final int CANVAS_UNKNOWN = 3;
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

    static final String OWN_QUERY1 = "select u." + DB.USER.COL_ID + ", u." + DB.USER.COL_NAME + " from " + DB.TABLE_USER + " u, " + DB.TABLE_CANVAS + " c where c." + DB.CANVAS.COL_ID + " = '";
    static final String OWN_QUERY2 = "' and u." + DB.USER.COL_ID + " = c." + DB.CANVAS.COL_OWNER;

    static final String OTHER_QUERY1 = "select u." + DB.USER.COL_ID + ", u." + DB.USER.COL_NAME + " from " + DB.TABLE_USER + " u, " + DB.TABLE_PARTICIPATION + " p where p." + DB.PARTICIPATION.COL_CANVAS + " = '";
    static final String OTHER_QUERY2 = "' and u." + DB.USER.COL_ID + " = p." + DB.PARTICIPATION.COL_USER + ";";

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
        JsonObjectBuilder reply = Json.createObjectBuilder();
        try (PrintWriter out = response.getWriter()) {
            try (Statement statement = connection.createStatement()) {
                JsonObject request = Json.createReader(is).readObject();
                int canvas_id = request.getInt(ParJCode.CANVAS_ID);
                //---------------- CANVAS OWNER --------------------
                String ownQuery = OWN_QUERY1 + canvas_id + OWN_QUERY2;
                ResultSet result = statement.executeQuery(ownQuery);
                if (result.next()) {
                    reply.add(ParJCode.OWNER_ID, result.getInt(1));
                    reply.add(ParJCode.OWNER_NAME, result.getString(2));

                    String otherQuery = OTHER_QUERY1 + canvas_id + OTHER_QUERY2;
                    result = statement.executeQuery(otherQuery);
                    JsonArrayBuilder jab = Json.createArrayBuilder();
                    while (result.next()) {
                        JsonObjectBuilder user = Json.createObjectBuilder();
                        user.add(ParJCode.USER_ID, result.getInt(1));
                        user.add(ParJCode.USER_NAME, result.getString(2));
                        jab.add(user);
                    }
                    reply.add(ParJCode.USER_LIST, jab);
                } else
                    reply.add(ParJCode.ERROR, ParJCode.CANVAS_UNKNOWN);
            } catch (Exception ex) {
                reply.add(ParJCode.ERROR, 0);
            }
            out.print(reply.build().toString());
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
