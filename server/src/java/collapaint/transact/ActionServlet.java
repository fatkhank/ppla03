package collapaint.transact;

import collapaint.code.ActionCode;
import collapaint.code.ActionJCode;
import collapaint.DB;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import javax.json.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 *
 * @author hamba v7
 */
@WebServlet(name = "action", urlPatterns = {"/action"})
public class ActionServlet extends HttpServlet {

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
     * Memproses request dari klien, baik
     *
     * @param is
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(InputStream is,
            HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            JsonObjectBuilder reply = Json.createObjectBuilder();
            try (Connection conn = dataSource.getConnection()) {
                JsonObject request = Json.createReader(is).readObject();

                //*************************** SAVE REQUEST *************************
                ArrayList<CanvasObject> objectPool = new ArrayList<>();
                ArrayList<Action> userActions = new ArrayList<>();
                int canvasId = request.getInt(ActionJCode.CANVAS_ID);

                //------------ proses objek2 ------------
                parseObjects(request, objectPool);
                insertObjects(conn, canvasId, objectPool);

                //------------ proses aksi2 ------------
                parseActions(request, canvasId, userActions, objectPool);
                insertActions(conn, canvasId, userActions);

                //*************************** REPLY *************************
                processReply(conn, request, canvasId, userActions, objectPool, reply);
            } catch (JsonException | NullPointerException | ClassCastException ex) {
                ex.printStackTrace();
                reply.add(ActionJCode.ERROR, ActionJCode.BAD_REQUEST);
            } catch (SQLException ex) {
                ex.printStackTrace();
                reply.add(ActionJCode.ERROR, ActionJCode.SERVER_ERROR);
            }
            out.println(reply.build());
        }
    }

    /**
     * Memproses daftar objek kanvas.
     *
     * @param request
     * @param objectPool penampung daftar objek
     */
    void parseObjects(JsonObject request, ArrayList<CanvasObject> objectPool) throws NullPointerException, ClassCastException {
        //abaikan jika tidak ada daftar objek
        if (!request.containsKey(ActionJCode.OBJECT_LIST))
            return;
        JsonArray objs = request.getJsonArray(ActionJCode.OBJECT_LIST);
        int size = objs.size();
        for (int i = 0; i < size; i++) {
            JsonObject obj = objs.getJsonObject(i);
            int id = obj.getInt(ActionJCode.OBJECT_LOCAL_ID);

            //cek apakah objek memiliki gid atau tidak (objek baru).
            int gid = (obj.containsKey(ActionJCode.OBJECT_GLOBAL_ID)) ? obj.
                    getInt(ActionJCode.OBJECT_GLOBAL_ID) : -1;
            int code = obj.getInt(ActionJCode.OBJECT_CODE);
            String geom = obj.getString(ActionJCode.OBJECT_GEOM);
            String style = obj.getString(ActionJCode.OBJECT_STYLE);
            String transform = obj.getString(ActionJCode.OBJECT_TRANSFORM);
            CanvasObject co = new CanvasObject(id, gid, code, geom, style,
                    transform);
            objectPool.add(co);
        }
    }

    /**
     * Memroses daftar aksi dari request.
     *
     * @param request
     * @param canvasId id kanvas
     * @param userActions
     * @param objectPool
     */
    void parseActions(JsonObject request, int canvasId, ArrayList<Action> userActions,
            ArrayList<CanvasObject> objectPool) {
        //abaikan jika tidak ada daftar aksi
        if (!request.containsKey(ActionJCode.ACTION_LIST))
            return;
        JsonArray acts = request.getJsonArray(ActionJCode.ACTION_LIST);
        int size = acts.size();
        for (int i = 0; i < size; i++) {
            JsonObject obj = acts.getJsonObject(i);

            //cek kode aksi
            int code = obj.getInt(ActionJCode.ACTION_CODE);
            String param = obj.getString(ActionJCode.ACTION_PARAM);
            if (code == ActionCode.RESIZE_ACTION) {
                //aksi berkatian dengan kanvas
                int width = obj.getInt(ActionJCode.CANVAS_WIDTH);
                int height = obj.getInt(ActionJCode.CANVAS_HEIGHT);
                int top = obj.getInt(ActionJCode.CANVAS_TOP, 0);
                int left = obj.getInt(ActionJCode.CANVAS_LEFT, 0);
                userActions.
                        add(new Resize(canvasId, width, height, top, left, param));
            } else {
                //aksi berkaitan dengan objek kanvas.
                //cek apakah objek yang berhubungan adalah objek baru, atau objek lama
                int oid = (obj.containsKey(ActionJCode.ACTION_OBJ_KNOWN))
                        ? obj.getInt(ActionJCode.ACTION_OBJ_KNOWN)
                        : objectPool.
                        get(obj.getInt(ActionJCode.ACTION_OBJ_LISTED)).globalId;
                userActions.add(new ActionObject(canvasId, oid, code, param));
            }
        }
    }

    /**
     * Memroses jawaban untuk klien
     *
     * @param request
     * @param canvasId
     * @param userActions
     * @param objectPool
     * @param reply
     * @throws SQLException
     */
    void processReply(Connection conn, JsonObject request, int canvasId, ArrayList<Action> userActions,
            ArrayList<CanvasObject> objectPool, JsonObjectBuilder reply) throws SQLException {
        try (PreparedStatement lastestAction = conn.prepareStatement(DB.Action.Q.Select.LASTEST)) {

            // ----- ambil aksi terakhir.
            int lastActionNumber = request.getInt(ActionJCode.LAST_ACTION_NUM);
            JsonArrayBuilder actionArray = Json.createArrayBuilder();
            lastestAction.setInt(DB.Action.Q.Select.Lastest.CANVAS_ID, canvasId);
            lastestAction.setInt(DB.Action.Q.Select.Lastest.LIMIT_MIN, lastActionNumber);
            lastestAction.setInt(DB.Action.Q.Select.Lastest.LIMIT_COUNT, 256);
            ResultSet result = lastestAction.executeQuery();
            int pointer = 0;//digunakan untuk mempercepat proses pencarian aksi
            int newObjects = 0;//menghitung jumlah objek yang belum diketahui klien
            while (result.next()) {
                lastActionNumber++;//tambahkan untuk menghitung berapa jumlah aksi terakhir
                JsonObjectBuilder actionObject = Json.createObjectBuilder();

                //cek apakah aksi ini adalah aksi yang disubmit klien
                int actionID = result.
                        getInt(DB.Action.Q.Select.Lastest.Column.ID);
                for (int i = pointer; i < userActions.size(); i++) {
                    if (userActions.get(i).id == actionID) {
                        actionObject.add(ActionJCode.ACTION_SUBMITTED, i);
                        pointer = i + 1; //update pointer untuk mencari pada indeks berikutnya.
                        actionID = -1;//digunakan untuk menandai bahwa aksi ini tercantum di request
                    }
                }

                //Jika aksi ini merupakan aksi dari user lain (tidak tercantum di request), masukkan datanya ke reply
                int actionCode = result.
                        getInt(DB.Action.Q.Select.Lastest.Column.CODE);
                if (actionID != -1) {
                    String param = result.
                            getString(DB.Action.Q.Select.Lastest.Column.PARAMETER);
                    actionObject.add(ActionJCode.ACTION_PARAM, param);
                    actionObject.add(ActionJCode.ACTION_CODE, actionCode);
                }

                if (actionCode != ActionCode.RESIZE_ACTION) {
                    //Jika aksi berkatian dengan suatu objek, ambil id objek
                    int objectID = result.
                            getInt(DB.Action.Q.Select.Lastest.Column.OBJECT_ID);
                    //var ini tidak digunakan lagi, dioverwrite untuk mencari indeks objek yang berkaitan dengan aksi di atas.
                    actionID = OBJECT_INCOMPLETE;
                    //Cari apakah objek ada di object pool.
                    for (int i = 0; i < objectPool.size(); i++) {
                        if (objectPool.get(i).globalId == objectID) {
                            actionID = i; //catat posisi objek di object pool
                            break;
                        }
                    }
                    if (actionID != OBJECT_INCOMPLETE) {
                        //Jika objek ada di object pool, masukkan posisinya
                        actionObject.add(ActionJCode.ACTION_OBJ_LISTED, actionID);
                    } else if (actionCode == ActionCode.DRAW_ACTION) {
                        /*
                         Jika objek belum ada di object pool, dan merupakan aksi 
                         draw dari user lain tambahkan ke objek pool dan masukkan urutannya
                         */
                        newObjects++;
                        actionObject.add(ActionJCode.ACTION_OBJ_LISTED, objectPool.
                                size());
                        objectPool.add(new CanvasObject(objectID, OBJECT_INCOMPLETE));
                    } else {
                        //objek sudah tersimpan di klien
                        actionObject.add(ActionJCode.ACTION_OBJ_KNOWN, objectID);
                    }
                }
                actionArray.add(actionObject); //masukkan aksi
            }
            //masukkan daftar aksi
            reply.add(ActionJCode.ACTION_LIST, actionArray);
            reply.add(ActionJCode.LAST_ACTION_NUM, lastActionNumber);//masukkan indeks aksi terakhir

            addObjectsToReply(conn, objectPool, newObjects, reply);
        }
    }

    private static final int OBJECT_INCOMPLETE = -1, OBJECT_MISSING = -2;

    /**
     * Mncantumkan daftar objek di reply
     *
     * @param objectPool
     * @param reply
     * @throws SQLException
     */
    void addObjectsToReply(Connection conn, ArrayList<CanvasObject> objectPool, int incompleteObjectsCount,
            JsonObjectBuilder reply) throws SQLException {
        /*
         Ambil data untuk objek di object pool yang belum ada sebelumnya.
         Object yang ada di object pool, pasti object yang ada di request, 
         atau objek dari aksi user lain.
         */
        JsonArrayBuilder objectArray = Json.createArrayBuilder();

        //ambil data objek yang belum lengkap
        if (incompleteObjectsCount > 0) {
            //menyimpan posisi objek yang belum lengkap
            int counter = 0;
            int size = objectPool.size();
            try (PreparedStatement getDetail = conn.prepareStatement(DB.Objects.Q.Select.BY_ID)) {
                //catat objek yang datanya belum lengkap
                for (int i = 0; i < size; i++) {
                    CanvasObject canvasObject = objectPool.get(i);
                    if (canvasObject.id == OBJECT_INCOMPLETE) {
                        getDetail.
                                setInt(DB.Objects.Q.Select.ById.ID, canvasObject.globalId);
                        //ambil datanya
                        ResultSet result = getDetail.executeQuery();
                        //masukkan datanya ke objek
                        counter = 0;
                        if (result.next()) {
                            canvasObject.globalId = result.
                                    getInt(DB.Objects.Q.Select.ById.Column.ID);
                            canvasObject.code = result.
                                    getInt(DB.Objects.Q.Select.ById.Column.CODE);
                            canvasObject.geom = result.
                                    getString(DB.Objects.Q.Select.ById.Column.GEOM);
                            canvasObject.style = result.
                                    getString(DB.Objects.Q.Select.ById.Column.STYLE);
                            canvasObject.transform = result.
                                    getString(DB.Objects.Q.Select.ById.Column.TRANSFORM);
                        } else
                            canvasObject.id = OBJECT_MISSING;
                    }
                }
            }
        }
        int size = objectPool.size();
        for (int i = 0; i < size; i++) {
            CanvasObject canvasObject = objectPool.get(i);
            if (canvasObject.id < 0) {
                //jika object merupakan objek yang baru dan datanya terdapat di
                if (canvasObject.id == OBJECT_MISSING) {
                    objectArray.add(Json.createObjectBuilder().add(ActionJCode.OBJECT_GLOBAL_ID, canvasObject.globalId)
                            .add(ActionJCode.OBJECT_MISSING, 1));
                } else
                    objectArray.add(Json.createObjectBuilder().
                            add(ActionJCode.OBJECT_GLOBAL_ID, canvasObject.globalId).
                            add(ActionJCode.OBJECT_CODE, canvasObject.code).
                            add(ActionJCode.OBJECT_GEOM, canvasObject.geom).
                            add(ActionJCode.OBJECT_STYLE, canvasObject.style).
                            add(ActionJCode.OBJECT_TRANSFORM, canvasObject.transform));
            } else {
                //jika objek berasal dari aksi di request, cantumkan id globalnya, supaya klien tahu.
                objectArray.add(Json.createObjectBuilder().
                        add(ActionJCode.OBJECT_LOCAL_ID, canvasObject.id).
                        add(ActionJCode.OBJECT_GLOBAL_ID, canvasObject.globalId));
            }
        }
        reply.add(ActionJCode.OBJECT_LIST, objectArray);//masukkan daftar objek
    }

    /**
     * Memasukkan daftar objek kanvas ke database.
     *
     * @param objects
     * @throws SQLException
     */
    void insertObjects(Connection conn, int canvasId, ArrayList<CanvasObject> objects) throws SQLException {
        int size = objects.size();
        if (size == 0)
            return;
        try (PreparedStatement insert = conn
                .prepareStatement(DB.Objects.Q.Insert.ALL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < size; i++) {
                CanvasObject obj = objects.get(0);
                insert.setInt(DB.Objects.Q.Insert.All.CANVAS_ID, canvasId);
                insert.setInt(DB.Objects.Q.Insert.All.CODE, obj.code);
                insert.setString(DB.Objects.Q.Insert.All.TRANSFORM, obj.transform);
                insert.setString(DB.Objects.Q.Insert.All.STYLE, obj.style);
                insert.setString(DB.Objects.Q.Insert.All.GEOM, obj.geom);
                insert.setBoolean(DB.Objects.Q.Insert.All.EXIST, true);
                insert.addBatch();
            }
            insert.executeBatch();
            ResultSet keys = insert.getGeneratedKeys();
            int i = 0;
            while (keys.next()) {
                objects.get(i++).globalId = keys.getInt(1);
            }
        }
    }

    /**
     * Memasukkan aksi-aksi ke database.
     *
     * @param canvasID
     * @param actions daftar aksi yang akan dimasukkan
     * @throws SQLException
     */
    void insertActions(Connection conn, int canvasID, ArrayList<Action> actions) throws SQLException {
        int size = actions.size();
        //abaikan jika kosong
        if (size == 0)
            return;

        try (PreparedStatement updateTrans = conn.prepareStatement(DB.Objects.Q.Update.TRANSFORM);
                PreparedStatement updateStyle = conn.prepareStatement(DB.Objects.Q.Update.STYLE);
                PreparedStatement updateGeom = conn.prepareStatement(DB.Objects.Q.Update.GEOM);
                PreparedStatement deleteObject = conn.prepareStatement(DB.Objects.Q.Update.STATUS);
                PreparedStatement insertAction = conn
                .prepareStatement(DB.Action.Q.Insert.ALL, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement updateCanvas = conn.prepareStatement(DB.Canvas.Q.Update.SIZE)) {
            for (int i = 0; i < size; i++) {
                Action act = actions.get(i);
                insertAction.setInt(DB.Action.Q.Insert.All.CANVAS_ID, canvasID);
                insertAction.setInt(DB.Action.Q.Insert.All.CODE, act.code);
                insertAction.setString(DB.Action.Q.Insert.All.PARAMETER, act.param);
                if (act instanceof Resize) {
                    insertAction.setInt(DB.Action.Q.Insert.All.OBJECT_ID, 0);
                    //Mengubah ukuran kanvas
                    Resize rs = (Resize) act;
                    updateCanvas.setInt(DB.Canvas.Q.Update.Size.WIDTH, rs.width);
                    updateCanvas.setInt(DB.Canvas.Q.Update.Size.HEIGHT, rs.height);
                    updateCanvas.setInt(DB.Canvas.Q.Update.Size.TOP, rs.top);
                    updateCanvas.setInt(DB.Canvas.Q.Update.Size.LEFT, rs.left);
                    updateCanvas.setInt(DB.Canvas.Q.Update.Size.ID, canvasID);
                    updateCanvas.addBatch();
                } else {
                    ActionObject actionObject = (ActionObject) act;
                    insertAction.
                            setInt(DB.Action.Q.Insert.All.OBJECT_ID, actionObject.objectID);
                    if (act.code == ActionCode.GEOM_ACTION) {
                        //ubah geometri objek
                        updateGeom.setString(DB.Objects.Q.Update.Geom.GEOM_PARAM, act.param);
                        updateGeom.setInt(DB.Objects.Q.Update.Geom.OBJECT_ID, actionObject.objectID);
                        updateGeom.addBatch();
                    } else if (act.code == ActionCode.TRANSFORM_ACTION) {
                        //ubah transformasi objek
                        updateTrans.setString(DB.Objects.Q.Update.Transform.TRANS_PARAM, act.param);
                        updateTrans.setInt(DB.Objects.Q.Update.Transform.OBJECT_ID, actionObject.objectID);
                        updateTrans.addBatch();
                    } else if (act.code == ActionCode.STYLE_ACTION) {
                        //ubah style objek
                        updateStyle.setString(DB.Objects.Q.Update.Style.STYLE_PARAM, act.param);
                        updateStyle.setInt(DB.Objects.Q.Update.Style.OBJECT_ID, actionObject.objectID);
                        updateStyle.addBatch();
                    } else if (act.code == ActionCode.DELETE_ACTION) {
                        //ubah status objek menjadi sudah terhapus
                        deleteObject.setBoolean(DB.Objects.Q.Update.Status.STATUS_PARAM, false);
                        deleteObject.setInt(DB.Objects.Q.Update.Status.OBJECT_ID, actionObject.objectID);
                        deleteObject.addBatch();
                        System.out.println("hore");
                    }
                }
                insertAction.addBatch();
            }
            updateGeom.executeBatch();
            updateTrans.executeBatch();
            updateStyle.executeBatch();
            updateCanvas.executeBatch();
            deleteObject.executeBatch();
            insertAction.executeBatch();
            ResultSet keys = insertAction.getGeneratedKeys();
            int i = 0;
            while (keys.next()) {
                actions.get(i++).id = keys.getInt(1);
            }
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
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
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
