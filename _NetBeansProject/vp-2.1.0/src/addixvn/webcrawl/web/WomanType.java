package addixvn.webcrawl.web;

import addixvn.webcrawl.model.PhoneList;
import addixvn.webcrawl.util.DataCollecter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 女の転職のクラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class WomanType extends Web {
    
    /**
     * トップ画面で選択したパラメータでインスタンスを生成。
     * @param path プロジェクトディレクトリ
     * @param web ウェブサイト名
     * @param new_all 新着指定の有無
     * @param item_cnt 求人一覧ページの表示上限数
     */
    public WomanType(String path, String web, String new_all, int item_cnt) {
        super(path, web, new_all, item_cnt);
    }

    @Override
    protected void listPageCrawl(int cnt) {
        ArrayList<String> hrefs = new ArrayList<>();
        
        DataCollecter pages = new DataCollecter(this.selector.get("url").toString().replace("p1", "p" + cnt));
        pages.setArray(this.selector.get("href").toString(), "href");
        for (String href : pages.getArray()) {
            href = this.selector.get("domain").toString() + href;
            hrefs.add(href);
        }
        
        // 詳細ページでランク取得するためarray_hashにはhref関連のみ
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
        array.add(editRecruiter(address));                  // 採用担当
        array.add(editPhone(address, new PhoneList()));     // 電話
        array.add("-");                                     // FAX
        array.add(editEmail(address));                      // E-mail
        array.add(edit(industry));                          // 業種
        array.add("-");                                     // 備考
        array.add(editPeriod(period));                      // 掲載期間
        array.add(editRank(rank));                          // 掲載ランク
        array.add("女の転職");                               // 掲載サイト
        return array;
    }
    
    @Override
    protected String editHrefForS3(String str) {
        str = str.replace("/?routeway=16", "");
        str = super.editHrefForS3(str);
        return str;
    }
    
    private static String editRank(String str) {
        // 判定する画像の文字列をセパレーターにして分割することで、配列の個数からランクを判定。
        String rank = "-";
        int[] html_tmps = {7, 6, 5, 1};
        String[] rank_tmps = {"Q(クイーン)", "P(プリンセス)", "R(レギュラー)", "L(ライト)"};
        String[] separated = str.split("<img src=\"/image");
        for (int i = 0; i < html_tmps.length; i++) {
            if (separated.length - 1 >= html_tmps[i]) {
                rank = rank_tmps[i];
                break;
            }
        }
        return rank;
    }
    
    private static String editCompanyName(String str) {
        String[] tmps = {"（", "【", "／", "/"};
        str = edit(str);
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
            str = str.substring(str.indexOf("〒") + 11);
        }
        if (str.contains("担当")) {
            str = str.substring(0, str.indexOf("担当"));
        }
        return str;
    }
    
    private static String editEmail(String str) {
        str = edit(str);
        String email = "-";
        if (str.contains("E-mail ／ ")) {
            email = str.substring(str.indexOf("E-mail ／ ") + 9);
        }
        return email;
    }
    
    private static String editRecruiter(String str) {
        str = edit(str);
        if (str.contains("担当者 ／ ")) {
            str = str.substring(str.indexOf("担当者 ／ ") + 6);
        }
        if (str.contains(" tel")) {
            str = str.substring(0, str.indexOf(" tel"));
        }
        if (str.contains(" E-mail")) {
            str = str.substring(0, str.indexOf(" E-mail"));
        }
        return str;
    }
    
    private static String editPeriod(String str) {
        str = edit(str);
        if (str.contains("掲載期間")) {
            str = str.substring(5);
        }
        return str;
    }
}