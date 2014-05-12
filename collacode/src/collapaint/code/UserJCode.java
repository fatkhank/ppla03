package collapaint.code;

/**
 * Daftar kode yang berkaitan dengan operasi manage user.
 *
 * @author hamba v7
 */
public class UserJCode {

    public static final class Request {

        /**
         * Aksi yang akan dilakukan.
         */
        public static final String ACTION = "act";
        /**
         * Aksi mengecek akun dan login.
         */
        public static final int ACTION_CHECK = 2;
        /**
         * Aksi logout.
         */
        public static final int ACTION_LOGOUT = 5;

        /**
         * Alamat email user.
         */
        public static final String USER_EMAIL = "email";
        /**
         * Nama user.
         */
        public static final String USER_NAME = "uname";
        /**
         * Id user.
         */
        public static final String USER_ID = "uid";
    }

    /**
     * Terjadi kesalahan.
     */
    public static final String ERROR = "error";

    public static final class Error {

        /**
         * Kesalahan dalam server.
         */
        public static final int SERVER_ERROR = 3;
        /**
         * Request memiliki format yang tidak valid.
         */
        public static final String BAD_REQUEST = "BAD_REQUEST";
    }

    public static final class Reply {

        /**
         * Id user yang telah dibuat.
         */
        public static final String USER_ID = "uid";
        /**
         * Status operasi
         */
        public static final String STATUS = "stat";
        /**
         * Login berhasil dengan akun sudah pernah ada.
         */
        public static final String ACCOUNT_EXIST = "ex";
        /**
         * Login berhasil dengan membuat akun baru.
         */
        public static final String ACCOUNT_CREATED = "cr";
        /**
         * Logout berhasil
         */
        public static final String LOGOUT_SUCESS = "SUCCESS";
    }

}
