package collapaint.code;

/**
 * Kode untuk operasi yang berkaitan dengan partisipan.
 *
 * @author hamba v7
 */
public class ParticipantJCode {

    public static final class Request {

        /**
         * Aksi yang akan dilakukan.
         *
         * @see Action
         */
        public static final String ACTION = "act";

        /**
         * Aksi yang bisa dilakukan user.
         */
        public static final class Action {

            /**
             * Aksi untuk membuang seorang partisipan.
             */
            public static final int KICK = 8;
            /**
             * Aksi untuk menanggapi suatu undangan.
             */
            public static final int RESPONSE = 1;
            /**
             * Aksi untuk mengambil daftar partisipan.
             */
            public static final int LIST = 2;
            /**
             * Aksi untuk mengundang seorang user.
             */
            public static final int INVITE = 5;
        }

        /**
         * Id kanvas yang dimaksud
         */
        public static final String CANVAS_ID = "cid";
        /**
         * Id user
         */
        public static final String USER_ID = "uid";
        /**
         * Id user yang me-kick
         */
        public static final String KICKER_ID = "kid";
        /**
         * Alamat email user.
         */
        public static final String USER_EMAIL = "email";

        /**
         * Response terhadap suatu undangan.
         *
         * @see Response
         */
        public static final String RESPONSE = "res";

        /**
         * Response yang mungkin terhadap suatu undangan.
         */
        public static final class Response {

            /**
             * Undangan diterima.
             */
            public static final int ACCEPT = 9;
            /**
             * Undangan ditolak.
             */
            public static final int DECLINE = 3;
        }

    }

    public static final class Reply {
        
        public static final String CANVAS_ID = "cid";
        /**
         * Id user
         */
        public static final String USER_ID = "uid";
        /**
         * Alamat email user.
         */
        public static final String USER_EMAIL = "email";

        /**
         * Status operasi invite.
         *
         * @see InviteStatus
         */
        public static final String INVITE_STATUS = "status";

        /**
         * Status operasi invite yang mungkin
         */
        public static final class InviteStatus {

            /**
             * Operasi berhasil.
             */
            public static final String SUCCESS = "success";

            /**
             * Status undangan bahwa user sudah bergabung.
             */
            public static final String ALREADY_JOINED = "joined";

            /**
             * Status undangan bahwa user sudah pernah diinvite.
             */
            public static final String ALREADY_INVITED = "invited";
        }

        /**
         * Daftar partisipan pada kanvas.
         */
        public static final String PARTICIPANT_LIST = "list";

        /**
         * Nama partisipan.
         */
        public static final String USER_NAME = "name";

        /**
         * Status partisipasi user.
         */
        public static final String PARTICIPANT_STATUS = "stat";

        public static final class ParticipantStatus {

            /**
             * Status partisipasi, bahwa user sudah bergabung sebagai member
             */
            public static final int MEMBER = 9;
            /**
             * Status partisipasi, bahwa user sedang diundang untuk bergabung.
             */
            public static final int INVITATION = 3;
            /**
             * Status partisipasi, bahwa user sudah bergabung sebagai owner.
             */
            public static final int OWNER = 4;
        }

        /**
         * Aksi dari partisipan.
         */
        public static final String PARTICIPANT_ACTION = "act";
        /**
         * Aksi partisipan bahwa kanvas sedang dibuka user.
         */
        public static final int PARTICIPANT_ACTION_OPEN = 5;
        /**
         * Aksi partisipan bahwa kanvas tidak sedang dibuka user.
         */
        public static final int PARTICIPANT_ACTION_CLOSE = 9;

        /**
         * Waktu akses terakhir seorang partisipan
         */
        public static final String LAST_ACCESS = "lass";

        /**
         * Status operasi kick
         */
        public static final String KICK_STATUS = "stat";

        /**
         * Status operasi kick yang mungkin terjadi.
         */
        public static final class KickStatus {

            /**
             * Operasi kick berhasil dijalankan.
             */
            public static final String SUCCESS = "success";

            /**
             * User yang dikick bukanlah seorang partisipan dari kanvas yang dimaksud.
             */
            public static final String NOT_A_MEMBER = "NOT_MEMBER";
        }

        /**
         * Status pada operasi menjawab undangan
         */
        public static final String RESPONSE_STATUS = "stat";

        /**
         * Status yang mungkin pada saat operasi menjawab undangan.
         */
        public static final class ResponseStatus {

            /**
             * Operasi berhasil.
             */
            public static final String SUCCESS = "success";

            /**
             * Status undangan bahwa user sudah bergabung.
             */
            public static final String ALREADY_JOINED = "joined";
        }
    }

    /**
     * Kode jika mengalami error.
     */
    public static final String ERROR = "error";

    /**
     * Berisi kode kesalahan yang mungkin terjadi.
     */
    public static final class Error {

        /**
         * Request memiliki format yang tidak valid.
         */
        public static final String BAD_REQUEST = "BAD_REQUEST";
        /**
         * Pengguna yang diundang belum terdaftar.
         */
        public static final String USER_UNREGISTERED = "unregistered";
        /**
         * Kesalahan dalam server.
         */
        public static final int SERVER_ERROR = 3;
        /**
         * User tidak berhak melakukan operasi ini.
         */
        public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";
        /**
         * Kanvas yang berkaitan dengan operasi tidak terdafar.
         */
        public static final String CANVAS_NOT_FOUND = "NOT_FOUND";
    }
}
