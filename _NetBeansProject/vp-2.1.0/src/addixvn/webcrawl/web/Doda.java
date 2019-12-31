package addixvn.webcrawl.web;

import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Dodaのクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Doda extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Doda(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }
    
    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        ArrayList<String> ranks = new ArrayList<>();
        
        DataCollecter pages = new DataCollecter(this.selector.get("url").toString() + "-page__" + cnt);
        pages.setArray(this.selector.get("href").toString(), "href");
        for (String href : pages.getArray()) {
            hrefs.add(href.replace("/-tab__pr/", "/-tab__jd"));
        }
        pages.setArray(this.selector.get("rank").toString(), "class");
        for (String rank : pages.getArray()) {
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
        String address = this.getData(content, "address");
        String recruiter = this.getData(content, "recruiter");
        String industry = this.getData(content, "industry");
        String period = this.getData(content, "period");
        
        // データなしの時用
        workplace = this.getDataT(content, workplace, "workplace_rankT");
        address = this.getDataT(content, address, "address_rankT");
        
        ArrayList<String> array = new ArrayList<>();
        array.add(hashmap.get("href").toString());          // URL
        array.add(hashmap.get("href_s3").toString());       // S3用URL
        array.add(edit(title));                             // タイトル
        array.add(editCompanyName(company));                // 会社名
        array.add(editWorkplace(workplace));                // 勤務地
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
        array.add(hashmap.get("rank").toString());          // 掲載ランク
        array.add("Doda");                                  // 掲載サイト
        return array;
    }
    
    private static String editRank(String str) {
        String rank = "-";
        String[] html_tmps = {"modList02 typeD typeE", "modList02 typeC", "modList02 typeB", "modList02 typeA", "modList02 typeT"};
        String[] rank_tmps = {"typeD-E", "typeC", "typeB", "typeA", "typeT"};
        for (int i = 0; i < html_tmps.length; i++) {
            if (str.contains(html_tmps[i])) {
                rank = rank_tmps[i];
                break;
            }
        }
        return rank;
    }
    
    private static String editWorkplace(String str) {
        str = edit(str);
        str = str.replace("＜勤務地詳細＞ ", "");
        return str;
    }
    
    private static String editCompanyName(String str) {
        str = edit(str);
        String[] tmps = {"【", "（", "(", "＜", "「"};
        for (String tmp : tmps) {
            if (str.contains(tmp)) {
                str = str.substring(0, str.indexOf(tmp));
                break;
            }
        }
        str = changeName(str);
        return str;
    }

    private static String editAddress(String str) {
        str = edit(str);
        if (str.contains("〒")) {
            str = str.substring(str.indexOf("〒") + 10);
        }
        return str;
    }
    
    private static String editRecruiter(String str) {
        str = edit(str);
        String recruiter = "-";
        if (str.contains("採用担当：")) {
            recruiter = str.substring(str.indexOf("採用担当："));
        }
        return recruiter;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        str = str.replace("掲載予定期間：", "");
        return str;
    }
}