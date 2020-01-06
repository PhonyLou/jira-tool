package jiratool.util;

import java.util.Map;

public class Converter {
    public static String convertMap(Map<String, Double> data) {
        if (null == data) return "{}";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        data.forEach((key, value) -> {
            stringBuilder.append(key);
            stringBuilder.append(":");
            stringBuilder.append(value);
            stringBuilder.append(",");
        });
        stringBuilder.append("}");

        String res;
        if (stringBuilder.toString().endsWith(",}")) {
            res = stringBuilder.toString().replace(",}", "}");
        } else {
            res = stringBuilder.toString();
        }
        return res;
    }

}
