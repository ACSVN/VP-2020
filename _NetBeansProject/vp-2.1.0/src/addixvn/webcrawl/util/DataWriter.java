package addixvn.webcrawl.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 任意のデータを文字コードを指定してファイルへ出力するクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class DataWriter {

    private final BufferedWriter bw;
    
    /**
     * 文字コードを指定して出力ファイルの準備を行うコンストラクタ。
     * @param filename 出力ファイル名
     * @param charset 出力文字コード
     */
    public DataWriter(String filename, String charset) {
        System.out.println("Export File: " + filename);
        BufferedWriter bw = null;
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
            bw = new BufferedWriter(osw);
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("UnsupportedEncodingException");
        }
        this.bw = bw;
    }
    
    /**
     * HashMapからHrefリストを出力するメソッド。
     * @param arr リストデータ
     */
    public void printHref(ArrayList<HashMap> arr) {
        try {
            for (HashMap hashmap : arr) {
                this.bw.write(hashmap.get("href").toString());
                this.bw.newLine();
            }
            this.bw.close();
        } catch (IOException ex) {
            System.out.println("IOException");
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Exporting File Completed." + " (" + timestamp + ")" + "\n");
    }
    
    /**
     * 任意のリストを出力するメソッド。
     * @param arr リストデータ
     */
    public void printData(ArrayList<String> arr) {
        try {
            for (String href : arr) {
                this.bw.write(href);
                this.bw.newLine();
            }
            this.bw.close();
        } catch (IOException ex) {
            System.out.println("IOException");
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Exporting File Completed." + " (" + timestamp + ")" + "\n");
    }
    
    /**
     * 任意のリストを先頭行の文字列付きで出力するメソッド。
     * @param arr リストデータ
     * @param index 先頭行の文字列
     */
    public void printData(ArrayList<String> arr, String index) {
        try {
            this.bw.write(index);
            this.bw.newLine();
            this.printData(arr);
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }
}