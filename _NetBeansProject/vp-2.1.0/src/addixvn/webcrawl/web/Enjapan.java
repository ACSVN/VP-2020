package addixvn.webcrawl.web;

import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * エン転職のクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Enjapan extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Enjapan(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }
    
    @Override
    protected void setTotalpage() {
        super.setTotalpage();
        // 新着指定クロール時に最終ページが表示されないため、totalpageを1減らす
        if (this.new_all.equals("new")) {
            this.totalpage--;
        }
    }

    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        ArrayList<String> ranks = new ArrayList<>();
        
        DataCollecter pages = new DataCollecter(this.selector.get("url").toString() + "&pagenum=" + cnt);
        pages.setArray(this.selector.get("href").toString(), "href");
        int href_size = pages.getArray().size();
        for (String href : pages.getArray()) {
            href = this.selector.get("domain").toString() + href.replace("/msg", "");
            hrefs.add(href);
        }
        pages.setArray_Rank(this.selector.get("rank").toString(), 1);
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
        String address = this.getData(content, "address");
        String recruiter = this.getData(content, "recruiter");
        String phone = this.getData(content, "phone");
        String fax = this.getData(content, "fax");
        String email = this.getData(content, "email");
        String industry = this.getData(content, "industry");
        String period = this.getData(content, "period");
        
        ArrayList<String> array = new ArrayList<>();
        array.add(hashmap.get("href").toString());      // URL
        array.add(hashmap.get("href_s3").toString());   // S3用URL
        array.add(edit(title));                         // タイトル
        array.add(editCompanyName(company));            // 会社名
        array.add(edit(workplace));                     // 勤務地
        array.add("-");                                 // 本社所在地
        array.add(editPostalCode(address));             // 郵便番号
        array.add(editAddress(address));                // 住所
        array.add(edit(recruiter));                     // 採用担当
        array.add(edit(phone));                         // 電話
        array.add(edit(fax));                           // FAX
        array.add(edit(email));                         // E-mail
        array.add(edit(industry));                      // 業種
        array.add("-");                                 // 備考
        array.add(editPeriod(period));                  // 掲載期間
        array.add(hashmap.get("rank").toString());      // 掲載ランク
        array.add("エン転職");                           // 掲載サイト
        return array;
    }
    
    private static String editRank(String str) {
        String rank;
        if (str.contains("<img")) {
            rank = "S-A";
        } else {
            rank = "B-C-D";
        }
        return rank;
    }
    
    private static String editCompanyName(String str) {
        str = edit(str);
        String[] tmps = {"（"};
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
        if (str.contains("担当")) {
            str = str.substring(0, str.indexOf("担当"));
        }
        return str;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        str = str.replace("掲載期間", "");
        return str;
    }
}