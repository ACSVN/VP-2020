package addixvn.webcrawl.web;

import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * マイナビ転職のクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Mynavi extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Mynavi(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }
    
    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        ArrayList<String> ranks = new ArrayList<>();
        
        DataCollecter pages = new DataCollecter(this.selector.get("url").toString().replace(this.selector.get("replace_nxt_p").toString(), "pageNum=" + cnt));
        pages.setArray(this.selector.get("href").toString(), "href");
        int href_size = pages.getArray().size();
        for (String href : pages.getArray()) {
            href = this.selector.get("domain").toString() + href.replace("/msg", "");
            hrefs.add(href);
        }
        pages.setArray_Rank(this.selector.get("rank").toString(), 32);
        for (int i = 0; i < href_size; i++) {
            // htmlでのランク取得で最終ページの最後で要らないデータを含んでしまう事情で特別に対応(=href_size)
            String rank = pages.getArray().get(i);
            ranks.add(editRank(rank));
        }
        
        for (int i = 0; i < hrefs.size(); i++) {
            HashMap<String, String> hashmap = new HashMap<>();
            hashmap.put("href", hrefs.get(i));
            hashmap.put("href_s3", this.editHrefForS3(hrefs.get(i)));
            hashmap.put("rank", ranks.get(i));
            this.array_hash.add(hashmap);
        }
    }
    
    @Override
    protected ArrayList<String> detailPageCrawl(HashMap hashmap) {
        DataCollecter content = new DataCollecter(hashmap.get("href").toString());
        String title = this.getData(content, "title");
        String company = this.getData(content, "company");
        String workplace = this.getData(content, "workplace");
        String headoffice = this.getData(content, "headoffice");
        String address = this.getData(content, "address");
        String recruiter = this.getData(content, "recruiter");
        String phone = this.getData(content, "phone");
        String email = this.getData(content, "email");
        String industry = this.getData(content, "industry");
        String remarks = this.getData(content, "remarks");
        String period = this.getData(content, "period");
        
        ArrayList<String> array = new ArrayList<>();
        array.add(hashmap.get("href").toString());      // URL
        array.add(hashmap.get("href_s3").toString());   // S3用URL
        array.add(edit(title));                         // タイトル
        array.add(editCompanyName(company));            // 会社名
        array.add(editWorkplace(workplace));            // 勤務地
        array.add(edit(headoffice));                    // 本社所在地
        array.add(editPostalCode(address));             // 郵便番号
        array.add(editAddress(address));                // 住所
        array.add(edit(recruiter));                     // 採用担当
        array.add(edit(phone));                         // 電話
        array.add("-");                                 // FAX
        array.add(edit(email));                         // E-mail
        array.add(edit(industry));                      // 業種
        array.add(edit(remarks));                       // 備考
        array.add(editPeriod(period));                  // 掲載期間
        array.add(hashmap.get("rank").toString());      // 掲載ランク
        array.add("マイナビ転職");                       // 掲載サイト
        return array;
    }
    
    @Override
    protected String editHrefForS3(String str) {
        if (str.contains("/?")) {
            str = str.substring(0, str.indexOf("/?"));
        }
        str = super.editHrefForS3(str);
        return str;
    }
    
    private static String editRank(String str) {
        String rank = "MT-D";
        String[] html_tmps = {"<img width=\"900\" height=\"150\"", "<img width=\"200\" height=\"150\"", "<img width=\"160\" height=\"120\"", "<img width=\"120\" height=\"90\""};
        String[] rank_tmps = {"MT-S", "MT-A", "MT-B", "MT-C"};
        for (int i = 0; i < html_tmps.length; i++) {
            if (str.contains(html_tmps[i])) {
                rank = rank_tmps[i];
                break;
            }
        }
        return rank;
    }
    
    private static String editCompanyName(String str) {
        String[] tmps = {" |", "|", " ｜", "｜", "／"};
        str = edit(str);
        for (String tmp : tmps) {
            if (str.contains(tmp)) {
                str = str.substring(0, str.indexOf(tmp));
                break;
            }
        }
        str = changeName(str);
        return str;
    }
    
    private static String editWorkplace(String str) {
        str = edit(str);
        str = str.replace("マイナビ転職の勤務地区分では… ", "");
        return str;
    }

    private static String editAddress(String str) {
        str = edit(str);
        str = str.replace(" 地図を見る", "");
        if (str.contains("〒")) {
            str = str.substring(str.indexOf("〒") + 11);
        }
        return str;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        str = str.replace("情報更新日：", "");
        str = str.replace(" 掲載終了予定日：", " ~ ");
        return str;
    }
}