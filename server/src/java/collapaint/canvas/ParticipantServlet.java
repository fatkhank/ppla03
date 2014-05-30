package collapaint.canvas;

import collapaint.DB;
import collapaint.DB.Participation.Q;
import collapaint.code.ParticipantJCode;
import collapaint.code.ParticipantJCode.Reply;
import collapaint.code.ParticipantJCode.Request;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
@WebServlet(name = "participant", urlPatterns = {"/participant"})
public class ParticipantServlet extends HttpServlet {

    private MysqlDataSource dataSource;

    @Override
    public void init() throws ServletException {
        dataSource = new MysqlDataSource();
        DB.init(dataSource, getServletContext());
    }

    /**
     * Memproses request
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
                int canvasId = request.getInt(Request.CANVAS_ID);
                int userId = request.getInt(Request.USER_ID);
                if (action == Request.Action.INVITE) {
                    String email = request.getString(Request.USER_EMAIL).toLowerCase();
                    invite(conn, reply, email, userId, canvasId);
                } else if (action == Request.Action.KICK) {
                    int kickerId = request.getInt(Request.KICKER_ID);
                    kick(conn, reply, userId, kickerId, canvasId);
                } else if (action == Request.Action.LIST) {
                    list(conn, reply, canvasId, userId);
                } else if (action == Request.Action.RESPONSE) {
                    int resp = request.getInt(Request.RESPONSE);
                    response(conn, reply, canvasId, userId, resp);
                }
            } catch (NullPointerException | ClassCastException | SQLException ex) {
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.BAD_REQUEST);
            }
            out.println(reply.build());
        }
    }

    /**
     * Mengundang seorang user untuk berpartisipasi pada suatu kanvas.
     *
     * @param conn
     * @param reply
     * @param email
     * @param inviterId user yang mengundang
     * @param canvasId
     */
    private void invite(Connection conn, JsonObjectBuilder reply, String email, int inviterId, int canvasId) {
        //cek apakah si pengundang adalah pemilik kanvas atau bukan
        try (PreparedStatement ownerCheck = conn.prepareStatement(DB.Canvas.Q.SELECT_OWNEROF)) {
            ownerCheck.setInt(DB.Canvas.Q.SELECT_OWNEROF_CANVASID, canvasId); //cari berdasar id kanvas

            ResultSet ownerResult = ownerCheck.executeQuery();
            if (!ownerResult.next()) {
                //kanvas dengan id yang disebutkan tidak ditemukan
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.CANVAS_NOT_FOUND);
                return;
            }

            //kanvas ditemukan, cek apakah yang mengundang adalah owner atau bukan.
            int ownerId = ownerResult.getInt(DB.Canvas.Q.SELECT_OWNEROF_RESULT_OWNERID);
            if (ownerId != inviterId) {
                //yang mengundang bukanlah owner, berarti tidak mempunyai hak mengundang
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //cek user yang diundang sudah terdaftar atau belum
            try (PreparedStatement userCheck = conn.prepareStatement(DB.User.Q.SELECT_BYEMAIL)) {
                userCheck.setString(DB.User.Q.SELECT_BYEMAIL_EMAIL, email);  //cari berdasar email

                //masukkan alamat email yang diundang
                reply.add(Reply.USER_EMAIL, email);

                ResultSet userResult = userCheck.executeQuery();
                if (!userResult.next()) {
                    //user yang diundang belum terdaftar
                    reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.USER_UNREGISTERED);
                    return;
                }

                //user yang diundang sudah terdaftar
                int userId = userResult.getInt(DB.User.Q.SELECT_BYEMAIL_RESULT_ID);

                //cek apakah data partisipasi user di kanvas tersebut ada atau tidak
                try (PreparedStatement inviteCheck = conn.prepareStatement(Q.CHECK_PARTICIPATION)) {
                    inviteCheck.setInt(Q.CHECK_PARTICIPATION_USERID, userId);
                    inviteCheck.setInt(Q.CHECK_PARTICIPATION_CANVASID, canvasId);

                    ResultSet inviteResult = inviteCheck.executeQuery();
                    if (inviteResult.next()) {
                        //user sudah berpartisipasi pada kanvas -> cek statusnya
                        String status = inviteResult.getString(Q.CHECK_PARTICIPATION_RESULT_STATUS);
                        if (status.equals(DB.Participation.Status.INVITATION))
                            //user memang sedang diundang ke kanvas tersebut
                            reply.add(Reply.INVITE_STATUS, Reply.InviteStatus.ALREADY_INVITED);
                        else
                            //user memang sudah bergabung di kanvas tersebut
                            reply.add(Reply.INVITE_STATUS, Reply.InviteStatus.ALREADY_JOINED);
                    } else {
                        //user belum terdaftar -> buat undanganya
                        String status = DB.Participation.Status.INVITATION;
                        if (addParticipation(conn, userId, canvasId, status)) {
                            //undangan berhasil dibuat
                            reply.add(Reply.INVITE_STATUS, Reply.InviteStatus.SUCCESS);
                        } else
                            //undangan gagal dibuat
                            reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.SERVER_ERROR);
                    }
                }
            }
        } catch (SQLException ex) {
            reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Membuang seorang user dari daftar partisipan.
     *
     * @param conn
     * @param reply
     * @param userId
     * @param canvasId
     */
    private void kick(Connection conn, JsonObjectBuilder reply, int userId, int kickerID, int canvasId) {
        //cek apakah si kicker adalah owner kanvas atau bukan
        try (PreparedStatement authCheck = conn.prepareStatement(DB.Canvas.Q.SELECT_OWNEROF)) {
            authCheck.setInt(DB.Canvas.Q.SELECT_OWNEROF_CANVASID, canvasId);
            ResultSet authResult = authCheck.executeQuery();
            if (!authResult.next()) {
                //data kanvas tidak ditemukan
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.CANVAS_NOT_FOUND);
                return;
            }

            //data kanvas ada -> periksa apakah user berwenang mengkick atau tidak
            int ownerId = authResult.getInt(DB.Canvas.Q.SELECT_OWNEROF_RESULT_OWNERID);

            if ((ownerId != kickerID && kickerID != userId) ||//selain owner han yaboleh me-kick dirinya sendiri
                    (ownerId == kickerID && userId == kickerID)) {//owner boleh me-kick selain dirinya                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //kicker adalah owner -> lakukan kick
            try (PreparedStatement delete = conn.prepareStatement(Q.DELETE_RECORD)) {
                delete.setInt(Q.DELETE_RECORD_USERID, userId);
                delete.setInt(Q.DELETE_RECORD_CANVASID, canvasId);
                if (delete.executeUpdate() > 0) {
                    //operasi kick sukses
                    reply.add(Reply.KICK_STATUS, Reply.KickStatus.SUCCESS);
                } else
                    //operasi kick gagal karena yang dikick bukanlah member
                    reply.add(Reply.KICK_STATUS, Reply.KickStatus.NOT_A_MEMBER);
            }
        } catch (SQLException ex) {
            reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Mengambil daftar partisipan pada suatu kanvas. Uesr yang melihat harus merupakan partisipan dari kanvas tersebut.
     *
     * @param conn
     * @param reply
     * @param canvasId
     * @param userId
     */
    private void list(Connection conn, JsonObjectBuilder reply, int canvasId, int userId) {
        //cek apakah user yang meminta berpartisipasi pada kanvas tersebut atau tidak
        try (PreparedStatement check = conn.prepareStatement(Q.CHECK_PARTICIPATION)) {
            check.setInt(Q.CHECK_PARTICIPATION_CANVASID, canvasId);
            check.setInt(Q.CHECK_PARTICIPATION_USERID, userId);

            if (!check.executeQuery().next()) {
                //user tidak berpartisipasi pada kanvas, berarti tidak berwenang melihat daftar partisipan.
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //user berwenang -> ambil daftar partisipan dari kanvas
            try (PreparedStatement getList = conn.prepareStatement(Q.SELECT_BYCANVAS)) {
                getList.setInt(Q.SELECT_BYCANVAS_CANVASID, canvasId);

                ResultSet result = getList.executeQuery();
                JsonArrayBuilder array = Json.createArrayBuilder();
                while (result.next()) {
                    //masukkan data partisipan satu persatu
                    JsonObjectBuilder user = Json.createObjectBuilder();
                    user.add(Reply.USER_ID, result.getInt(Q.SELECT_BYCANVAS_RESULT_USERID));
                    user.add(Reply.USER_NAME, result.getString(Q.SELECT_BYCANVAS_RESULT_USERNAME));
                    user.add(Reply.LAST_ACCESS, result.getString(Q.SELECT_BYCANVAS_RESULT_LASTACCESS));

                    //terjemahkan status partisipan
                    String status = result.getString(Q.SELECT_BYCANVAS_RESULT_STATUS);
                    switch (status) {
                        case DB.Participation.Status.OWNER:
                            user.add(Reply.PARTICIPANT_STATUS, Reply.ParticipantStatus.OWNER);
                            break;
                        case DB.Participation.Status.MEMBER:
                            user.add(Reply.PARTICIPANT_STATUS, Reply.ParticipantStatus.MEMBER);
                            break;
                        case DB.Participation.Status.INVITATION:
                            user.add(Reply.PARTICIPANT_STATUS, Reply.ParticipantStatus.INVITATION);
                            break;
                    }

                    //terjemahkan aksi ang dilakukan partisipan
                    String action = result.getString(Q.SELECT_BYCANVAS_RESULT_ACTION);
                    if (action.equals(DB.Participation.Action.OPEN))
                        user.add(Reply.PARTICIPANT_ACTION, Reply.PARTICIPANT_ACTION_OPEN);
                    else
                        user.add(Reply.PARTICIPANT_ACTION, Reply.PARTICIPANT_ACTION_CLOSE);
                    //masukkan data partisipan ke daftar partisipan
                    array.add(user);
                }
                reply.add(Reply.PARTICIPANT_LIST, array);
            }
        } catch (SQLException ex) {
            reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Meresponse pada suatu undangan.
     *
     * @param conn
     * @param reply
     * @param canvasId
     * @param userId
     * @param response
     */
    private void response(Connection conn, JsonObjectBuilder reply, int canvasId, int userId, int response) {
        //cek apakah data undangan user masuk pada kanvas tersebu ada atau tidak
        try (PreparedStatement check = conn.prepareStatement(Q.CHECK_PARTICIPATION)) {
            check.setInt(Q.CHECK_PARTICIPATION_USERID, userId);
            check.setInt(Q.CHECK_PARTICIPATION_CANVASID, canvasId);
            //masukkan data request
            reply.add(Reply.USER_ID, userId);
            reply.add(Reply.CANVAS_ID, canvasId);

            ResultSet checkResult = check.executeQuery();
            if (!checkResult.next()) {
                //jika user tidak diinvite atau kanvas tidak ada, maka tidak berhak meresponse
                reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.NOT_AUTHORIZED);
                return;
            }

            //user ada di tabel partisipan -> cek statusnya
            String status = checkResult.getString(Q.CHECK_PARTICIPATION_RESULT_STATUS);
            if (status.equals(DB.Participation.Status.INVITATION)) {
                //user memang sedang diundang -> jalankan response user
                if (response == Request.Response.ACCEPT) {
                    //Jika user menyetujui undangan, maka ubah status partisipasi menjadi member
                    try (PreparedStatement update = conn.prepareStatement(Q.UPDATE_STATUS)) {
                        update.setInt(Q.UPDATE_STATUS_USERID, userId);
                        update.setInt(Q.UPDATE_STATUS_CANVASID, canvasId);
                        update.setString(Q.UPDATE_STATUS_STATUS, DB.Participation.Status.MEMBER);
                        update.executeUpdate();
                        //undangan berhasil disetujui
                        reply.add(Reply.RESPONSE_STATUS, Reply.ResponseStatus.SUCCESS);
                    }
                } else {
                    //Jika user menolak undangan, maka hapus data undangan
                    try (PreparedStatement delete = conn.prepareStatement(Q.DELETE_RECORD)) {
                        delete.setInt(Q.DELETE_RECORD_USERID, userId);
                        delete.setInt(Q.DELETE_RECORD_CANVASID, canvasId);
                        delete.executeUpdate();
                        //undangan berhasil dihapus
                        reply.add(Reply.RESPONSE_STATUS, Reply.ResponseStatus.SUCCESS);
                    }
                }
            } else
                //user ada di tabel sebagai anggota
                reply.add(Reply.RESPONSE_STATUS, Reply.ResponseStatus.ALREADY_JOINED);
        } catch (SQLException ex) {
            reply.add(ParticipantJCode.ERROR, ParticipantJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Menambahkan data partisipasi user ke kanvas
     *
     * @param conn
     * @param userId
     * @param canvasId
     * @param status
     * @return berhasil atau tidak
     * @throws SQLException
     */
    public static boolean addParticipation(Connection conn, int userId, int canvasId, String status) throws SQLException {
        try (PreparedStatement invitation = conn.prepareCall(Q.INSERT_ALL)) {
            invitation.setInt(Q.INSERT_ALL_USERID, userId);
            invitation.setInt(Q.INSERT_ALL_CANVASID, canvasId);
            Date today = new Date(System.currentTimeMillis());
            invitation.setObject(Q.INSERT_ALL_LASTACCESS, today);
            invitation.setString(Q.INSERT_ALL_STATUS, status);
            invitation.setString(Q.INSERT_ALL_ACTION, DB.Participation.Action.CLOSE);

            return invitation.executeUpdate() > 0;
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
