package cn.daycode.core.orm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jl on 17-7-6.
 */
public class SQLParser {

    private String table;

    public SQLParser(String table) {
        this.table = table;
    }

    public String parse(Method method) {
        String methodName = method.getName();
        String sql = null;
        if (methodName.startsWith("find")) {
//            sql = parseFindQuery(methodName);
        } else if (methodName.startsWith("count")) {
//            sql = parseCountQuery(methodName);
        }
        return sql;
    }

    private String parseCountQuery(String methodName) {
        return null;
    }

    /**
     * 解析查询语句
     *
     * @param words
     * @return
     */
    private String parseFindQuery(List<String> words) {
        StringBuilder sql = new StringBuilder();
        int limit = 0;
        //findFirstByAppId

        for (String word : words) {
            if ("find".equals(word)) {
                sql.append("SELECT * FROM ")
                        .append(table);
            }

            if (word.equals("First")) {
                limit = 1;
            } else if (word.startsWith("Top")) {
                limit = Integer.parseInt(word.replace("Top", ""));
            }

            //WHERE
            boolean hasWhere = false;
            if ("By".equals(word)) {
                sql.append(" WHERE ");
                hasWhere = true;
            }

            if (hasWhere) {

            }

        }

        return null;


    }

    private List<String> parseMethodName(String methodName) {
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (char c : methodName.toCharArray()) {
            if (c > 65 && c < 90) {
                words.add(word.toString());
                word = new StringBuilder();
                word.append(c);
            } else {
                word.append(c);
            }
        }
        return words;
    }


}
