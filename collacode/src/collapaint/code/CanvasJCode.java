package collapaint.code;

/**
 * Daftar kode untuk operasi membuat kanvas
 *
 * @author hamba v7
 */
public class CanvasJCode {
    
    public static final class Request {

        /**
         * Aksi yang akan dilakukan.
         */
        public static final String ACTION = "act";

        /**
         * Aksi yang bisa dilakukan user
         */
        public static final class Action {

            /**
             * Aksi mengambil daftar kanvas.
             */
            public static final int LIST = 1;
            /**
             * Membuat kanvas baru.
             */
            public static final int CREATE = 2;
            /**
             * Menghapus suatu kanvas.
             */
            public static final int DELETE = 3;
        }

        /**
         * Id user yang melakukan permintaan.
         */
        public static final String USER_ID = "uid";

        public static final String CANVAS_ID = "cid";
        public static final String CANVAS_NAME = "n";
        public static final String CANVAS_WIDTH = "w";
        public static final String CANVAS_HEIGHT = "h";
        public static final String CANVAS_TOP = "t";
        public static final String CANVAS_LEFT = "l";
    }

    public static final class Reply {

        /**
         * Daftar kanvas yang ownernya adalah user yang meminta.
         */
        public static final String OWNED_LIST = "own";
        /**
         * Daftar kanvas yang sudah pernah dibuka, bukan milik user.
         */
        public static final String OLD_LIST = "old";
        /**
         * Daftar kanvas undangan baru ke user.
         */
        public static final String INVITATION_LIST = "new";
        
        public static final String CANVAS_ID = "cid";
        public static final String CANVAS_NAME = "n";
        public static final String CANVAS_WIDTH = "w";
        public static final String CANVAS_HEIGHT = "h";
        public static final String LAST_ACCESS = "las";
        public static final String OWNER_ID = "oid";
        public static final String OWNER_NAME = "on";
        /**
         * Hasil operasi hapus kanvas
         */
        public static final String DELETE_STATUS = "status";
        /**
         * Kanvas berhasil dihapus
         */
        public static final int DELETE_STATUS_SUCCESS = 1;
    }

    /**
     * Kode jika mengalami error
     */
    public static final String ERROR = "error";

    public static final class Error {

        /**
         * Kesalahan dalam server.
         */
        public static final String SERVER_ERROR = "SERVER_ERROR";
        /**
         * Request memiliki format yang tidak valid.
         */
        public static final String BAD_REQUEST = "BAD_REQUEST";
        /**
         * User bukan owner, sehingga tidak berhak menghapus kanvas
         */
        public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";
        /**
         * Kanvas dengan id yang disebutkan tidak ditemukan
         */
        public static final String CANVAS_NOT_FOUND = "NOT_FOUND";
        /**
         * Nama kanvas yang sama sudah perah dibuat oleh user.
         */
        public static final String DUPLICATE_NAME = "DUPLICATE_NAME";
    }
}
