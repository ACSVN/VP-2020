package addixvn.webcrawl.web;

import addixvn.webcrawl.db.Database;
import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import addixvn.webcrawl.util.DataReader;
import addixvn.webcrawl.util.DataWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 各求人サイトの親クラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
abstract class Web {
    
    protected final String path;
    protected final String web;
    protected final String new_all;
    protected final int item_cnt;
    protected final String s3_domain = "https://vp-web-crawl.s3-ap-northeast-1.amazonaws.com/";
    protected final HashMap selector;
    protected final Database db;
    
    protected int totalpage;
    protected ArrayList<HashMap> array_hash;
    
    /**
     * 各サイトクラスのコンストラクタ
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    Web(String path, String web, String new_all, int item_cnt) {
        DataReader input = new DataReader(web, new_all, path + "\\config\\input.csv", "Shift-JIS");
        this.selector = input.getSelector();
        this.db = new Database(path, "\\db\\vp-2.1.0.db");
        
        this.path = path;
        this.web = web;
        this.new_all = new_all;
        this.item_cnt = item_cnt;
        this.array_hash = new ArrayList<>();
    }
    
    /**
     * 各サイトウェブクロールのメインメソッド。
     */
    public void main() {
        this.getHref();
        
        this.printHref(this.path + "\\results\\hreflist.txt", "Shift-JIS");
        
        this.sendInfoForDB();
        
        String[] result_columns = {"URL", "S3URL", "Title", "CompanyName", "Workplace", "HeadOffice", "PostalCode", "Address", "Recruiter", "Phone", "Fax", "Email", "Industry", "Remarks", "PostPeriod", "Rank", "Website"};
        String result_index = "URL,S3-URL,職種名,会社名,勤務地,本社所在地,郵便番号,住所,採用担当,電話番号,FAX,E-mail,業種名,備考,掲載期間,掲載ランク,掲載サイト";
        this.print(this.path + "\\results\\jobinfo.csv", "Shift-JIS", "WebData", result_columns, result_index);
        
        String[] branch_columns = {"S3URL", "CorporateNum", "BranchNum", "Title", "CompanyName", "Workplace", "HeadOffice", "PostalCode", "Address", "Recruiter", "Phone", "Fax", "Email", "Industry", "Remarks", "PostPeriod", "Rank", "Website"};
        String branch_index = "URL,法人番号,支店番号,職種名,会社名,勤務地,本社所在地,郵便番号,住所,採用担当,電話番号,FAX,E-mail,業種名,備考,掲載期間,掲載ランク,掲載サイト";
        this.print(this.path + "\\results\\upload.csv", "Shift-JIS", "BranchData", branch_columns, branch_index, this.path + "\\db\\analyze.sql");
    }
    
    
    /** 
     * ====================================================================================
     *  求人一覧ページ内での動作（トータルページ数を取得後にその数の分だけループしてarray_hashを作成）
     * ====================================================================================
     */
    
    /**
     * トップページで何ページ分の求人一覧をクローリングするのか取得するメソッド。
     */
    protected void setTotalpage() {
        DataCollecter top = new DataCollecter(this.selector.get("url").toString());
        top.setTotalpage(this.selector.get("total").toString(), this.item_cnt);
        this.totalpage = top.getTotalpage();
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Calculating The Number Of Total Page Completed.  (" + timestamp + ")" + "\n");
    }
    
    /**
     * 求人一覧ページのウェブクロールを行うメソッド。
     * @param cnt 求人一覧ページでのページ数
     */
    protected abstract void listPageCrawl(int cnt);
    
    /**
     * 各求人情報のURLとランクを取得するメソッド。
     */
    protected void getHref() {
        System.out.println("\n********** Start Getting Href & Rank Data **********\n");
        
        this.setTotalpage();
        
        for (int i = 1; i <= this.totalpage; i++) {
            this.listPageCrawl(i);
            timestamp(i, this.totalpage);
        }
        System.out.println("Getting Href And Rank Completed.\n");
    }
    
    /**
     * S3へアップロードされる際のデータの行き先を文字列編集するメソッド。
     * @param str 編集前の文字列
     * @return S3アップロード後のURL
     */
    protected String editHrefForS3(String str) {
        str = str.replace("https://", this.s3_domain + this.web + "/") + "/index.html";
        return str;
    }
    
    /**
     * 先頭行に見出しを入れずにHrefリストを出力するメソッド。
     * @param file ファイル名
     * @param charset 文字コード
     */
    protected void printHref(String file, String charset) {
        System.out.println("\n********** Start Exporting File From Database **********\n");
        
        DataWriter dr = new DataWriter(file, charset);
        dr.printHref(this.array_hash);
        
        System.out.println("Exporting Data Completed.\n");
    }
    
    /** 
     * ========================================================================
     *  求人詳細ページ内での動作（array_hashからアクセスしてデータをデータベースへ保存）
     * ========================================================================
     */
    
    /**
     * 求人詳細ページのウェブクロールを行いリスト型データとして返すメソッド。
     * @param hashmap HashMap形式のURLとランクのリスト
     * @return 各セレクタに対応するデータのリスト
     */
    protected abstract ArrayList<String> detailPageCrawl(HashMap hashmap);
    
    /**
     * array_hrefに入っているURL全部にアクセスして要求データをDBに格納するメソッド。
     */
    protected void sendInfoForDB() {
        System.out.println("\n********** Start Importing Data To Database **********\n");
        
        int cnt = 1;
        for (HashMap hashmap : this.array_hash) {
            ArrayList<String> array = this.detailPageCrawl(hashmap);
            this.db.importDB(array);
            timestamp(cnt, this.array_hash.size());
            cnt++;
        }
        System.out.println("Importing Data To Database Completed.\n");
    }
    
    /**
     * 先頭行に見出しを入れて出力するメソッド。
     * @param file ファイル名
     * @param charset 文字コード
     * @param table データベースのテーブル名
     * @param columns データベースの列名
     * @param index 出力先頭行の見出し
     */
    protected void print(String file, String charset, String table, String[] columns, String index) {
        System.out.println("\n********** Start Exporting File From Database **********\n");
        
        DataWriter dr = new DataWriter(file, charset);
        dr.printData(this.db.exportDB(table, columns), index);
        
        System.out.println("Exporting Data Completed.\n");
    }
    
    /**
     * SQLクエリを実行してから先頭行に見出しを入れて出力するメソッド。
     * @param file ファイル名
     * @param charset 文字コード
     * @param table データベースのテーブル名
     * @param columns データベースの列名
     * @param index 出力先頭行の見出し
     * @param query_file クエリファイル名
     */
    protected void print(String file, String charset, String table, String[] columns, String index, String query_file) {
        this.db.executeQuery(query_file);
        this.print(file, charset, table, columns, index);
    }
    
    /**
     * 求人情報の各セレクターに対応するデータを取得するメソッド。
     * @param dc DataCollecterクラスのインスタンス
     * @param key HashMap(≒連想配列)のキー
     * @return セレクターで取得されたデータ
     */
    protected String getData(DataCollecter dc, String key) {
        dc.setStr(this.selector.get(key).toString());
        String str = dc.getStr();
        return str;
    }
    
    /**
     * ランクTの時にデータが取れていない(str="-")時に再度データを取得しなおすメソッド。
     * @param dc DataCollecterクラスのインスタンス
     * @param str 取得されたデータ
     * @param key HashMap(≒連想配列)のキー
     * @return 再度別セレクターで取得されたデータ
     */
    protected String getDataT(DataCollecter dc, String str, String key) {
        if (str.equals("-")) {
            dc.setStr(this.selector.get(key).toString());
            str = dc.getStr();
        }
        return str;
    }
    
    /**
     * 郵便番号を抽出するメソッド。
     * @param str 編集前の文字列
     * @return 郵便番号
     */
    protected static String editPostalCode(String str) {
        String postalcode = "-";
        str = edit(str).replace(" ", "");
        if (str.contains("〒")) {
            if (!str.contains("〒-")) {
                postalcode = str.substring(str.indexOf("〒") + 1, str.indexOf("〒") + 9);
            }
        }
        return postalcode;
    }
    
    /**
     * PhoneListクラスのデータと比較して同値の電話番号を抽出するメソッド。
     * @param str 編集前の文字列
     * @param phonelist PhoneListクラスのインスタンス
     * @return 抽出された電話番号
     */
    protected static String editPhone(String str, PhoneList phonelist) {
        str = edit(str);
        String phone = "-";
        for (String phone_tmp : phonelist.getArray()) {
            if (str.contains(phone_tmp)) {
                if (phone_tmp.startsWith("080-") || phone_tmp.startsWith("090-")) {
                    phone = str.substring(str.indexOf(phone_tmp), str.indexOf(phone_tmp) + 13);
                } else {
                    phone = str.substring(str.indexOf(phone_tmp), str.indexOf(phone_tmp) + 12);
                }
                break;
            }
        }
        return phone;
    }
    
    /**
     * データベースでの法人番号一致用に文字を変更するメソッド。
     * @param str 文字列(法人名を想定)
     * @return アルファベットを変更した法人名
     */
    protected static String changeName(String str) {
        String[] array = str.split("");
        String[] small = {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z",
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
        };
        String[] large = {
            "ａ", "ｂ", "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ", "ｊ", "ｋ", "ｌ", "ｍ", "ｎ", "ｏ", "ｐ", "ｑ", "ｒ", "ｓ", "ｔ", "ｕ", "ｗ", "ｘ", "ｙ", "ｚ",
            "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ", "Ｆ", "Ｇ", "Ｈ", "Ｉ", "Ｊ", "Ｋ", "Ｌ", "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｗ", "Ｘ", "Ｙ", "Ｚ",
            "１", "２", "３", "４", "５", "６", "７", "８", "９", "０"
        };
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            for (int i = 0; i < small.length; i++) {
                if (s.equals(small[i])) {
                    s = s.replace(small[i], large[i]);
                }
            }
            sb.append(s);
        }
        str = sb.toString();
        return str;
    }
    
    /**
     * 全ての文字列に共通する文字列編集メソッド。
     * @param str 編集前の文字列
     * @return 文字化け防止用に編集しなおした文字列
     */
    protected static String edit(String str) {
        String[] tmps = {",", "―", "－", "‐", "-", "～"};
        String[] reps = {"", "-", "-", "-", "-", "~"};
        for (int i = 0; i < tmps.length; i++) {
            str = str.replace(tmps[i], reps[i]);
        }
        return str;
    }
    
    /**
     * タイムスタンプを出力するメソッド。
     * @param i クローリングの累積回数
     * @param total 全体のクローリング数
     */
    protected static void timestamp(int i, int total) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Finished Web Crawling Page No." + i + "/" + total + " (" + timestamp + ")" + "\n");
    }
}