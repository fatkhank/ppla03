package collapaint.transact;

/**
 *
 * @author hamba v7
 */
class CanvasObject {

    /**
     * Indeks objek menurut katalog klien.
     */
    int id;
    /**
     * Indeks objek menurut katalog database.
     */
    int globalId;
    int code;
    String geom;
    String style;
    String transform;

    public CanvasObject(int id, int globalId, int code, String geom,
            String style, String transform) {
        this.id = id;
        this.globalId = globalId;
        this.code = code;
        this.geom = geom;
        this.style = style;
        this.transform = transform;
    }

    /**
     * Membuat objek baru dengan suatu global id, namun belum data yang dimiliki belum lengkap.
     * @param gid indeks menurut katalog database.
     */
    public CanvasObject(int gid, int id) {
        this.globalId = gid;
        this.id = id;
    }
    
}
