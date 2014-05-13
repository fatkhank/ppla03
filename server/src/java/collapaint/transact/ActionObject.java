package collapaint.transact;

import collapaint.code.ActionCode;

/**
 *
 * @author hamba v7
 */
class Action {

    /**
     * Id aksi.
     */
    int id;
    int code;
    int canvas_id;
    String param;

    public Action(int canvasID, int code, String param) {
        this.canvas_id = canvasID;
        this.code = code;
        this.param = param;
    }
}

class ActionObject extends Action {

    int objectID;

    public ActionObject(int canvasID, int objectID, int code, String param) {
        super(canvasID, code, param);
        this.objectID = objectID;
    }
}

class Resize extends Action {

    int width;
    int height;
    int top;
    int left;

    public Resize(int canvasID, int width, int height, int top, int left, String param) {
        super(canvasID, ActionCode.RESIZE_ACTION, param);
        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;
    }

}
