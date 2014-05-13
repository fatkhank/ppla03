/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint.canvas;

import collapaint.DB;
import collapaint.code.PortalJCode;
import collapaint.code.PortalJCode.Reply;
import collapaint.code.PortalJCode.Request;
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
@WebServlet(name = "portal", urlPatterns = {"/portal"})
public class PortalServlet extends HttpServlet {

    private MysqlDataSource dataSource;

    @Override
    public void init() throws ServletException {
        dataSource = new MysqlDataSource();
        dataSource.setDatabaseName(DB.DB_NAME);
        dataSource.setURL(DB.DB_URL);
        dataSource.setUser(DB.DB_USERNAME);
        dataSource.setPassword(DB.DB_PASSWORD);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
                int userId = request.getInt(Request.USER_ID);
                int canvasId = request.getInt(Request.CANVAS_ID);

                //tangani aksi berdasarkan kode pada request
                int action = request.getInt(Request.ACTION);
                if (action == Request.ACTION_OPEN)
                    open(conn, reply, userId, canvasId);
                else if (action == Request.ACTION_CLOSE)
                    close(conn, reply, userId, canvasId);
                else
                    reply.add(PortalJCode.ERROR, PortalJCode.Error.BAD_REQUEST);
            } catch (NullPointerException | ClassCastException | JsonParsingException ex) {
                reply.add(PortalJCode.ERROR, PortalJCode.Error.BAD_REQUEST);
            } catch (SQLException ex) {
                reply.add(PortalJCode.ERROR, PortalJCode.Error.SERVER_ERROR);
            }

            out.print(reply.build());
        }
    }

    /**
     * Membuka sebuah kanvas.
     *
     * @param conn
     * @param reply
     * @param userId id user yang membuka kanvas.
     * @param canvasId id kanvas yang dibuka.
     */
    private void open(Connection conn, JsonObjectBuilder reply, int userId, int canvasId) {
        //cek apakah user punya akses ke kanvas atau tidak
        try (PreparedStatement open = conn.prepareStatement(DB.Participation.Q.Update.TRACE)) {
            open.setInt(DB.Participation.Q.Update.Trace.USER_ID, userId);
            open.setInt(DB.Participation.Q.Update.Trace.CANVAS_ID, canvasId);
            open.setObject(DB.Participation.Q.Update.Trace.LAST_ACCESS, new Date(System.currentTimeMillis()));
            open.setString(DB.Participation.Q.Update.Trace.ACTION, DB.Participation.Action.OPEN);
            int result = open.executeUpdate();
            if (result > 0) {
                try (PreparedStatement canvasDetail = conn.prepareStatement(DB.Canvas.Q.Select.DETAIL_BY_ID);
                        PreparedStatement actionCount = conn.prepareStatement(DB.Action.Q.Select.COUNT);
                        PreparedStatement getObject = conn.prepareStatement(DB.Objects.Q.Select.BY_CANVAS_ID)) {
                    //mengambil data kanvas
                    canvasDetail.setInt(DB.Canvas.Q.Select.DetailById.CANVAS_ID, canvasId);
                    ResultSet detail = canvasDetail.executeQuery();
                    if (detail.next()) {
                        reply.add(Reply.CANVAS_ID, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.CANVAS_ID));
                        reply.add(Reply.CANVAS_NAME, detail
                                .getString(DB.Canvas.Q.Select.DetailById.Column.CANVAS_NAME));
                        reply.add(Reply.CANVAS_WIDTH, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.WIDTH));
                        reply.add(Reply.CANVAS_HEIGHT, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.HEIGHT));
                        reply.add(Reply.CANVAS_TOP, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.TOP));
                        reply.add(Reply.CANVAS_LEFT, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.LEFT));
                        reply.add(Reply.CREATE_TIME, detail
                                .getString(DB.Canvas.Q.Select.DetailById.Column.CREATE_TIME));
                        reply.add(Reply.OWNER_ID, detail.getInt(DB.Canvas.Q.Select.DetailById.Column.OWNER_ID));
                        reply.add(Reply.OWNER_NAME, detail.getString(DB.Canvas.Q.Select.DetailById.Column.OWNER_NAME));

                        //mengambil jumlah aksi terakhir pada kanvas
                        actionCount.setInt(DB.Action.Q.Select.Count.CANVAS_ID, canvasId);
                        ResultSet count = actionCount.executeQuery();
                        if (count.next())
                            reply.add(Reply.LAST_ACTION_NUM, count.getInt(DB.Action.Q.Select.Count.CANVAS_ID));

                        //mengambil daftar objek pada kanvas
                        getObject.setInt(DB.Objects.Q.Select.ByCanvasId.CANVAS_ID, canvasId);
                        ResultSet objectResult = getObject.executeQuery();
                        JsonArrayBuilder objects = Json.createArrayBuilder();
                        while (objectResult.next()) {
                            JsonObjectBuilder obj = Json.createObjectBuilder();
                            obj.add(Reply.OBJECT_ID, objectResult.getInt(DB.Objects.Q.Select.ByCanvasId.Column.ID));

                            int objCode = objectResult.getInt(DB.Objects.Q.Select.ByCanvasId.Column.CODE);
                            obj.add(Reply.OBJECT_CODE, objCode);

                            String objGeom = objectResult.getString(DB.Objects.Q.Select.ByCanvasId.Column.GEOM);
                            obj.add(Reply.OBJECT_GEOM, objGeom);

                            String objStyle = objectResult.getString(DB.Objects.Q.Select.ByCanvasId.Column.STYLE);
                            obj.add(Reply.OBJECT_STYLE, objStyle);

                            String objTrans = objectResult
                                    .getString(DB.Objects.Q.Select.ByCanvasId.Column.TRANSFORM);
                            obj.add(Reply.OBJECT_TRANSFORM, objTrans);

                            if (objectResult.getBoolean(DB.Objects.Q.Select.ByCanvasId.Column.EXIST))
                                obj.add(Reply.OBJECT_EXIST, true);
                            else
                                obj.add(Reply.OBJECT_EXIST, false);
                            objects.add(obj);
                        }
                        //masukkan daftar objek ke reply
                        reply.add(Reply.OBJECT_LIST, objects);
                    } else
                        //jika detail kanvas tidak ditemukan
                        reply.add(PortalJCode.ERROR, PortalJCode.Error.CANVAS_NOT_FOUND);
                }
            } else
                //beritahu jika user tidak punya akses
                reply.add(PortalJCode.ERROR, PortalJCode.Error.NOT_AUTHORIZED);
        } catch (SQLException ex) {
            reply.add(PortalJCode.ERROR, PortalJCode.Error.SERVER_ERROR);
        }
    }

    /**
     * Menutup sebuah kanvas.
     *
     * @param conn
     * @param reply
     * @param userId id user yang menutup kanvas.
     * @param canvasId id kanvas yang ditutup.
     */
    private void close(Connection conn, JsonObjectBuilder reply, int userId, int canvasId) {
        try (PreparedStatement closing = conn.prepareStatement(DB.Participation.Q.Update.TRACE)) {
            closing.setInt(DB.Participation.Q.Update.Trace.USER_ID, userId);
            closing.setInt(DB.Participation.Q.Update.Trace.CANVAS_ID, canvasId);
            closing.setObject(DB.Participation.Q.Update.Trace.LAST_ACCESS, new Date(System.currentTimeMillis()));
            closing.setString(DB.Participation.Q.Update.Trace.ACTION, DB.Participation.Action.CLOSE);
            if (closing.executeUpdate() <= 0)
                reply.add(PortalJCode.ERROR, PortalJCode.Error.NOT_AUTHORIZED);
        } catch (SQLException ex) {
            reply.add(PortalJCode.ERROR, PortalJCode.Error.SERVER_ERROR);
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
