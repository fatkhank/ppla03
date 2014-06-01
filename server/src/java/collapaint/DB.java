package collapaint;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.servlet.ServletContext;

/**
 *
 * @author hamba v7
 */
public class DB {

    public static final void init(MysqlDataSource dataSource, ServletContext context) {
        dataSource.setURL(context.getInitParameter("DB_URL"));
        dataSource.setUser(context.getInitParameter("DB_USERNAME"));
        dataSource.setPassword(context.getInitParameter("DB_PASSWORD"));
    }

    public static final String TABLE_ACTION = "action";
    public static final String TABLE_CANVAS = "canvas";
    public static final String TABLE_OBJECT = "object";
    public static final String TABLE_PARTICIPATION = "participation";
    public static final String TABLE_USER = "user";

    public static final class Action {

        public static final String ID = "id";
        public static final String CANVAS_ID = "canvas_id";
        public static final String CODE = "code";
        public static final String OBJECT_ID = "object_id";
        public static final String PARAMETER = "parameter";

        public static final class Q {

            public static final String INSERT_ACTION = "INSERT INTO " + TABLE_ACTION + "(" + Action.CANVAS_ID + ","
                    + Action.CODE + "," + Action.OBJECT_ID + "," + Action.PARAMETER + ") VALUES (?,?,?,?)";
            public static final int INSERT_ACTION_PARAMETER = 4;
            public static final int INSERT_ACTION_OBJECTID = 3;
            public static final int INSERT_ACTION_CANVASID = 1;
            public static final int INSERT_ACTION_CODE = 2;

            /**
             * Mengambil aksi terakhir dari {@link #SELECT_LAST_LIMITMIN} sejumlah {@link #SELECT_LAST_LIMITCOUNT}
             */
            public static final String SELECT_LAST = "SELECT " + Action.ID + "," + Action.CANVAS_ID + "," + Action.CODE
                    + "," + OBJECT_ID + "," + Action.PARAMETER + " FROM " + TABLE_ACTION + " WHERE " + Action.CANVAS_ID
                    + " = ? ORDER BY " + Action.ID + " LIMIT ?, ?";
            public static final int SELECT_LAST_LIMITMIN = 2;
            public static final int SELECT_LAST_LIMITCOUNT = 3;
            public static final int SELECT_LAST_CANVASID = 1;
            public static final int SELECT_LAST_RESULT_ID = 1;
            public static final int SELECT_LAST_RESULT_PARAMETER = 5;
            public static final int SELECT_LAST_RESULT_CANVASID = 2;
            public static final int SELECT_LAST_RESULT_CODE = 3;
            public static final int SELECT_LAST_RESULT_OBJECTID = 4;
            /**
             * Mengambil jumlah aksi pada suatu kanvas.
             */
            public static final String SELECT_COUNT = "SELECT COUNT(*) FROM " + TABLE_ACTION + " WHERE " + CANVAS_ID
                    + "=?";
            public static final int SELECT_COUNT_CANVASID = 1;
            public static final int SELECT_COUNT_RESULT_COUNT = 1;
            /**
             * Menghapus semua aksi pada kanvas tertentu
             */
            public static final String DELETE_BYCANVAS = "DELETE FROM " + TABLE_ACTION + " WHERE " + CANVAS_ID + "=?";
            public static final int DELETE_BYCANVAS_CANVASID = 1;

        }
    }

    public static final class Canvas {

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String TOP = "top";
        public static final String LEFT = "canvas_left";
        public static final String OWNER_ID = "owner";
        public static final String CREATE_TIME = "create_time";

        public static final class Q {

            /**
             * Menghapus suatu kanvas berdasarkan parameter id kanvas sebagai {@link #DELETE_BYID_CANVASID}.
             */
            public static final String DELETE_BYID = "DELETE FROM " + TABLE_CANVAS + " WHERE " + ID + "=?";
            public static final int DELETE_BYID_CANVASID = 1;

            /**
             * Membuat kanvas baru, dengan data
             */
            public static final String INSERT_CANVAS = "INSERT INTO " + TABLE_CANVAS + "(" + Canvas.NAME + ", "
                    + Canvas.WIDTH + ", " + Canvas.HEIGHT + ", " + Canvas.TOP + ", " + Canvas.LEFT + ", "
                    + Canvas.OWNER_ID + ", " + Canvas.CREATE_TIME + ") VALUES (?,?,?,?,?,?,?)";
            public static final int INSERT_CANVAS_NAME = 1;
            public static final int INSERT_CANVAS_WIDTH = 2;
            public static final int INSERT_CANVAS_LEFT = 5;
            public static final int INSERT_CANVAS_CREATE_TIME = 7;
            public static final int INSERT_CANVAS_TOP = 4;
            public static final int INSERT_CANVAS_OWNER_ID = 6;
            public static final int INSERT_CANVAS_HEIGHT = 3;

            /**
             * Mengambil daftar kanvas yang dimiliki oleh seorang user. Data kanvas berisi
             * {@link Canvas#ID}, {@link Canvas#NAME}, {@link Canvas#WIDTH}, {@link Canvas#HEIGHT}, dan
             * {@link Canvas#CREATE_TIME}; terurut berdasarkan {@link Canvas#CREATE_TIME} menurun.
             */
            public static final String SELECT_BYOWNER = "SELECT " + ID + "," + NAME + "," + WIDTH + "," + HEIGHT + ","
                    + CREATE_TIME + " FROM " + TABLE_CANVAS + " WHERE " + OWNER_ID + " = ? ORDER BY " + CREATE_TIME
                    + " DESC";
            public static final int SELECT_BYOWNER_OWNERID = 1;
            public static final int SELECT_BYOWNER_RESULT_ID = 1;
            public static final int SELECT_BYOWNER_RESULT_NAME = 2;
            public static final int SELECT_BYOWNER_RESULT_WIDTH = 3;
            public static final int SELECT_BYOWNER_RESULT_CREATETIME = 5;
            public static final int SELECT_BYOWNER_RESULT_HEIGHT = 4;

            /**
             * Mengambil owner dari suatu kanvas berdasarkan id kanvas ({@link #SELECT_OWNEROF_CANVASID}). Mengembalikan
             * id dan nama owner.
             */
            public static final String SELECT_OWNEROF = "SELECT c." + OWNER_ID + ", u." + DB.User.NAME
                    + " FROM " + TABLE_CANVAS + " c, " + TABLE_USER + " u WHERE c." + ID + "=? AND u."
                    + DB.User.ID + "=c." + OWNER_ID;
            public static final int SELECT_OWNEROF_CANVASID = 1;
            public static final int SELECT_OWNEROF_RESULT_OWNERNAME = 2;
            public static final int SELECT_OWNEROF_RESULT_OWNERID = 1;

            /**
             * Mengambil detail kanvas berdasarkan idnya, berikut id owner dan nama owner.
             */
            public static final String SELECT_DETAILBYID = "SELECT c." + ID + ", c." + NAME + ", c." + WIDTH + ", c."
                    + HEIGHT + ", c." + TOP + ", c." + LEFT + ", c." + CREATE_TIME + ", c." + OWNER_ID + ", u."
                    + collapaint.DB.User.NAME + " FROM " + TABLE_CANVAS + " c, " + TABLE_USER + " u WHERE c." + ID
                    + "=? AND u." + collapaint.DB.User.ID + "=c." + OWNER_ID;
            public static final int SELECT_DETAILBYID_CANVASID = 1;
            public static final int SELECT_DETAILBYID_RESULT_WIDTH = 3;
            public static final int SELECT_DETAILBYID_RESULT_OWNERNAME = 9;
            public static final int SELECT_DETAILBYID_RESULT_CANVASNAME = 2;
            public static final int SELECT_DETAILBYID_RESULT_CANVASID = 1;
            public static final int SELECT_DETAILBYID_RESULT_OWNERID = 8;
            public static final int SELECT_DETAILBYID_RESULT_TOP = 5;
            public static final int SELECT_DETAILBYID_RESULT_LEFT = 6;
            public static final int SELECT_DETAILBYID_RESULT_HEIGHT = 4;
            public static final int SELECT_DETAILBYID_RESULT_CREATETIME = 7;

            /**
             * Mengubah ukuran kanvas
             */
            public static final String UDATE_SIZE = "UPDATE " + TABLE_CANVAS + " SET " + WIDTH + "=?, " + HEIGHT
                    + "=?, " + TOP + "=?, " + LEFT + "=? WHERE " + ID + " = ?";
            public static final int UDATE_SIZE_LEFT = 4;
            public static final int UDATE_SIZE_WIDTH = 1;
            public static final int UDATE_SIZE_CANVASID = 5;
            public static final int UDATE_SIZE_HEIGHT = 2;
            public static final int UDATE_SIZE_TOP = 3;

        }
    }

    public static final class Objects {

        public static final String ID = "id";
        public static final String CANVAS_ID = "canvas_id";
        public static final String CODE = "code";
        public static final String TRANSFORM = "transform";
        public static final String GEOM = "geom";
        public static final String STYLE = "style";
        public static final String EXIST = "`exist`";

        public static final class Q {

            /**
             * Memasukkan sebuah objek ke database.
             */
            public static final String INSERT_OBJECT = "INSERT INTO " + TABLE_OBJECT + "(" + CANVAS_ID + "," + CODE
                    + "," + TRANSFORM + "," + STYLE + "," + GEOM + "," + EXIST + ") VALUES (?,?,?,?,?,?)";
            public static final int INSERT_OBJECT_CODE = 2;
            public static final int INSERT_OBJECT_TRANSFORM = 3;
            public static final int INSERT_OBJECT_CANVASID = 1;
            public static final int INSERT_OBJECT_GEOM = 5;
            public static final int INSERT_OBJECT_EXIST = 6;
            public static final int INSERT_OBJECT_STYLE = 4;
            /**
             * Mengambil daftar objek pada suatu kanvas.
             */
            public static final String SELECT_BYCANVASID = "SELECT " + ID + ", " + CODE + ", " + GEOM + ", " + STYLE
                    + ", " + TRANSFORM + ", " + EXIST + " FROM " + TABLE_OBJECT + " WHERE " + CANVAS_ID + "=?";
            public static final int SELECT_BYCANVASID_CANVASID = 1;
            public static final int SELECT_BYCANVASID_RESULT_EXIST = 6;
            public static final int SELECT_BYCANVASID_RESULT_ID = 1;
            public static final int SELECT_BYCANVASID_RESULT_TRANSFORM = 5;
            public static final int SELECT_BYCANVASID_RESULT_GEOM = 3;
            public static final int SELECT_BYCANVASID_RESULT_STYLE = 4;
            public static final int SELECT_BYCANVASID_RESULT_CODE = 2;
            /**
             * Mengambil data objek berdasarkan id objek.
             */
            public static final String SELECT_BYID = "SELECT " + ID + ", " + CODE + ", " + GEOM + ", " + STYLE + ", "
                    + TRANSFORM + ", " + EXIST + " FROM " + TABLE_OBJECT + " WHERE " + ID + "=?";
            public static final int SELECT_BYID_ID = 1;
            public static final int SELECT_BYID_RESULT_EXIST = 6;
            public static final int SELECT_BYID_RESULT_STYLE = 4;
            public static final int SELECT_BYID_RESULT_CODE = 2;
            public static final int SELECT_BYID_RESULT_ID = 1;
            public static final int SELECT_BYID_RESULT_TRANSFORM = 5;
            public static final int SELECT_BYID_RESULT_GEOM = 3;
            /**
             * Mengganti data geom, transform, style, dan status
             */
            public static final String UPDATE_DATA = "UPDATE " + TABLE_OBJECT + " SET " + GEOM + "=?, " + STYLE + "=?, "
                    + TRANSFORM + "=?, " + EXIST + "=? WHERE " + ID + "=?";
            public static final int UPDATE_DATA_EXIST = 4;
            public static final int UPDATE_DATA_OBJECTID = 5;
            public static final int UPDATE_DATA_TRANS = 3;
            public static final int UPDATE_DATA_GEOM = 1;
            public static final int UPDATE_DATA_STYLE = 2;
            public static final String UPDATE_TRANSFORM = "UPDATE " + TABLE_OBJECT + " SET " + TRANSFORM + "=? WHERE "
                    + ID + "=?";
            public static final int UPDATE_TRANSFORM_OBJECTID = 2;
            public static final int UPDATE_TRANSFORM_TRANSPARAM = 1;
            public static final String UDATE_GEOM = "UPDATE " + TABLE_OBJECT + " SET " + GEOM + "=? WHERE " + ID + "=?";
            public static final int UDATE_GEOM_OBJECTID = 2;
            public static final int UDATE_GEOM_GEOMPARAM = 1;
            public static final String UPDATE_STYLE = "UPDATE " + TABLE_OBJECT + " SET " + STYLE + "=? WHERE " + ID
                    + "=?";
            public static final int UPDATE_STYLE_STYLEPARAM = 1;
            public static final int UPDATE_STYLE_OBJECTID = 2;
            public static final String UPDATE_STATUS = "UPDATE " + TABLE_OBJECT + " SET " + EXIST + "=? WHERE " + ID
                    + "=?";
            public static final int UPDATE_STATUS_STATUSPARAM = 1;
            public static final int UPDATE_STATUS_OBJECTID = 2;
        }
    }

    public static final class Participation {

        public static final String USER_ID = "user_id";
        public static final String CANVAS_ID = "canvas_id";
        public static final String LAST_ACCESS = "last_access";
        public static final String STATUS = "status";
        public static final String ACTION = "action";

        public static final class Status {

            public static final String INVITATION = "INVITE";
            public static final String MEMBER = "MEMBER";
            public static final String OWNER = "OWNER";
        }

        public static final class Action {

            public static final String OPEN = "OPEN";
            public static final String CLOSE = "CLOSE";
        }

        public static final class Q {

            public static final String DELETE_RECORD = "DELETE FROM " + TABLE_PARTICIPATION + " WHERE " + CANVAS_ID
                    + "=? AND " + USER_ID + "=?";
            public static final int DELETE_RECORD_CANVASID = 1;
            public static final int DELETE_RECORD_USERID = 2;
            /**
             * Memasukkan data partisipasi baru.
             */
            public static final String INSERT_ALL = "INSERT INTO " + TABLE_PARTICIPATION + "(" + USER_ID + ","
                    + CANVAS_ID + "," + LAST_ACCESS + "," + STATUS + "," + ACTION + ") VALUES (?,?,?,?,?)";
            public static final int INSERT_ALL_ACTION = 5;
            public static final int INSERT_ALL_CANVASID = 2;
            public static final int INSERT_ALL_LASTACCESS = 3;
            public static final int INSERT_ALL_USERID = 1;
            public static final int INSERT_ALL_STATUS = 4;
            /**
             * Mengecek ada tidaknya partisipasi seorang user pada sebuah kanvas.
             */
            public static final String CHECK_PARTICIPATION = "SELECT " + STATUS + ", " + ACTION + " FROM "
                    + TABLE_PARTICIPATION + " WHERE " + USER_ID + "=? AND " + CANVAS_ID + "=?";
            public static final int CHECK_PARTICIPATION_CANVASID = 2;
            public static final int CHECK_PARTICIPATION_USERID = 1;
            public static final int CHECK_PARTICIPATION_RESULT_STATUS = 1;
            public static final int CHECK_PARTICIPATION_RESULT_ACTION = 2;
            public static final String SELECT_BYCANVAS = "SELECT u." + User.ID + ", u." + User.NAME + ", p." + STATUS
                    + ", p." + ACTION + ", p." + LAST_ACCESS + " FROM " + TABLE_PARTICIPATION + " p, " + TABLE_USER
                    + " u WHERE " + CANVAS_ID + "=? AND u." + User.ID + "=p." + USER_ID;
            public static final int SELECT_BYCANVAS_CANVASID = 1;
            public static final int SELECT_BYCANVAS_RESULT_STATUS = 3;
            public static final int SELECT_BYCANVAS_RESULT_ACTION = 4;
            public static final int SELECT_BYCANVAS_RESULT_USERNAME = 2;
            public static final int SELECT_BYCANVAS_RESULT_USERID = 1;
            public static final int SELECT_BYCANVAS_RESULT_LASTACCESS = 5;
            /**
             * Menghitung jumlah user yang aktif pada suatu kanvas.
             */
            public static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM " + TABLE_PARTICIPATION + " WHERE "
                    + CANVAS_ID + "=? AND " + Participation.ACTION + "='" + Action.OPEN + "'";
            public static final int COUNT_ACTIVE_CANVASID = 1;
            public static final int COUNT_ACTIVE_RESULT_COUNT = 1;
            /**
             * Mengubah status partisipasi.
             */
            public static final String UPDATE_STATUS = "UPDATE " + TABLE_PARTICIPATION + " SET " + STATUS + "=? WHERE "
                    + USER_ID + "=? AND " + CANVAS_ID + "=?";
            public static final int UPDATE_STATUS_USERID = 2;
            public static final int UPDATE_STATUS_STATUS = 1;
            public static final int UPDATE_STATUS_CANVASID = 3;
            /**
             * Mengubah waktu akses terakhir dan status aksi kanvas suatu kanvas berdasar id user dan id kanvas.
             */
            public static final String UPDATE_TRACE = "UPDATE " + TABLE_PARTICIPATION + " SET " + LAST_ACCESS + "=?, "
                    + ACTION + "=? WHERE " + USER_ID + "=? AND " + CANVAS_ID + "=?";
            public static final int UPDATE_TRACE_USERID = 3;
            public static final int UPDATE_TRACE_ACTION = 2;
            public static final int UPDATE_TRACE_CANVASID = 4;
            public static final int UPDATE_TRACE_LASTACCESS = 1;
        }
    }

    public static final class User {

        public static final String ID = "id";
        public static final String ACCOUNT_ID = "account_id";
        public static final String NAME = "nickname";
        public static final String STATUS = "status";

        public static final class Status {

            public static final String LOGIN = "LOGIN";
            public static final String LOGOUT = "LOGOUT";
        }

        public static final class Q {

            public static final int INSERT_USER_NAME = 2;
            public static final int INSERT_USER_STATUS = 3;
            public static final String INSERT_USER = "INSERT INTO " + TABLE_USER + "(" + ACCOUNT_ID + "," + NAME + ","
                    + STATUS + ") VALUES (?,?,?)";
            public static final int INSERT_USER_ACCOUNTID = 1;

            /**
             * Mengambil data user berdasarkan id
             */
            public static final String SELECT_BYID = "SELECT " + ACCOUNT_ID + ", " + NAME + ", " + STATUS + " FROM "
                    + TABLE_USER + " WHERE " + ID + "=?";
            public static final int SELECT_BYID_ID = 1;
            public static final int SELECT_BYID_RESULT_NAME = 2;
            public static final int SELECT_BYID_RESULT_STATUS = 3;
            public static final int SELECT_BYID_RESULT_ACCOUNTID = 1;
            public static final String SELECT_BYEMAIL = "SELECT " + ID + ", " + NAME + ", " + STATUS + " FROM "
                    + TABLE_USER + " WHERE " + ACCOUNT_ID + "=?";
            public static final int SELECT_BYEMAIL_EMAIL = 1;
            public static final int SELECT_BYEMAIL_RESULT_STATUS = 3;
            public static final int SELECT_BYEMAIL_RESULT_NAME = 2;
            public static final int SELECT_BYEMAIL_RESULT_ID = 1;
            public static final String UPDATE_STATUS_BYID = "UPDATE " + TABLE_USER + " SET " + STATUS + "=? WHERE " + ID
                    + "=?";
            public static final int UPDATE_STATUS_BYID_USERID = 2;
            public static final int UPDATE_STATUS_BYID_STATUS = 1;
        }
    }

    public static final class Q {

        /**
         * Mengambil daftar partisipasi user pada suatu kanvas, berikut. Data kanvas berisi
         * {@link Canvas#ID}, {@link Canvas#NAME}, {@link Canvas#WIDTH}, {@link Canvas#HEIGHT}, {@link Canvas#OWNER_ID}
         * dan {@link Participation#LAST_ACCESS}; terurut berdasarkan {@link Participation#LAST_ACCESS} menurun.
         */
        public static final String CANVAS_BY_USER = "SELECT c." + Canvas.ID + ", c." + Canvas.NAME + ", c."
                + Canvas.WIDTH + ", c." + Canvas.HEIGHT + ", p." + Participation.LAST_ACCESS + ", c."
                + Canvas.OWNER_ID + ", u." + User.NAME + ", p." + Participation.STATUS + " FROM "
                + DB.TABLE_CANVAS + " c, " + TABLE_PARTICIPATION + " p, " + TABLE_USER + " u WHERE p."
                + Participation.USER_ID + " = ? AND c." + Canvas.ID + "=p." + Participation.CANVAS_ID
                + " AND u." + User.ID + "=c." + Canvas.OWNER_ID + " ORDER BY " + Participation.LAST_ACCESS
                + " DESC";
        public static final int CANVAS_BY_USER_USERID = 1;
        public static final int CANVAS_BYUSER_RESULT_CANVASNAME = 2;
        public static final int CANVAS_BYUSER_RESULT_LASTACCESS = 5;
        public static final int CANVAS_BYUSER_RESULT_STATUS = 8;
        public static final int CANVAS_BYUSER_RESULT_CANVASWIDTH = 3;
        public static final int CANVAS_BYUSER_RESULT_CANVASHEIGHT = 4;
        public static final int CANVAS_BYUSER_RESULT_CANVASID = 1;
        public static final int CANVAS_BYUSER_RESULT_OWNERNAME = 7;
        public static final int CANVAS_BYUSER_RESULT_OWNERID = 6;

    }

}
