package io.github.sawors.tiboise.core.database;

import io.github.sawors.tiboise.Main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseLink {
    // |====================================[GIT GUD]=====================================|
    // |                     Reminder for the newbie I'm in SQL :                         |
    // | -> Set  : INSERT into [table]([column]) VALUES([value])                          |
    // | -> Get  : SELECT [column] FROM [table] // WHERE [condition]=[something]          |
    // | -> Edit : UPDATE [table] SET [column] = [value] // WHERE [condition]=[something] |
    // | -> Del  : DELETE FROM [table] WHERE [condition]=[something]                      |
    // | ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~  |
    // | DataTypes :                                                                      |
    // |   NULL. The value is a NULL value.                                               |
    // |                                                                                  |
    // |   INTEGER. The value is a signed integer, stored in 0, 1, 2, 3, 4, 6, or 8 bytes |
    // |            depending on the magnitude of the value.                              |
    // |                                                                                  |
    // |   REAL. The value is a floating point value, stored as an 8-byte IEEE            |
    // |         floating point number.                                                   |
    // |                                                                                  |
    // |   TEXT. The value is a text string, stored using the database encoding           |
    // |         (UTF-8, UTF-16BE or UTF-16LE).                                           |
    // |                                                                                  |
    // |   BLOB. The value is a blob of data, stored exactly as it was input.             |
    // |==================================================================================|
    
    public static void connectInit(){
        try(Connection co = connect()){
            //  Init "Users" table
            co.createStatement().execute(createPlayerPreferencesTable());
        } catch (
                SQLException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private static Connection connect(){
        Connection co;
        try{
            String target = "jdbc:sqlite:"+ Main.getDbFile().getCanonicalFile();
            co = DriverManager.getConnection(target);
            return co;
        } catch (
                IOException |
                SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String createPlayerPreferencesTable(){
        return "CREATE TABLE IF NOT EXISTS Users ("
                + DBPlayerOptions.USERID+" text NOT NULL UNIQUE,"
                + DBPlayerOptions.CUSTOM_NAME+" text UNIQUE,"
                + DBPlayerOptions.HIDE_NAMETAG+" integer"
                + DBPlayerOptions.CONNECTION_MESSAGES+" integer"
                + ");"
                ;
    }
}
