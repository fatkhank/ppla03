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
import java.sql.SQLIntegrityConstraintViolationException;
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
 * Menangani proses pembuatan kanvas baru oleh seorang user.
 *
 * @author hamba v7
 */
@WebServlet(name = "create", urlPatterns = {"/create"})
public class Create extends HttpServlet {

    static class CreateJCode {

        //--- request ---
        static final String OWNER_ID = "oid";
        static final String CANVAS_NAME = "name";
        static final String CANVAS_WIDTH = "width";
        static final String CANVAS_HEIGHT = "height";
        //--- reply ---
        static final String CANVAS_ID = "id";
        static final String RESULT_ERROR = "error";
        static final int DUPLICATE_NAME = 2;
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

            JsonObject request = Json.createReader(is).readObject();
            JsonObjectBuilder reply = Json.createObjectBuilder();

            int ownerID = request.getInt(CreateJCode.OWNER_ID);
            String name = request.getString(CreateJCode.CANVAS_NAME);
            int width = request.getInt(CreateJCode.CANVAS_WIDTH);
            int height = request.getInt(CreateJCode.CANVAS_HEIGHT);

            StringBuilder sb = new StringBuilder();
            sb.append("insert into canvas(owner,name,width,height) values ");
            sb.append("('").append(ownerID).append("','").append(name).
                    append("','").append(width).append("','").append(height).
                    append("');");

            try (Statement statement = connection.createStatement()) {
                statement.
                        execute(sb.toString(), Statement.RETURN_GENERATED_KEYS);
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next())
                    reply.add(CreateJCode.CANVAS_ID, keys.getInt(1));
                reply.add(CreateJCode.CANVAS_NAME, name);
            } catch (SQLIntegrityConstraintViolationException ex) {
                reply.add(CreateJCode.RESULT_ERROR, CreateJCode.DUPLICATE_NAME);
            } catch (Exception ex) {
                reply.add(CreateJCode.RESULT_ERROR, "error");
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
//        processRequest(new StringInputStream(request.
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
