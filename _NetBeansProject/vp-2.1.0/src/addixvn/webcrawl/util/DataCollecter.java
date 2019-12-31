package addixvn.webcrawl.util;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 任意のURLにJsoupを用いてアクセスしてデータを取得するクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class DataCollecter {
    
    private final Document doc;
    private int totalpage;
    private String str;
    private ArrayList<String> array;
    
    /**
     * インスタンス生成時に指定URLへアクセスし同時にhtml取得を行うコンストラクタ。
     * @param url ウェブクロール対象URL
     */
    public DataCollecter(String url) {
        Document doc;
        // JsoupでDocumentを取得できない場合があるので取得できるまで無限ループ
        while (true) {
            doc = null;
            try {
                doc = Jsoup.connect(url).get();
                System.out.println("Jsoup Connection: Succeeded");
                break;
            } catch (IOException ex) {
                System.out.println("Jsoup Connection: Failed");
            }
        }
        this.doc = doc;
    }
    
    /**
     * トータルページ数を計算するメソッド。
     * @param selector セレクター
     * @param itemsPerPage 求人一覧1ページ当たりの求人情報数(=基本は50件)
     */
    public void setTotalpage(String selector, int itemsPerPage) {
        String str = this.doc.select(selector).get(0).text();
        int cnt = Integer.parseInt(str);
        if (cnt % itemsPerPage == 0) {
            this.totalpage = cnt / itemsPerPage;
        } else {
            this.totalpage = cnt / itemsPerPage + 1;
        }
    }
    
    /**
     * URLやランクのように一覧から50件引っ張ってくるような場合に使用するメソッド。
     * @param selector セレクター
     * @param method 任意要素の中の属性
     */
    public void setArray(String selector, String method) {
        ArrayList<String> array = new ArrayList<>();
        for (Element element : this.doc.select(selector)) {
            array.add(element.attr(method));
        }
        this.array = array;
    }
    
    /**
     * マイナビ、エン転職のランク取得だけ特別なメソッドで対応。
     * @param selector セレクター
     * @param firstNum html分析によるランク取得でのセレクタの最初の番号
     */
    public void setArray_Rank(String selector, int firstNum) {
        String selector_tmp = selector;
        ArrayList<String> array = new ArrayList<>();
        for (int i = firstNum; i < firstNum + 50; i++) {
            String num = String.valueOf(i);
            selector = selector_tmp.replace("NUM", num);
            for (Element element : this.doc.select(selector)) {
                array.add(element.toString());
            }
        }
        this.array = array;
    }
    
    /**
     * 文字列1つのみ情報を取得する際に用いるメソッド。
     * @param selector セレクター
     */
    public void setStr(String selector) {
        String str = this.doc.select(selector).text();
        if (!str.equals("")) {
            this.str = str;
        } else {
            this.str = "-";
        }
    }
    
    /**
     * 指定されたページのhtmlを文字列として返すメソッド。
     * @return ページのhtml
     */
    public String getHtml() {
        return this.doc.toString();
    }
    
    /**
     * 求人一覧ページ数のゲッター。
     * @return 求人一覧のページ数
     */
    public int getTotalpage() {
        return this.totalpage;
    }
    
    /**
     * リスト型データを返すゲッター。
     * @return 取得されたリスト型データ
     */
    public ArrayList<String> getArray() {
        return this.array;
    }
    
    /**
     * 文字列データを返すゲッター。
     * @return 取得された文字列データ
     */
    public String getStr() {
        return this.str;
    }
}