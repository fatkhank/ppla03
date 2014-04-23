/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collapaint;

/**
 *
 * @author hamba v7
 */
public class DB {

    public static final String URL = "jdbc:mysql://localhost/collapaint";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    public static final String TABLE_CANVAS = "canvas";
    public static final String TABLE_OBJECT = "object";
    public static final String TABLE_PARTICIPATION = "participation";
    public static final String TABLE_USER = "user";

    public static final class CANVAS {

        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String COL_WIDTH = "width";
        public static final String COL_HEIGHT = "height";
        public static final String COL_OWNER = "owner";
    }
    
    public static final class OBJECTS{
        public static final String COL_ID = "id";
    }

    public static final class PARTICIPATION {

        public static final String COL_USER = "user_id";
        public static final String COL_CANVAS = "canvas_id";
        public static final String COL_STATUS = "status";
    }
    
    public static final class USER {

        public static final String COL_ID = "id";
        public static final String COL_ACCOUNT_ID = "account_id";
        public static final String COL_NAME = "nickname";
    }

}
