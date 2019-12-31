package addixvn.webcrawl.web;

import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Typeのクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Type extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Type(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }

    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        ArrayList<String> ranks = new ArrayList<>();
        
        String url;
        if (this.new_all.equals("new")) {
            url = this.selector.get("url").toString().replace("p1", "p" + cnt);
        } else {
            int cnt_all = (cnt - 1) * 20;
            url = this.selector.get("url").toString() + "&offset=" + cnt_all;
        }
        
        DataCollecter pages = new DataCollecter(url);
        pages.setArray(this.selector.get("href").toString(), "href");
        for (String href : pages.getArray()) {
            href = this.selector.get("domain").toString() + href;
            href = href.replace("message/", "detail");
            href = href.replace("detail/", "detail");
            hrefs.add(href);
        }
        pages.setArray_Rank(this.selector.get("rank").toString(), 1);
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
        String industry = this.getData(content, "industry");
        String period = this.getData(content, "period");
        
        ArrayList<String> array = new ArrayList<>();
        array.add(hashmap.get("href").toString());          // URL
        array.add(hashmap.get("href_s3").toString());       // S3用URL
        array.add(edit(title));                             // タイトル
        array.add(editCompanyName(company));                // 会社名
        array.add(edit(workplace));                         // 勤務地
        array.add("-");                                     // 本社所在地
        array.add(editPostalCode(address));                 // 郵便番号
        array.add(editAddress(address));                    // 住所
        array.add(editRecruiter(address));                  // 採用担当
        array.add(editPhone(address, new PhoneList()));     // 電話
        array.add("-");                                     // FAX
        array.add("-");                                     // E-mail
        array.add(edit(industry));                          // 業種
        array.add("-");                                     // 備考
        array.add(editPeriod(period));                      // 掲載期間
        array.add(hashmap.get("rank").toString());          // 掲載ランク
        array.add("Type");                                  // 掲載サイト
        return array;
    }
    
    private static String editRank(String str) {
        String rank;
        String[] separated = str.split("<img");
        if (str.contains("_message")) {
            rank = "type-A";
        } else if (str.contains("<img") && separated.length >= 5) {
            rank = "type-B";
        } else if (str.contains("<img")) {
            rank = "type-C";
        } else {
            rank = "type-D";
        }
        return rank;
    }
    
    private static String editCompanyName(String str) {
        String[] tmps = {"（", "【", "／", "/", "≪"};
        str = edit(str);
        for (String tmp : tmps) {
            if (str.contains(tmp)) {
                str = str.substring(0, str.indexOf(tmp));
            }
        }
        if (str.startsWith("株式会社 ") || str.startsWith("株式会社　")) {
            str = str.replace("株式会社 ", "株式会社");
            str = str.replace("株式会社　", "株式会社");
        }
        if (str.contains("株式会社 ")) {
            str = str.substring(0, str.indexOf("株式会社 ") + 4);
        } else if (str.contains("株式会社　")) {
            str = str.substring(0, str.indexOf("株式会社　") + 4);
        }
        str = changeName(str);
        return str;
    }
    
    private static String editAddress(String str) {
        str = edit(str);
        if (str.contains("〒")) {
            str = str.substring(str.indexOf("〒") + 10);
        }
        if (str.contains("採用担当")) {
            str = str.substring(0, str.indexOf("採用担当"));
        }
        return str;
    }
    
    private static String editRecruiter(String str) {
        str = edit(str);
        String recruiter = "-";
        if (str.contains("採用担当")) {
            recruiter = str.substring(str.indexOf("採用担当"));
        }
        return recruiter;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        String[] tmps = str.split(" ");
        
        String month = null;
        String[] month_tmps = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] month_reps = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        for (int i = 0; i < month_tmps.length; i++) {
            if (tmps[1].equals(month_tmps[i])) {
                month = month_reps[i];
            }
        }
        
        String year = tmps[5];
        if (year.contains("残り")) {
            year = year.substring(0, year.indexOf("残り"));
        }
        
        String period = "~" + year + "." + month + "." + tmps[2];
        return period;
    }
}