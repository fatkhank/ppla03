package collapaint.canvas;

import collapaint.code.CanvasJCode.Request;
import collapaint.DB;
import collapaint.DB.Canvas.Q;
import collapaint.auth.UserServlet;
import collapaint.code.CanvasJCode;
import collapaint.code.CanvasJCode.Reply;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hamba v7
 */
@WebServlet(name = "canvas", urlPatterns = {"/canvas"})
public class CanvasServlet extends HttpServlet {

    private MysqlDataSource dataSource;

    @Override
    public void init() throws ServletException {
        dataSource = new MysqlDataSource();
        DB.init(dataSource, getServletContext());
    }

    /**
     * Memroses request
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
                int userID = request.getInt(Request.USER_ID);
                //putuskan penanganan request berdasar kode aksi
                int action = request.getInt(Request.ACTION);
                if (action == Request.Action.LIST) {
                    //mengambil daftar kanvas
                    list(conn, reply, userID);
                } else if (action == Request.Action.CREATE) {
                    //membuat kanvas baru
                    String name = request.getString(Request.CANVAS_NAME);
                    int width = request.getInt(Request.CANVAS_WIDTH);
                    int height = request.getInt(Request.CANVAS_HEIGHT);
                    int top = request.getInt(Request.CANVAS_TOP, 0);
                    int left = request.getInt(Request.CANVAS_LEFT, 0);
                    create(conn, reply, userID, name, width, height, top, left);
                } else if (action == Request.Action.DELETE) {
                    //menghapus suatu kanvas
                    int canvasId = request.getInt(Request.CANVAS_ID);
                    delete(conn, reply, userID, canvasId);
                } else
                    reply.add(CanvasJCode.ERROR, CanvasJCode.Error.BAD_REQUEST);
            } catch (NullPointerException | ClassCastException | JsonParsingException ex) {
                reply.add(CanvasJCode.ERROR, CanvasJCode.Error.BAD_REQUEST);
            } catch (SQLException ex) {
                reply.add(CanvasJCode.ERROR, CanvasJCode.Error.SERVER_ERROR);
            }
            out.println(reply.build().toString());
        }
    }

    /**
     * Membuat sebuah kanvas baru. Pengguna harus terdaftar.
     *
     * @param conn
     * @param reply
     * @param userId
     * @param canvasName
     * @param width
     * @param height
     * @param top
     * @param left
     */
    private void create(Connection conn, JsonObjectBuilder reply, int userId, String canvasName, int width, int height,
            int top, int left) {
        try {
            //cek apakah pengguna terdaftar atau tidak
            if (!UserServlet.exist(conn, userId)) {
                //pengguna tidak terdaftar
                reply.add(CanvasJCode.ERROR, CanvasJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //pengguna terdaftar -> buat kanvas baru
            try (PreparedStatement create = conn
                    .prepareStatement(Q.INSERT_CANVAS, PreparedStatement.RETURN_GENERATED_KEYS)) {
                create.setString(Q.INSERT_CANVAS_NAME, canvasName);
                create.setInt(Q.INSERT_CANVAS_WIDTH, width);
                create.setInt(Q.INSERT_CANVAS_HEIGHT, height);
                create.setInt(Q.INSERT_CANVAS_TOP, top);
                create.setInt(Q.INSERT_CANVAS_LEFT, left);
                create.setInt(Q.INSERT_CANVAS_OWNER_ID, userId);
                create.setObject(Q.INSERT_CANVAS_CREATE_TIME, new Date(System.currentTimeMillis()));
                create.execute();

                ResultSet keys = create.getGeneratedKeys();
                if (keys.next()) {
                    int canvasId = keys.getInt(1);
                    reply.add(Reply.CANVAS_ID, canvasId);
                    reply.add(Reply.CANVAS_NAME, canvasName);

                    //tambahkan data partisipasi user pada kanvas sebagai owner
                    ParticipantServlet.addParticipation(conn, userId, canvasId, DB.Participation.Status.OWNER);
                } else
                    reply.add(CanvasJCode.ERROR, CanvasJCode.Error.SERVER_ERROR);
            } catch (SQLIntegrityConstraintViolationException ex) {
                reply.add(CanvasJCode.ERROR, CanvasJCode.Error.DUPLICATE_NAME);
            }
        } catch (SQLException ex) {
            reply.add(CanvasJCode.ERROR, CanvasJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Menghapus suatu kanvas. Pengguna harus sebagai owner.
     *
     * @param conn
     * @param reply
     * @param userId id user yang menghapus
     * @param canvasId id kanvas yang dihapus
     */
    private void delete(Connection conn, JsonObjectBuilder reply, int userId, int canvasId) {
        //cek apakah user adalah owner kanvas atau bukan
        try (PreparedStatement check = conn.prepareStatement(Q.SELECT_OWNEROF)) {
            check.setInt(Q.SELECT_OWNEROF_CANVASID, canvasId);
            ResultSet checkResult = check.executeQuery();
            if (!checkResult.next()) {
                //data kanvas tidak ditemukan
                reply.add(Reply.DELETE_STATUS, CanvasJCode.Error.CANVAS_NOT_FOUND);
                return;
            }

            //data kanvas ditemukan -> cek owner atau bukan
            int ownerId = checkResult.getInt(Q.SELECT_OWNEROF_RESULT_OWNERID);
            if (ownerId != userId) {
                //user bukan owner -> tidak berwenang
                reply.add(Reply.DELETE_STATUS, CanvasJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //user adalah owner -> hapus kanvas
            try (PreparedStatement delete = conn.prepareStatement(Q.DELETE_BYID)) {
                delete.setInt(Q.DELETE_BYID_CANVASID, canvasId);
                if (delete.executeUpdate() > 0)
                    reply.add(Reply.DELETE_STATUS, Reply.DELETE_STATUS_SUCCESS);
                else
                    reply.add(CanvasJCode.ERROR, CanvasJCode.Error.CANVAS_NOT_FOUND);
            }
        } catch (SQLException ex) {
            reply.add(CanvasJCode.ERROR, CanvasJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Mengambil daftar kanvas yang berhubungan dengan seorang user.
     *
     * @param conn
     * @param reply
     * @param userId id user
     */
    private void list(Connection conn, JsonObjectBuilder reply, int userId) {
        //ambil daftar  partisipasi canvas
        try (PreparedStatement participation = conn.prepareStatement(DB.Q.CANVAS_BY_USER)) {
            participation.setInt(DB.Q.CANVAS_BY_USER_USERID, userId);
            ResultSet result = participation.executeQuery();
            JsonArrayBuilder inviteList = Json.createArrayBuilder();
            JsonArrayBuilder oldList = Json.createArrayBuilder();
            JsonArrayBuilder ownList = Json.createArrayBuilder();
            while (result.next()) {
                JsonObjectBuilder canvas = Json.createObjectBuilder();
                canvas.add(Reply.CANVAS_ID, result.getString(DB.Q.CANVAS_BYUSER_RESULT_CANVASID));
                canvas.add(Reply.CANVAS_NAME, result.getString(DB.Q.CANVAS_BYUSER_RESULT_CANVASNAME));
                canvas.add(Reply.CANVAS_WIDTH, result.getString(DB.Q.CANVAS_BYUSER_RESULT_CANVASWIDTH));
                canvas.add(Reply.CANVAS_HEIGHT, result.getString(DB.Q.CANVAS_BYUSER_RESULT_CANVASHEIGHT));
                canvas.add(Reply.OWNER_ID, result.getString(DB.Q.CANVAS_BYUSER_RESULT_OWNERID));
                canvas.add(Reply.OWNER_NAME, result.getString(DB.Q.CANVAS_BYUSER_RESULT_OWNERNAME));
                canvas.add(Reply.LAST_ACCESS, result.getString(DB.Q.CANVAS_BYUSER_RESULT_LASTACCESS));

                //pisahkan berdasarkan status partisipasi
                String status = result.getString(DB.Q.CANVAS_BYUSER_RESULT_STATUS);
                switch (status) {
                    case DB.Participation.Status.MEMBER:
                        oldList.add(canvas);
                        break;
                    case DB.Participation.Status.INVITATION:
                        inviteList.add(canvas);
                        break;
                    case DB.Participation.Status.OWNER:
                        ownList.add(canvas);
                        break;
                }
            }
            reply.add(Reply.OLD_LIST, oldList);
            reply.add(Reply.INVITATION_LIST, inviteList);
            reply.add(Reply.OWNED_LIST, ownList);
        } catch (SQLException ex) {
            reply.add(CanvasJCode.ERROR, CanvasJCode.Error.SERVER_ERROR);
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
