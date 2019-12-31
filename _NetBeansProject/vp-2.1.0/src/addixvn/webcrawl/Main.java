package addixvn.webcrawl;

import addixvn.webcrawl.web.Doda;
import addixvn.webcrawl.web.Ecareer;
import addixvn.webcrawl.web.Enjapan;
import addixvn.webcrawl.web.Mynavi;
import addixvn.webcrawl.web.Rikunabi;
import addixvn.webcrawl.web.Type;
import addixvn.webcrawl.web.WomanType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Scanner;

/**
 * このプロジェクトのメインクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Main {
    
    public static void main(String[] args) {
        String path = new File(".").getAbsoluteFile().getParent().replace("\\jar", "");
        Scanner sc = new Scanner(System.in);
        
        // ウェブサイト名選択
        String[] option_web = {"doda", "mynavi", "rikunabi", "en-japan", "woman-type", "type", "ecareer"};
        String web = selectOption(sc, option_web, "Website Name");
        
        // 新着情報取得の選択
        String[] option_new_all = {"new", "all"};
        String new_all = selectOption(sc, option_new_all, "New or All");
        
        // プロジェクトディレクトリ手動設定の選択
        boolean bool_path = setBool(sc, "Do You Want To Set Project Directory?");
        if (bool_path) {
            path = setPath(sc);
        }
        
        // ログファイル出力設定の選択
        boolean bool_log = setBool(sc, "Do You Want To Export Log File?");
        if (bool_log) {
            setLogFile(path);
        }
        
        startDisplay(path, web);
        
        switch (web) {
            case "doda":
                Doda doda = new Doda(path, web, new_all, 50);
                doda.main();
                break;
            case "mynavi":
                Mynavi mynavi = new Mynavi(path, web, new_all, 50);
                mynavi.main();
                break;
            case "rikunabi":
                Rikunabi rikunabi = new Rikunabi(path, web, new_all, 50);
                rikunabi.main();
                break;
            case "en-japan":
                Enjapan enjapan = new Enjapan(path, web, new_all, 50);
                enjapan.main();
                break;
            case "woman-type":
                WomanType womantype = new WomanType(path, web, new_all, 20);
                womantype.main();
                break;
            case "type":
                Type type = new Type(path, web, new_all, 20);
                type.main();
                break;
            case "ecareer":
                Ecareer ecareer = new Ecareer(path, web, new_all, 30);
                ecareer.main();
                break;
        }
        
        finishDisplay();
    }
    
    /**
     * 選択肢の中から情報を取得するメソッド。
     * @param sc Scannerクラスのインスタンス
     * @param options 選択肢
     * @param question 質問
     * @return 選択肢の中から選んだ回答
     */
    private static String selectOption(Scanner sc, String[] options, String question) {
        String str;
        boolean check = false;
        while (0 == 0) {
            System.out.print(question + " (Example:");
            for (String op : options) {
                System.out.print(" \"" + op + "\"");
            }
            System.out.print("): ");
            
            str = sc.nextLine();
            for (String op : options) {
                if (str.equals(op)) {
                    check = true;
                    break;
                }
            }
            if (check) {
                break;
            } else {
                System.out.println("Inputted Information Was Not Correct. Please Try Again.\n");
            }
        }
        return str;
    }
    
    /**
     * 任意のメッセージに対して実行するかしないかを選択させるメソッド。
     * @param sc Scannerクラスのインスタンス
     * @param message 表示するメッセージ
     * @return boolean型の値("true" or "false")
     */
    private static boolean setBool(Scanner sc, String message) {
        boolean bool;
        while (0 == 0) {
            System.out.print(message + " (Y/N): ");
            String str = sc.nextLine();
            if (str.equals("Y") || str.equals("y")) {
                bool = true;
                break;
            } else if (str.equals("N") || str.equals("n")) {
                bool = false;
                break;
            } else {
                System.out.println("Inputted Information Was Not Correct. Please Try Again.\n");
            }
        }
        return bool;
    }
    
    /**
     * ディレクトリを手動で設定するメソッド。
     * @param sc Scannerクラスのインスタンス
     * @return プロジェクトディレクトリ
     */
    private static String setPath(Scanner sc) {
        System.out.print("Project Directory Name: ");
        String path = sc.nextLine();
        return path;
    }
    
    /**
     * System_out_printlnをログファイル出力へ切り替えるメソッド。
     * @param path プロジェクトディレクトリ
     */
    private static void setLogFile(String path) {
        try {
            String log_file = path + "\\jar\\vp-2.0.0.log";
            System.out.println("\nChanged Log Place From Command To: " + log_file + "\nThis Project Is Running, Please Wait...");
            PrintStream ps = new PrintStream(new FileOutputStream(log_file));
            System.setOut(ps);
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException: \\jar\\log.txt");
        }
    }
    
    /**
     * 動作を開始する時の表示。
     * @param path プロジェクトディレクトリ
     * @param web 選択されたウェブサイト名
     */
    private static void startDisplay(String path, String web) {
        System.out.println("\n********** Start Web Crawling **********\n");
        System.out.println("Project Directory: " + path);
        System.out.println("Website Name: " + web);
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Start Time: " + timestamp + "\n");
    }
    
    /**
     * 全ての動作が終了した時の表示。
     */
    private static void finishDisplay() {
        System.out.println("\n********** Finish Web Crawling **********\n");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Finish Time: " + timestamp + "\n");
    }
}