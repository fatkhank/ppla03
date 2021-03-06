package collapaint.auth;

import collapaint.DB;
import collapaint.code.UserJCode;
import collapaint.code.UserJCode.Reply;
import collapaint.code.UserJCode.Request;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
public class UserServlet extends HttpServlet {

    private MysqlDataSource dataSource;

    @Override
    public void init() throws ServletException {
        dataSource = new MysqlDataSource();
        DB.init(dataSource, getServletContext());
    }

    /**
     * Memproses request.
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
            try (Connection conn = dataSource.getConnection()) {
                JsonObject request = Json.createReader(is).readObject();
                int action = request.getInt(Request.ACTION);
                if (action == Request.ACTION_CHECK) {
                    String email = request.getString(Request.USER_EMAIL);
                    String username = request.getString(Request.USER_NAME);
                    check(conn, reply, email, username);
                } else if (action == Request.ACTION_LOGOUT) {
                    int userId = request.getInt(Request.USER_ID);
                    logout(conn, reply, userId);
                } else
                    reply.add(UserJCode.ERROR, UserJCode.Error.BAD_REQUEST);
            } catch (NullPointerException | ClassCastException | SQLException ex) {
                reply.add(UserJCode.ERROR, UserJCode.Error.SERVER_ERROR);
            }
            out.println(reply.build());
        }
    }

    /**
     * Mencoba login dengan email dan username tertentu. Jika akun belum ada, maka akan dibuat dulu.
     *
     * @param conn
     * @param reply
     * @param email
     * @param username
     */
    private void check(Connection conn, JsonObjectBuilder reply, String email, String username) {
        //periksa apakah akun sudah ada atau belum
        try (PreparedStatement checkAccount = conn.prepareStatement(DB.User.Q.SELECT_BYEMAIL)) {
            checkAccount.setString(DB.User.Q.SELECT_BYEMAIL_EMAIL, email);
            ResultSet checkResult = checkAccount.executeQuery();
            if (checkResult.next()) {
                //ambil id user
                reply.add(Reply.USER_ID, checkResult.getInt(DB.User.Q.SELECT_BYEMAIL_RESULT_ID));
                //email sudah terdaftar, ubah status jadi login
                int userId = checkResult.getInt(DB.User.Q.SELECT_BYEMAIL_RESULT_ID);
                try (PreparedStatement changeStatus = conn.prepareStatement(DB.User.Q.UPDATE_STATUS_BYID)) {
                    changeStatus.setInt(DB.User.Q.UPDATE_STATUS_BYID_USERID, userId);
                    changeStatus.setString(DB.User.Q.UPDATE_STATUS_BYID_STATUS, DB.User.Status.LOGIN);
                    changeStatus.execute();
                    reply.add(Reply.STATUS, Reply.ACCOUNT_EXIST);
                }
            } else {
                //jika email belum terdaftar, buat akun baru dengan status sudah login
                try (PreparedStatement create = conn
                        .prepareStatement(DB.User.Q.INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    create.setString(DB.User.Q.INSERT_USER_ACCOUNTID, email);
                    create.setString(DB.User.Q.INSERT_USER_NAME, username);
                    create.setString(DB.User.Q.INSERT_USER_STATUS, DB.User.Status.LOGIN);
                    create.execute();
                    //akun berhasil dibuat
                    reply.add(Reply.STATUS, Reply.ACCOUNT_CREATED);
                    //ambil key
                    ResultSet key = create.getGeneratedKeys();
                    key.next();
                    reply.add(Reply.USER_ID, key.getInt(1));
                }
            }
        } catch (SQLException ex) {
            reply.add(UserJCode.ERROR, UserJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Melogout user.
     *
     * @param conn
     * @param reply
     * @param userID
     */
    private void logout(Connection conn, JsonObjectBuilder reply, int userID) {
        //ubah status user jadi logout
        try (PreparedStatement logout = conn.prepareStatement(DB.User.Q.UPDATE_STATUS_BYID)) {
            logout.setInt(DB.User.Q.UPDATE_STATUS_BYID_USERID, userID);
            logout.setString(DB.User.Q.UPDATE_STATUS_BYID_STATUS, DB.User.Status.LOGOUT);
            logout.execute();
            reply.add(Reply.STATUS, Reply.LOGOUT_SUCESS);
        } catch (SQLException ex) {
            reply.add(UserJCode.ERROR, UserJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Mengecek apakah seorang user ada atau tidak
     *
     * @param conn
     * @param userId
     * @return
     * @throws SQLException
     */
    public static final boolean exist(Connection conn, int userId) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(DB.User.Q.SELECT_BYID)) {
            check.setInt(DB.User.Q.SELECT_BYID_ID, userId);
            if (check.executeQuery().next())
                return true;
        }
        return false;
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
        processRequest(new com.sun.xml.bind.StringInputStream(request.
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
