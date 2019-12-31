package addixvn.webcrawl.web;

import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import addixvn.webcrawl.util.DataReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * リクナビNEXTのクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class Rikunabi extends Web {
    
    private String url_r;
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public Rikunabi(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }
    
    @Override
    protected void setTotalpage() {
        DataCollecter top = new DataCollecter(this.url_r + "1.html");
        top.setTotalpage(this.selector.get("total").toString(), this.item_cnt);
        this.totalpage = top.getTotalpage();
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("Top Page URL: " + this.url_r + "1.html");
        System.out.println("Total Page Count of This Website: " + this.totalpage + " (" + timestamp + ")" + "\n");
    }
    
    @Override
    protected void getHref() {
        // リクナビNEXT全件取得のみ上限5000件ルールにより特別に変更
        if (this.new_all.equals("all")) {
            DataReader dr = new DataReader(this.path + "\\config\\rikunabi-URLlist.csv", "Shift-JIS");
            ArrayList<String> urllist = dr.getArray();
            for (String url : urllist) {
                this.url_r = url;
                super.getHref();
            }
        } else {
            this.url_r = this.selector.get("url").toString();
            super.getHref();
        }
    }
    
    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        ArrayList<String> ranks = new ArrayList<>();
        
        int num = 50 * (cnt - 1) + 1;
        DataCollecter pages = new DataCollecter(this.url_r + num + ".html");
        pages.setArray(this.selector.get("href").toString(), "href");
        for (String href : pages.getArray()) {
            hrefs.add(this.selector.get("domain").toString() + href.replace("/nx1", "/nx2"));
        }
        pages.setArray(this.selector.get("rank").toString(), "data-ntype");
        for (String rank : pages.getArray()) {
            ranks.add(rank);
        }
        
        for (int i = 0; i < hrefs.size(); i++) {
            HashMap<String, String> hashmap = new HashMap<>();
            hashmap.put("href", hrefs.get(i));
            hashmap.put("href_s3", this.editHrefForS3(hrefs.get(i)));
            hashmap.put("rank", ranks.get(i));
            if (!hashmap.get("rank").equals("t")) {
                this.array_hash.add(hashmap);
            }
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
        PhoneList phonelist = new PhoneList();
        array.add(hashmap.get("href").toString());              // URL
        array.add(hashmap.get("href_s3").toString());           // S3用URL
        array.add(edit(title));                                 // タイトル
        array.add(editCompanyName(company));                    // 会社名
        array.add(editWorkplace(workplace));                    // 勤務地
        array.add("-");                                         // 本社所在地
        array.add(editPostalCode(address));                     // 郵便番号
        array.add(editAddress(address, company, phonelist));    // 住所
        array.add(editRecruiter(address));                      // 採用担当
        array.add(editPhone(address, phonelist));               // 電話番号
        array.add("-");                                         // FAX
        array.add("-");                                         // E-mail
        array.add(edit(industry));                              // 業種
        array.add("-");                                         // 備考
        array.add(editPeriod(period));                          // 掲載期間
        array.add(hashmap.get("rank").toString());              // 掲載ランク
        array.add("リクナビNEXT");                               // 掲載サイト
        return array;
    }
    
    @Override
    protected String editHrefForS3(String str) {
        if (str.contains("cmi")) {
            str = str.substring(str.indexOf("cmi"));
        }
        if (str.contains("/?")) {
            str = str.substring(0, str.indexOf("/?"));
        }
        str = str.replace("/", "_");
        str = str.replace("_nx2_", "_nx1_");
        str = this.s3_domain + this.web + "/" + str + ".png";
        return str;
    }

    private static String editWorkplace(String str) {
        str = edit(str);
        if (str.contains("リクナビＮＥＸＴ上の地域分類では…… ")) {
            str = str.substring(str.indexOf("リクナビＮＥＸＴ上の地域分類では…… "));
            str = str.replace("リクナビＮＥＸＴ上の地域分類では…… ", "");
        }
        if (str.contains(" 【")) {
            str = str.substring(0, str.indexOf(" 【"));
        }
        return str;
    }
    
    private static String editCompanyName(String str) {
        str = edit(str);
        str = changeName(str);
        return str;
    }

    private static String editAddress(String str, String companyName, PhoneList phonelist) {
        str = edit(str);
        String[] rep_tmps = {companyName, "（ホームページ）", "【本社】"};
        String[] start_tmps = {" ", "　"};
        if (str.contains("〒")) {
            str = str.substring(str.indexOf("〒") + 10);
        }
        for (String tmp : rep_tmps) {
            str = str.replace(tmp, "");
        }
        for (String phone_tmp : phonelist.getArray()) {
            if (str.contains(phone_tmp)) {
                str = str.substring(0, str.indexOf(phone_tmp));
                break;
            }
        }
        if (str.contains("、【その他事業所】")) {
            str = str.substring(0, str.indexOf("、【その他事業所】"));
        }
        for (String tmp : start_tmps) {
            if (str.startsWith(tmp)) {
                str = str.substring(1);
            }
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
        str = str.replace("掲載期間：", "");
        return str;
    }
}