package collapaint.code;

/**
 * Berisi kode-kode JSON untuk request ke dan reply dari server.
 *
 * @author hamba v7
 */
public class ActionJCode {

    /**
     * Urutan aksi terakhir yang diketahui user
     */
    public static final String LAST_ACTION_NUM = "lan";
    /**
     * Id kanvas yang dipengaruhi aksi ini
     */
    public static final String CANVAS_ID = "cid";
    /**
     * Parameter lebar kanvas.
     */
    public static final String CANVAS_WIDTH = "wd";
    /**
     * Parameter tinggi kanvas.
     */
    public static final String CANVAS_HEIGHT = "wd";
    /**
     * Parameter left kanvas.
     */
    public static final String CANVAS_LEFT = "wd";
    /**
     * Parameter top kanvas.
     */
    public static final String CANVAS_TOP = "wd";
    /**
     * Daftar aksi.
     */
    public static final String ACTION_LIST = "act";
    /**
     * Objek dicantumkan di request.
     */
    public static final String ACTION_OBJ_LISTED = "ol";
    /**
     * Objek sudah diketahui.
     */
    public static final String ACTION_OBJ_KNOWN = "ok";
    /**
     * Kode aksi. Lihat {@link ActionJCode}
     */
    public static final String ACTION_CODE = "cd";
    /**
     * Parameter aksi.
     */
    public static final String ACTION_PARAM = "par";
    /**
     * Daftar objek.
     */
    public static final String OBJECT_LIST = "obj";
    /**
     * Id objek menurut katalog client.
     */
    public static final String OBJECT_LOCAL_ID = "id";
    /**
     * Id objek menurut katalog database.
     */
    public static final String OBJECT_GLOBAL_ID = "gid";
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
    public static final String OBJECT_STYLE = "st";

    /**
     * Menandakan bahwa aksi ini adalah aksi yang disubmit oleh klien.
     */
    public static final String ACTION_SUBMITTED = "as";
    /**
     * Terdapat kesalahan.
     */
    public static final String ERROR = "error";
    /**
     * Kesalahan pada server.
     */
    public static final int SERVER_ERROR = 3;
    /**
     * Request yang dimasukkan tidak valid.
     */
    public static final int BAD_REQUEST = 5;
}
