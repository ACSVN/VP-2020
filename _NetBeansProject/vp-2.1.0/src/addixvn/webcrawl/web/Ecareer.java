package addixvn.webcrawl.web;

import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * イーキャリアのクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Ecareer extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Ecareer(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }

    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        
        DataCollecter pages = new DataCollecter(this.selector.get("url").toString() + "?window=" + cnt);
        pages.setArray(this.selector.get("href").toString(), "href");
        for (String href : pages.getArray()) {
            href = this.selector.get("domain").toString() + href;
            hrefs.add(href);
        }
        
        for (String href : hrefs) {
            HashMap<String, String> hashmap = new HashMap<>();
            hashmap.put("href", href);
            hashmap.put("href_s3", this.editHrefForS3(href));
            this.array_hash.add(hashmap);
        }
    }

    @Override
    protected ArrayList<String> detailPageCrawl(HashMap hashmap) {
        DataCollecter content = new DataCollecter(hashmap.get("href").toString());
        String title = this.getData(content, "title");
        String company = this.getData(content, "company");
        String workplace = this.getData(content, "workplace");
        String address = this.getData(content, "address");
        String recruiter = this.getData(content, "recruiter");
        String industry = this.getData(content, "industry");
        String period = this.getData(content, "period");
        String rank = content.getHtml();
        
        ArrayList<String> array = new ArrayList<>();
        array.add(hashmap.get("href").toString());          // URL
        array.add(hashmap.get("href_s3").toString());       // S3用URL
        array.add(edit(title));                             // タイトル
        array.add(editCompanyName(company));                // 会社名
        array.add(edit(workplace));                         // 勤務地
        array.add("-");                                     // 本社所在地
        array.add(editPostalCode(address));                 // 郵便番号
        array.add(editAddress(address));                    // 住所
        array.add(editRecruiter(recruiter));                // 採用担当
        array.add(editPhone(recruiter, new PhoneList()));   // 電話
        array.add("-");                                     // FAX
        array.add("-");                                     // E-mail
        array.add(edit(industry));                          // 業種
        array.add("-");                                     // 備考
        array.add(editPeriod(period));                      // 掲載期間
        array.add(editRank(rank));                          // 掲載ランク
        array.add("イーキャリア");                            // 掲載サイト
        return array;
    }
    
    private static String editRank(String str) {
        // メインイメージ、フォトエリアの写真からランク判定。
        String main_img = "width=\"1020\" height=\"415\" alt=\"\">";
        String sub_img = "width=\"280\" height=\"224\" alt=\"\">";
        
        String rank;
        if (str.contains(main_img) && str.contains(sub_img)) {
            rank = "マキシマム"; // 要件段階で"ハイエンド"との違いなし
        } else if (str.contains(main_img)) {
            rank = "スタンダード";
        } else {
            rank = "エントリー";
        }
        return rank;
    }
    
    private static String editCompanyName(String str) {
        String[] tmps = {"（", "【", "／", "/"};
        str = edit(str);
        str = str.replace(" NEW", "");
        for (String tmp : tmps) {
            if (str.contains(tmp)) {
                str = str.substring(0, str.indexOf(tmp));
            }
        }
        str = changeName(str);
        return str;
    }
    
    private static String editAddress(String str) {
        str = edit(str);
        if (str.contains("〒")) {
            if (str.contains("〒-")) {
                str = str.replace("〒-", "");
            } else {
                str = str.substring(str.indexOf("〒") + 10);
            }
        }
        if (str.contains("採用担当")) {
            str = str.substring(0, str.indexOf("採用担当"));
        }
        if (str.startsWith(" ")) {
            str = str.substring(1);
        }
        return str;
    }
    
    private static String editRecruiter(String str) {
        str = edit(str);
        str = str.replace("連絡先 ", "");
        if (str.contains("〒")) {
            str = str.substring(0, str.indexOf("〒"));
        }
        return str;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        str = str.replace("掲載終了日：", "~");
        return str;
    }
}