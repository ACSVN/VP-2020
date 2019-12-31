package addixvn.webcrawl.model;

import java.util.ArrayList;

/**
 * 電話番号一覧取得クラス
 * @author Akira Nose (ADDIX VIETNAM Co., Ltd)
 * @version 2.1.0
 */
public class PhoneList {
    
    private final ArrayList<String> array;
    
    /**
     * 電話番号テンプレートが同時に生成されるコンストラクタ。
     */
    public PhoneList() {
        ArrayList<String> array = new ArrayList<>();
        StringBuilder sb;
        String s;
        //　先頭2桁
        for (int first = 1; first < 10; first++) {
            for (int second = 1; second <= 9999; second++) {
                sb = new StringBuilder();
                sb.append("0").append(first).append("-");
                if (second < 10) {
                    sb.append("00");
                } else if (second < 100){
                    sb.append("0");
                }
                sb.append(second).append("-");
                s = sb.toString();
                array.add(s);
            }
        }
        // 先頭3桁
        for (int first = 10; first < 100; first++) {
            for (int second = 1; second <= 999; second++) {
                sb = new StringBuilder();
                sb.append("0").append(first).append("-");
                if (second < 10) {
                    sb.append("00");
                } else if (second < 100){
                    sb.append("0");
                }
                sb.append(second).append("-");
                s = sb.toString();
                array.add(s);
            }
        }
        // 先頭4桁
        for (int first = 100; first < 1000; first++) {
            for (int second = 1; second <= 999; second++) {
                sb = new StringBuilder();
                sb.append("0").append(first).append("-");
                if (second < 10) {
                    sb.append("0");
                }
                sb.append(second).append("-");
                s = sb.toString();
                array.add(s);
            }
        }
        // 携帯
        for (int first = 8; first <= 9; first++) {
            for (int second = 1; second <= 9999; second++) {
                sb = new StringBuilder();
                sb.append("0").append(first).append("0").append("-");
                if (second < 10) {
                    sb.append("000");
                } else if (second < 100) {
                    sb.append("00");
                } else if (second < 1000) {
                    sb.append("0");
                }
                sb.append(second).append("-");
                s = sb.toString();
                array.add(s);
            }
        }
        this.array = array;
    }
    
    /**
     * 電話番号一覧テンプレートを返すメソッド。
     * @return リスト型の電話番号テンプレ
     */
    public ArrayList<String> getArray() {
        return this.array;
    }
}
