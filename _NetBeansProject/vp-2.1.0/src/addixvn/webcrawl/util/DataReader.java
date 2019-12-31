package addixvn.webcrawl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * csvファイルから任意のデータを読み込むクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class DataReader {
    
    private final String web;
    private final String new_all;
    private final BufferedReader br;
    private HashMap<String, String> selector;
    private ArrayList<String> array;
    
    /**
     * 1列複数行のデータがまとまっているデータを読み込む際のコンストラクタ。
     * @param filename ファイル名
     * @param charset 文字コード
     */
    public DataReader(String filename, String charset) {
        System.out.println("Input File: " + filename);
        this.web = null;
        this.new_all = null;
        this.br = createBufferedReader(filename, charset);
        this.setArray();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Inputting File Completed." + " (" + timestamp + ")" + "\n");
    }
    
    /**
     * セレクターがまとまっているCSVファイルを読み込む際のコンストラクタ。
     * @param web ウェブサイト名
     * @param new_all 新着指定
     * @param filename ファイル名
     * @param charset 文字コード
     */
    public DataReader(String web, String new_all, String filename, String charset) {
        System.out.println("Input File: " + filename);
        this.web = web;
        this.new_all = new_all;
        this.br = createBufferedReader(filename, charset);
        this.setHashMap();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Inputting File Completed." + " (" + timestamp + ")" + "\n");
    }
    
    /**
     * BufferedReaderクラスのインスタンスを作成するメソッド。
     * @param filename ファイル名
     * @param charset 文字コード
     * @return BufferedReaderクラスのインスタンス
     */
    private static BufferedReader createBufferedReader(String filename, String charset) {
        BufferedReader br = null;
        try {
            FileInputStream f = new FileInputStream(new File(filename));
            br = new BufferedReader(new InputStreamReader(f, charset));
        } catch (UnsupportedEncodingException ex) {
            System.out.println("UnsupportedEncodingException");
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException");
        }
        return br;
    }
    
    /**
     * 1列のみかつ複数行にわたるデータを取得するメソッド。
     */
    private void setArray() {
        ArrayList<String> array = new ArrayList<>();
        String line;
        try {
            while ((line = this.br.readLine()) != null) {
                array.add(line);
            }
            this.br.close();
        } catch (IOException ex) {
            System.out.println("IOException");
        }
        this.array = array;
    }
    
    /**
     * CSVファイルからウェブサイト名・新着選択の有無に該当するセレクターを取得するメソッド。
     */
    private void setHashMap() {
        String line;
        HashMap hashmap = new HashMap<>();
        try {
            while ((line = this.br.readLine()) != null) {
                String lines[] = line.split(",");
                if (lines[0].equals(this.web) && lines[1].equals(this.new_all)) {
                    hashmap.put("url", lines[2]);
                    hashmap.put("total", lines[3]);
                    hashmap.put("href", lines[4]);
                    hashmap.put("rank", lines[5]);
                    hashmap.put("title", lines[6]);
                    hashmap.put("company", lines[7]);
                    hashmap.put("workplace", lines[8]);
                    hashmap.put("headoffice", lines[9]);
                    hashmap.put("address", lines[10]);
                    hashmap.put("recruiter", lines[11]);
                    hashmap.put("phone", lines[12]);
                    hashmap.put("fax", lines[13]);
                    hashmap.put("email", lines[14]);
                    hashmap.put("industry", lines[15]);
                    hashmap.put("remarks", lines[16]);
                    hashmap.put("period", lines[17]);
                    hashmap.put("replace_nxt_p", lines[18]);
                    hashmap.put("domain", lines[19]);
                    hashmap.put("title_rankT", lines[20]);
                    hashmap.put("company_rankT",lines[21]);
                    hashmap.put("workplace_rankT", lines[22]);
                    hashmap.put("address_rankT", lines[23]);
                    hashmap.put("industry_rankT", lines[24]);
                    break;
                }
            }
            this.br.close();
            this.selector = hashmap;
        } catch (IOException ex) {
            System.out.println("IOException -aa");
        }
    }
    
    /**
     * 読み取ってhashmap形式に格納したセレクター一覧のゲッター。
     * @return hashmap型のセレクター
     */
    public HashMap getSelector() {
        return this.selector;
    }
    
    /**
     * 読み取った1列複数行のデータのゲッター。
     * @return リスト型データ
     */
    public ArrayList<String> getArray() {
        return this.array;
    }
}