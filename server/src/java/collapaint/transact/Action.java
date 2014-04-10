/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint.transact;

import collapaint.DB;
import com.sun.xml.bind.StringInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
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
@WebServlet(name = "action", urlPatterns = {"/action"})
public class Action extends HttpServlet {

    public static final class JCode {

        static final String LAST_ACTION_NUM = "lan";
        static final String CANVAS_ID = "cid";
        static final String ACTION_LIST = "act";
        static final String ACTION_OBJ_LISTED = "ol";
        static final String ACTION_OBJ_KNOWN = "ok";
        static final String ACTION_SUBMITTED = "as";
        static final String ACTION_CODE = "cd";
        static final String ACTION_PARAM = "par";
        static final String OBJECT_LIST = "obj";
        static final String OBJECT_ID = "id";
        static final String OBJECT_GLOBAL_ID = "gid";
        static final String OBJECT_CODE = "cd";
        static final String OBJECT_SHAPE = "sh";
        static final String OBJECT_STYLE = "st";
        static final String OBJECT_TRANSFORM = "tx";
    }

    public static final class ActionCode {

        public static final int DRAW_ACTION = 1;
        public static final int DELETE_ACTION = 2;
        public static final int RESHAPE_ACTION = 3;
        public static final int STYLE_ACTION = 4;
        public static final int TRANSFORM_ACTION = 5;
    }

    static final class ActionCol {

        static final int ID = 1;
        static final int CANVAS_ID = 2;
        static final int CODE = 3;
        static final int OBJECT_ID = 4;
        static final int PARAMETER = 5;
    }

    static final class ObjectCol {

        static final int ID = 1;
        static final int CODE = 2;
        static final int TRANSFORM = 3;
        static final int STYLE = 4;
        static final int SHAPE = 5;
    }

    private final class ActionObject {

        int id;
        int objectID;
        int code;
        String param;

        public ActionObject(int objectID, int code, String param) {
            this.objectID = objectID;
            this.code = code;
            this.param = param;
        }
    }

    private final class CanvasObject {

        int id;
        int globalId;
        int code;
        String shape;
        String style;
        String transform;

        public CanvasObject(int id, int globalId, int code, String shape,
                String style, String transform) {
            this.id = id;
            this.globalId = globalId;
            this.code = code;
            this.shape = shape;
            this.style = style;
            this.transform = transform;
        }

        public CanvasObject(int gid) {
            this.globalId = gid;
            this.id = -1;
        }
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
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            //*************************** SAVE REQUEST *************************
            ArrayList<CanvasObject> objectPool = new ArrayList<>();
            ArrayList<ActionObject> userActions = new ArrayList<>();

            JsonObject json = Json.createReader(is).readObject();
            System.out.println("recieve:" + json);

            //------------ process objects ------------
            JsonArray objs = json.getJsonArray(JCode.OBJECT_LIST);
            int size = objs.size();
            for (int i = 0; i < size; i++) {
                JsonObject obj = objs.getJsonObject(i);
                int id = obj.getInt(JCode.OBJECT_ID);
                int gid = (obj.containsKey(JCode.OBJECT_GLOBAL_ID)) ? obj.
                        getInt(JCode.OBJECT_GLOBAL_ID) : -1;
                int code = obj.getInt(JCode.OBJECT_CODE);
                String shape = obj.getString(JCode.OBJECT_SHAPE);
                String style = obj.getString(JCode.OBJECT_STYLE);
                String transform = obj.getString(JCode.OBJECT_TRANSFORM);
                CanvasObject co = new CanvasObject(id, gid, code, shape, style,
                        transform);
                objectPool.add(co);
            }

            insertObjects(objectPool, out);

            //------------ process actions ------------
            JsonArray acts = json.getJsonArray(JCode.ACTION_LIST);
            size = acts.size();
            for (int i = 0; i < size; i++) {
                JsonObject obj = acts.getJsonObject(i);
                int oid = (obj.containsKey(JCode.ACTION_OBJ_KNOWN))
                        ? obj.getInt(JCode.ACTION_OBJ_KNOWN)
                        : objectPool.get(obj.getInt(JCode.ACTION_OBJ_LISTED)).globalId;

                int code = obj.getInt(JCode.ACTION_CODE);
                String param = obj.getString(JCode.ACTION_PARAM, "");
                ActionObject ao = new ActionObject(oid, code, param);
                userActions.add(ao);
            }
            int canvasId = json.getInt(JCode.CANVAS_ID);
            insertActions(canvasId, userActions);

            //*************************** REPLY *************************
            try (Statement statement = connection.createStatement()) {
                JsonObjectBuilder reply = Json.createObjectBuilder();

                // ----- get latest action list
                int lan = json.getInt(JCode.LAST_ACTION_NUM);
                JsonArrayBuilder ajab = Json.createArrayBuilder();
                StringBuilder sb = new StringBuilder();
                sb.append("select * from action where canvas_id='").
                        append(canvasId).
                        append("' limit ").append(lan).append(",256");

                ResultSet result = statement.executeQuery(sb.toString());
                int pointer = 0;
                while (result.next()) {
                    lan++;
                    JsonObjectBuilder ob = Json.createObjectBuilder();
                    //cek if it is submitted action
                    int id = result.getInt(ActionCol.ID);
                    for (int i = pointer; i < userActions.size(); i++) {
                        if (userActions.get(i).id == id) {
                            ob.add(JCode.ACTION_SUBMITTED, i);
                            pointer = i + 1;
                            id = -1;
                        }
                    }
                    //if not a submitted action
                    int code = result.getInt(ActionCol.CODE);
                    if (id != -1) {
                        String param = result.getString(ActionCol.PARAMETER);
                        ob.add(JCode.ACTION_PARAM, param);
                        ob.add(JCode.ACTION_CODE, code);
                        id = -1;
                    }
                    int objectID = result.getInt(ActionCol.OBJECT_ID);

                    //search object in object pool
                    for (int i = 0; i < objectPool.size(); i++) {
                        if (objectPool.get(i).globalId == objectID) {
                            id = i;
                            break;
                        }
                    }
                    if (id != -1) {//object is found in list
                        ob.add(JCode.ACTION_OBJ_LISTED, id);
                    } else if (code == ActionCode.DRAW_ACTION) {//object is not found but new
                        ob.add(JCode.ACTION_OBJ_LISTED, objectPool.size());
                        objectPool.add(new CanvasObject(objectID));
                    } else//object user has known the object
                        ob.add(JCode.ACTION_OBJ_KNOWN, objectID);
                    ajab.add(ob);
                }
                reply.add(JCode.ACTION_LIST, ajab);

                // ----- get object data
                JsonArrayBuilder ojab = Json.createArrayBuilder();
                size = objectPool.size();
                for (int i = 0; i < size; i++) {
                    CanvasObject co = objectPool.get(i);
                    if (co.id == -1) {
                        fetchDetail(co);
                        ojab.add(Json.createObjectBuilder().
                                add(JCode.OBJECT_GLOBAL_ID, co.globalId).
                                add(JCode.OBJECT_CODE, co.code).
                                add(JCode.OBJECT_SHAPE, co.shape).
                                add(JCode.OBJECT_STYLE, co.style).
                                add(JCode.OBJECT_TRANSFORM, co.transform));
                    } else {
                        ojab.add(Json.createObjectBuilder().
                                add(JCode.OBJECT_ID, co.id).
                                add(JCode.OBJECT_GLOBAL_ID, co.globalId));
                    }
                }
                reply.add(JCode.OBJECT_LIST, ojab);

                reply.add(JCode.LAST_ACTION_NUM, lan);
                out.println(reply.build());

                System.out.println("reply:" + reply.build());
            }
        } catch (IOException | SQLException ex) {
            StackTraceElement[] ste = ex.getStackTrace();
            PrintWriter out = response.getWriter();
            out.println("error:" + ex.toString() + ":" + ex.getCause());
            out.print("<pre>");
            for (StackTraceElement ste1 : ste) {
                out.println(ste1.toString());
            }
            out.print("</pre>");
            Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void insertObjects(ArrayList<CanvasObject> objects, PrintWriter out) throws SQLException {
        int size = objects.size();
        if (size == 0)
            return;
        StringBuilder sb = new StringBuilder();
        sb.append("insert into object(code,transform,style,shape) values ");
        CanvasObject obj = objects.get(0);
        sb.append("('").append(obj.code).append("','").append(obj.transform).
                append("','").append(obj.style).append("','").append(obj.shape).
                append("')");
        for (int i = 1; i < size; i++) {
            obj = objects.get(i);
            sb.append(",('").append(obj.code).append("','").
                    append(obj.transform).
                    append("','").append(obj.style).append("','").
                    append(obj.shape).append("')");
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sb.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = statement.getGeneratedKeys();
            int i = 0;
            while (keys.next()) {
                objects.get(i++).globalId = keys.getInt(1);
            }
        }
    }

    void insertActions(int canvasID, ArrayList<ActionObject> actions) throws SQLException {
        int size = actions.size();
        if (size == 0)
            return;
        StringBuilder sb = new StringBuilder();
        sb.append(
                "insert into action(canvas_id,code,object_id,parameter) values ");
        ActionObject act = actions.get(0);
        sb.append("('").append(canvasID).append("','").append(act.code).
                append("','").append(act.objectID).append("','").
                append(act.param).append("')");
        for (int i = 1; i < size; i++) {
            act = actions.get(i);
            sb.append(",('").append(canvasID).append("','").append(act.code).
                    append("','").append(act.objectID).append("','").
                    append(act.param).append("')");
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sb.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = statement.getGeneratedKeys();
            int i = 0;
            while (keys.next()) {
                actions.get(i++).id = keys.getInt(1);
            }
        }
    }

    void fetchDetail(CanvasObject obj) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            StringBuilder sb = new StringBuilder();
            sb.append("select * from object where id='").append(obj.globalId).
                    append("'");
            ResultSet result = statement.executeQuery(sb.toString());
            result.next();
            obj.code = result.getInt(ObjectCol.CODE);
            obj.transform = result.getString(ObjectCol.TRANSFORM);
            obj.style = result.getString(ObjectCol.STYLE);
            obj.shape = result.getString(ObjectCol.SHAPE);
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
