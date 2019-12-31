package addixvn.webcrawl.db;

import addixvn.webcrawl.util.DataReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * データベースに関する諸々の処理クラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public final class Database {
    
    private final Connection conn;
    
    public Database(String path, String db_name) {
        System.out.println("\n********** Start Connecting Database **********\n");
        
        Connection conn = null;
        
        while (true) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + path + db_name);
                System.out.println("Database Info: " + "jdbc:sqlite:" + path + db_name);
                System.out.println("Connecting Database Completed.\n");
                break;
            } catch (ClassNotFoundException ex) {
                System.out.println("Database Connection: Failed [ClassNotFoundException]\n");
            } catch (SQLException ex) {
                System.out.println("Database Connection: Failed [SQLException]\n");
            }
        }
        this.conn = conn;
        this.executeQuery(path + "\\db\\prepare.sql");
    }
    
    /**
     * ファイルから読み込んだSQLクエリを実行するメソッド。
     * @param query_file クエリがまとまったファイル
     */
    public void executeQuery(String query_file) {
        System.out.println("\n********** Start Executing Query **********\n");
        
        DataReader dr = new DataReader(query_file, "Shift-JIS");
        try {
            Statement stmt = this.conn.createStatement();
            for (String query : dr.getArray()) {
                stmt.execute(query);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println("Executing Query: " + query + "\n(" + timestamp + ")\n");
            }
            System.out.println("Executing Query Completed.\n");
        } catch (SQLException ex) {
            System.out.println("\nExecuting Query Failed. [SQLException]\n");
        }
    }
    
    /**
     * 各求人情報をデータベース(Webテーブル)へ格納するメソッド。
     * @param array 各求人データ群
     */
    public void importDB(ArrayList<String> array) {
        String query = "INSERT INTO WebData (URL, S3URL, Title, CompanyName, Workplace, HeadOffice, PostalCode, Address, Recruiter, Phone, Fax, Email, Industry, Remarks, PostPeriod, Rank, Website) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstmt = this.conn.prepareStatement(query);
            for (int i = 0; i < array.size(); i++) {
                pstmt.setString(i + 1, array.get(i));
            }
            pstmt.executeUpdate();
            System.out.println("Inserting Data To Database: Succeeded");
        } catch (SQLException ex) {
            System.out.println("Inserting Data To Database: Failed [SQLException]");
        }
    }
    
    /**
     * カラム名を所定してリストデータとして出力するメソッド。
     * @param table テーブル名
     * @param columns データベースのカラム名
     * @return 条件に該当するデータ群
     */
    public ArrayList<String> exportDB(String table, String[] columns) {
        ArrayList<String> array = new ArrayList<>();
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < columns.length; i++) {
                    sb.append(rs.getString(columns[i]));
                    if (i != columns.length - 1) {
                        sb.append(",");
                    }
                }
                String s = sb.toString();
                array.add(s);
            }
        } catch (SQLException ex) {
            System.out.println("Exporting Data From Database Failed. [SQLException]\n");
        }
        return array;
    }
}