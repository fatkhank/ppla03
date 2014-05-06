package collapaint;

/**
 *
 * @author hamba v7
 */
public class DB {

    public static final String DB_URL = "jdbc:mysql://localhost/collapaint";
    public static final String DB_NAME = "collapaint";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "";

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

            public static final class Insert {

                public static final String ALL = "INSERT INTO " + DB.TABLE_ACTION + "(" + DB.Action.CANVAS_ID + ","
                        + DB.Action.CODE + "," + DB.Action.OBJECT_ID + "," + DB.Action.PARAMETER + ") VALUES (?,?,?,?)";

                public static final class All {

                    public static final int CANVAS_ID = 1;
                    public static final int CODE = 2;
                    public static final int OBJECT_ID = 3;
                    public static final int PARAMETER = 4;
                }
            }

            public static final class Select {

                /**
                 * Mengambil aksi terakhir dari {@link Lastest#LIMIT_MIN} sejumlah {@link Lastest#LIMIT_COUNT}.
                 */
                public static final String LASTEST = "SELECT " + Action.ID + "," + Action.CANVAS_ID + "," + Action.CODE
                        + "," + Action.OBJECT_ID + "," + Action.PARAMETER + " FROM " + TABLE_ACTION + " WHERE "
                        + Action.CANVAS_ID + " = ? ORDER BY " + Action.ID + " LIMIT ?, ?";

                public static final class Lastest {

                    public static final int CANVAS_ID = 1;
                    public static final int LIMIT_MIN = 2;
                    public static final int LIMIT_COUNT = 3;

                    public static final class Column {

                        public static final int ID = 1;
                        public static final int CANVAS_ID = 2;
                        public static final int CODE = 3;
                        public static final int OBJECT_ID = 4;
                        public static final int PARAMETER = 5;
                    }
                }

                /**
                 * Mengambil jumlah aksi pada suatu kanvas.
                 */
                public static final String COUNT = "SELECT COUNT(*) FROM " + TABLE_ACTION + " WHERE " + Action.CANVAS_ID
                        + "=?";

                public static final class Count {

                    public static final int CANVAS_ID = 1;

                    public static final class Column {

                        public static final int COUNT = 1;
                    }
                }
            }
        }
    }

    public static final class Canvas {

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String TOP = "top";
        public static final String LEFT = "`left`";
        public static final String OWNER_ID = "owner";
        public static final String CREATE_TIME = "create_time";

        public static final class Q {

            public static final class Delete {

                /**
                 * Menghapus suatu kanvas berdasarkan parameter id kanvas sebagai {@link ById#CANVAS_ID}.
                 */
                public static final String BY_ID = "DELETE FROM " + TABLE_CANVAS + " WHERE " + Canvas.ID + "=?";

                public static final class ById {

                    public static final int CANVAS_ID = 1;
                }
            }

            public static final class Insert {

                /**
                 * Membuat kanvas baru, dengan data
                 */
                public static final String ALL = "INSERT INTO " + TABLE_CANVAS + "(" + Canvas.NAME + ", " + Canvas.WIDTH
                        + ", " + Canvas.HEIGHT + ", " + Canvas.TOP + ", " + Canvas.LEFT + ", " + Canvas.OWNER_ID
                        + ", " + Canvas.CREATE_TIME + ") VALUES (?,?,?,?,?,?,?)";

                public static final class All {

                    public static final int NAME = 1;
                    public static final int WIDTH = 2;
                    public static final int HEIGHT = 3;
                    public static final int TOP = 4;
                    public static final int LEFT = 5;
                    public static final int OWNER_ID = 6;
                    public static final int CREATE_TIME = 7;
                }
            }

            public static final class Select {

                /**
                 * Mengambil daftar kanvas yang dimiliki oleh seorang user. Data kanvas berisi
                 * {@link Canvas#ID}, {@link Canvas#NAME}, {@link Canvas#WIDTH}, {@link Canvas#HEIGHT}, dan
                 * {@link Canvas#CREATE_TIME}; terurut berdasarkan {@link Canvas#CREATE_TIME} menurun.
                 */
                public static final String BY_OWNER = "SELECT " + Canvas.ID + "," + Canvas.NAME + ","
                        + Canvas.WIDTH + "," + Canvas.HEIGHT + "," + Canvas.CREATE_TIME + " FROM " + DB.TABLE_CANVAS
                        + " WHERE " + DB.Canvas.OWNER_ID + " = ? ORDER BY " + Canvas.CREATE_TIME + " DESC";

                public static final class ByOwner {

                    public static final int OWNER_ID = 1;

                    public static final class Column {

                        public static final int ID = 1;
                        public static final int NAME = 2;
                        public static final int WIDTH = 3;
                        public static final int HEIGHT = 4;
                        public static final int CREATE_TIME = 5;
                    }
                }

                /**
                 * Mengambil owner dari suatu kanvas berdasarkan id kanvas ({@link OwnerOf#CANVAS_ID}). Mengembalikan id
                 * dan nama owner.
                 */
                public static final String OWNER_OF = "SELECT c." + Canvas.OWNER_ID + ", u." + User.NAME + " FROM "
                        + TABLE_CANVAS + " c, " + TABLE_USER
                        + " u WHERE c." + Canvas.ID + "=? AND u." + User.ID + "=c." + OWNER_ID;

                public static final class OwnerOf {

                    public static final int CANVAS_ID = 1;

                    public static final class Column {

                        public static final int OWNER_ID = 1;
                        public static final int OWNER_NAME = 2;
                    }
                }

                /**
                 * Mengambil detail kanvas berdasarkan idnya, berikut id owner dan nama owner.
                 */
                public static final String DETAIL_BY_ID = "SELECT c." + Canvas.ID + ", c." + Canvas.NAME + ", c."
                        + Canvas.WIDTH + ", c." + Canvas.HEIGHT + ", c." + Canvas.TOP + ", c." + Canvas.LEFT + ", c."
                        + Canvas.CREATE_TIME + ", c." + Canvas.OWNER_ID + ", u." + User.NAME + " FROM " + TABLE_CANVAS
                        + " c, " + TABLE_USER + " u WHERE c." + Canvas.ID + "=? AND u." + User.ID + "=c."
                        + Canvas.OWNER_ID;

                public static final class DetailById {

                    public static final int CANVAS_ID = 1;

                    public static final class Column {

                        public static final int CANVAS_ID = 1;
                        public static final int CANVAS_NAME = 2;
                        public static final int WIDTH = 3;
                        public static final int HEIGHT = 4;
                        public static final int TOP = 5;
                        public static final int LEFT = 6;
                        public static final int CREATE_TIME = 7;
                        public static final int OWNER_ID = 8;
                        public static final int OWNER_NAME = 9;

                    }
                }

            }

            public static final class Update {

                public static final String SIZE = "UPDATE " + TABLE_CANVAS + " SET " + WIDTH + "=?, " + HEIGHT + "=?, "
                        + TOP + "=?, " + LEFT + "=? WHERE " + ID + " = ?";

                public static final class Size {

                    public static final int WIDTH = 1;
                    public static final int HEIGHT = 2;
                    public static final int TOP = 3;
                    public static final int LEFT = 4;
                    public static final int ID = 5;
                }
            }
        }
    }

    public static final class Objects {

        public static final String ID = "id";
        public static final String CANVAS_ID = "canvas_id";
        public static final String CODE = "code";
        public static final String TRANSFORM = "transform";
        public static final String GEOM = "geom";
        public static final String STYLE = "style";
        public static final String EXIST = "exist";

        public static final class Q {

            public static final class Insert {

                /**
                 * Memasukkan sebuah objek ke database.
                 */
                public static final String ALL = "INSERT INTO " + DB.TABLE_OBJECT + "(" + DB.Objects.CANVAS_ID + ","
                        + DB.Objects.CODE + "," + DB.Objects.TRANSFORM + "," + DB.Objects.STYLE + "," + DB.Objects.GEOM
                        + "," + DB.Objects.EXIST + ") VALUES (?,?,?,?,?,?)";

                public static final class All {

                    public static final int CANVAS_ID = 1;
                    public static final int CODE = 2;
                    public static final int TRANSFORM = 3;
                    public static final int STYLE = 4;
                    public static final int GEOM = 5;
                    public static final int EXIST = 6;
                }
            }

            public static final class Select {

                /**
                 * Mengambil daftar objek pada suatu kanvas.
                 */
                public static final String BY_CANVAS_ID = "SELECT " + Objects.ID + ", " + Objects.CODE + ", "
                        + Objects.GEOM + ", " + Objects.STYLE + ", " + Objects.TRANSFORM + ", " + Objects.EXIST
                        + " FROM " + TABLE_OBJECT + " WHERE " + Objects.CANVAS_ID + "=?";

                public static final class ByCanvasId {

                    public static final int CANVAS_ID = 1;

                    public static final class Column {

                        public static final int ID = 1;
                        public static final int CODE = 2;
                        public static final int GEOM = 3;
                        public static final int STYLE = 4;
                        public static final int TRANSFORM = 5;
                        public static final int EXIST = 6;
                    }
                }

                /**
                 * Mengambil data objek berdasarkan id objek.
                 */
                public static final String BY_ID = "SELECT " + Objects.ID + ", " + Objects.CODE + ", " + Objects.GEOM
                        + ", " + Objects.STYLE + ", " + Objects.TRANSFORM + ", " + Objects.EXIST + " FROM "
                        + TABLE_OBJECT + " WHERE " + Objects.ID + "=?";

                public static final class ById {

                    public static final int ID = 1;

                    public static final class Column {

                        public static final int ID = 1;
                        public static final int CODE = 2;
                        public static final int GEOM = 3;
                        public static final int STYLE = 4;
                        public static final int TRANSFORM = 5;
                        public static final int EXIST = 6;
                    }
                }
            }

            public static final class Update {

                /**
                 * Mengganti data geom, transform, style, dan status
                 */
                public static final String DATA = "UPDATE " + DB.Objects + " SET " + DB.Objects.GEOM + "=?, "
                        + DB.Objects.STYLE + "=?, " + DB.Objects.TRANSFORM + "=?, " + DB.Objects.EXIST + "=? WHERE "
                        + DB.Objects.CODE + "=?";

                public static final class Data {

                    public static final int GEOM = 1;
                    public static final int STYLE = 2;
                    public static final int TRANS = 3;
                    public static final int EXIST = 4;
                    public static final int OBJECT_ID = 5;
                }

                public static final String TRANSFORM = "UPDATE " + DB.Objects + " SET " + DB.Objects.TRANSFORM
                        + "=? WHERE " + DB.Objects.CODE + "=?";

                public static final class Transform {

                    public static final int TRANS_PARAM = 1;
                    public static final int OBJECT_ID = 2;
                }
                public static final String GEOM = "UPDATE " + DB.Objects + " SET " + DB.Objects.GEOM + "=? WHERE "
                        + DB.Objects.CODE + "=?";

                public static final class Geom {

                    public static final int GEOM_PARAM = 1;
                    public static final int OBJECT_ID = 2;
                }
                public static final String STYLE = "UPDATE " + DB.Objects + " SET " + DB.Objects.STYLE + "=? WHERE "
                        + DB.Objects.CODE + "=?";

                public static final class Style {

                    public static final int STYLE_PARAM = 1;
                    public static final int OBJECT_ID = 2;
                }
                public static final String STATUS = "UPDATE " + DB.Objects + " SET " + DB.Objects.EXIST + "=? WHERE "
                        + DB.Objects.CODE + "=?";

                public static final class Status {

                    public static final int STATUS_PARAM = 1;
                    public static final int OBJECT_ID = 2;
                }
            }
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

            public static final class Delete {

                public static final String RECORD = "DELETE FROM " + TABLE_PARTICIPATION + " WHERE "
                        + Participation.CANVAS_ID + "=? AND " + Participation.USER_ID + "=?";

                public static final class Record {

                    public static final int CANVAS_ID = 1;
                    public static final int USER_ID = 2;
                }
            }

            public static final class Insert {

                /**
                 * Memasukkan data partisipasi baru.
                 */
                public static final String ALL = "INSERT INTO " + TABLE_PARTICIPATION + "(" + Participation.USER_ID
                        + "," + Participation.CANVAS_ID + "," + Participation.LAST_ACCESS + "," + Participation.STATUS
                        + "," + Participation.ACTION + ") VALUES (?,?,?,?,?)";

                public static final class All {

                    public static final int USER_ID = 1;
                    public static final int CANVAS_ID = 2;
                    public static final int LAST_ACCESS = 3;
                    public static final int STATUS = 4;
                    public static final int ACTION = 5;
                }
            }

            public static final class Select {

                /**
                 * Mengecek ada tidaknya partisipasi seorang user pada sebuah kanvas.
                 */
                public static final String CHECK_PARTICIPATION = "SELECT " + Participation.STATUS + ", "
                        + Participation.ACTION + " FROM " + TABLE_PARTICIPATION + " WHERE " + Participation.USER_ID
                        + "=? AND " + Participation.CANVAS_ID + "=?";

                public static final class CheckParticipation {

                    public static final int USER_ID = 1;
                    public static final int CANVAS_ID = 2;

                    public static final class Column {

                        public static final int STATUS = 1;
                        public static final int ACTION = 2;
                    }
                }

                public static final String BY_CANVAS = "SELECT u." + User.ID + ", u." + User.NAME + ", p."
                        + Participation.STATUS + ", p." + Participation.ACTION + ", p." + Participation.LAST_ACCESS
                        + " FROM " + TABLE_PARTICIPATION + " p, " + TABLE_USER + " u WHERE "
                        + Participation.CANVAS_ID + "=? AND u." + User.ID + "=p." + Participation.USER_ID;

                public static final class ByCanvas {

                    public static final int CANVAS_ID = 1;

                    public static final class Column {

                        public static final int USER_ID = 1;
                        public static final int USER_NAME = 2;
                        public static final int STATUS = 3;
                        public static final int ACTION = 4;
                        public static final int LAST_ACCESS = 5;
                    }
                }
            }

            public static final class Update {

                /**
                 * Mengubah status partisipasi.
                 */
                public static final String STATUS = "UPDATE " + TABLE_PARTICIPATION + " SET "
                        + Participation.STATUS + "=? WHERE "
                        + Participation.USER_ID + "=? AND "
                        + Participation.CANVAS_ID + "=?";

                public static final class Status {

                    public static final int STATUS = 1;
                    public static final int USER_ID = 2;
                    public static final int CANVAS_ID = 3;
                }

                /**
                 * Mengubah waktu akses terakhir dan status aksi kanvas suatu kanvas berdasar id user dan id kanvas.
                 */
                public static final String TRACE = "UPDATE " + TABLE_PARTICIPATION + " SET "
                        + Participation.LAST_ACCESS + "=?, " + Participation.ACTION + "=? WHERE "
                        + Participation.USER_ID + "=? AND "
                        + Participation.CANVAS_ID + "=?";

                public static final class Trace {

                    public static final int LAST_ACCESS = 1;
                    public static final int ACTION = 2;
                    public static final int USER_ID = 3;
                    public static final int CANVAS_ID = 4;
                }
            }
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

            public static final class Insert {

                public static final String ALL = "INSERT INTO " + TABLE_USER + "(" + User.ACCOUNT_ID + "," + User.NAME
                        + "," + User.STATUS + ") VALUES (?,?,?)";

                public static final class All {

                    public static final int ACCOUNT_ID = 1;
                    public static final int NAME = 2;
                    public static final int STATUS = 3;
                }
            }

            public static final class Select {

                public static final String BY_ID = "SELECT " + User.ACCOUNT_ID + ", " + User.NAME
                        + ", " + User.STATUS + " FROM " + TABLE_USER + " WHERE " + User.ID + "=?";

                public static final class ById {

                    public static final int ID = 1;

                    public static final class Column {

                        public static final int ACCOUNT_ID = 1;
                        public static final int NAME = 2;
                        public static final int STATUS = 3;
                    }
                }

                public static final String BY_EMAIL = "SELECT " + User.ID + ", " + User.NAME + ", " + User.STATUS
                        + " FROM " + TABLE_USER + " WHERE " + User.ACCOUNT_ID + "=?";

                public static final class ByEmail {

                    public static final int EMAIL = 1;

                    public static final class Column {

                        public static final int ID = 1;
                        public static final int NAME = 2;
                        public static final int STATUS = 3;
                    }
                }
            }

            public static final class Update {

                public static final String STATUS_BY_ID = "UPDATE " + TABLE_USER + " SET " + User.STATUS + "=? WHERE "
                        + User.ID + "=?";

                public static final class StatusById {

                    public static final int STATUS = 1;
                    public static final int USER_ID = 2;
                }
            }
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

        public static final class CanvasByUser {

            public static final int USER_ID = 1;

            public static final class Column {

                public static final int CANVAS_ID = 1;
                public static final int CANVAS_NAME = 2;
                public static final int CANVAS_WIDTH = 3;
                public static final int CANVAS_HEIGHT = 4;
                public static final int LAST_ACCESS = 5;
                public static final int OWNER_ID = 6;
                public static final int OWNER_NAME = 7;
                public static final int STATUS = 8;
            }
        }
    }

}
