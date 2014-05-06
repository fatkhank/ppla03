package collapaint.code;

/**
 * Kode-kode yang berkaitan dengan operasi menutup atau membuka kanvas.
 *
 * @author hamba v7
 */
public final class PortalJCode {

    public static final class Request {

        /**
         * Aksi yang akan dilakukan.
         */
        public static final String ACTION = "act";
        /**
         * Aksi membuka kanvas.
         */
        public static final int ACTION_OPEN = 1;
        /**
         * Aksi menutup kanvas.
         */
        public static final int ACTION_CLOSE = 5;

        /**
         * Id kanvas yang akan dibuka atau ditutup.
         */
        public static final String CANVAS_ID = "cid";
        /**
         * Id user yang melakukan operasi.
         */
        public static final String USER_ID = "uid";
    }
    /**
     * Terdapat kesalahan.
     */
    public static final String ERROR = "error";

    /**
     * Kesalahan yang mungkin terjadi
     */
    public static final class Error {

        /**
         * Terdapat kesalahan pada server.
         */
        public static final String SERVER_ERROR = "SERVER_ERROR";
        /**
         * Request yang masuk tidak valid.
         */
        public static final String BAD_REQUEST = "BAD_REQUEST";
        /**
         * User tidak berhak membuka suatu kanvas.
         */
        public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";
        /**
         * Canvas dengan id yang dicantumkan tidak ditemukan.
         */
        public static final String CANVAS_NOT_FOUND = "NOT_FOUND";
    }

    public static final class Reply {

        public static final String CANVAS_ID = "id";
        public static final String CANVAS_NAME = "name";
        public static final String CANVAS_WIDTH = "w";
        public static final String CANVAS_HEIGHT = "h";
        public static final String CANVAS_TOP = "t";
        public static final String CANVAS_LEFT = "l";
        public static final String OWNER_ID = "oid";
        public static final String OWNER_NAME = "oname";
        public static final String CREATE_TIME = "create";
        public static final String LAST_ACTION_NUM = "lan";

        /**
         * Daftar ojek yang ada pada kanvas.
         */
        public static final String OBJECT_LIST = "obj";
        /**
         * Id objek menurut katalog database.
         */
        public static final String OBJECT_ID = "id";
        /**
         * Kode objek.
         */
        public static final String OBJECT_CODE = "cd";
        /**
         * Parameter transformasi objek.
         */
        public static final String OBJECT_TRANSFORM = "tx";
        /**
         * Parameter geometri objek.
         */
        public static final String OBJECT_GEOM = "ge";
        /**
         * Parameter style objek.
         */
        public static final String OBJECT_STYLE = "sy";
        /**
         * Parameter status objek.
         */
        public static final String OBJECT_EXIST = "ex";
    }

}
