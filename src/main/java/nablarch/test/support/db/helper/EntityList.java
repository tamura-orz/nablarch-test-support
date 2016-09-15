package nablarch.test.support.db.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import nablarch.core.util.FileUtil;
import nablarch.core.util.StringUtil;

/**
 * Entityのリストを取得するクラス。
 *
 * @author tani takanori
 */
class EntityList {

    /**
     * @param url ファイルのURL
     * @return ファイルにリストアップされたエンティティ
     */
    static List<String> get(String url) {
        InputStream i = null;
        BufferedReader reader = null;
        List<String> list = new ArrayList<String>();

        try {
            i = FileUtil.getResource(url);
            reader = new  BufferedReader(new InputStreamReader(i));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (StringUtil.hasValue(line.trim())) {
                    list.add(line.trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.closeQuietly(i, reader);
        }
        return list;
    }
}
