package simulation;

import java.util.Arrays;

public class ArraysFillEx {

    public static void fill(Object array, Object value) {

        // 第一引数が配列か判定
        Class<?> type = array.getClass();
        if (!type.isArray()) {
            throw new IllegalArgumentException("not array");
        }

        // クラスの型を判定
        String arrayClassName = array.getClass().getSimpleName()
                .replace("[]", "")
                .toLowerCase();
        String valueClassName = value.getClass().getSimpleName()
                .toLowerCase()
                .replace("character", "char")
                .replace("integer", "int");
        if (!arrayClassName.equals(valueClassName)) {
            throw new IllegalArgumentException("does not match");
        }

        // 処理
        if (type.getComponentType().isArray()) {
            for (Object o : (Object[])array) {
                fill(o, value);
            }
        } else if (array instanceof boolean[]) {
                Arrays.fill((boolean[])array, (boolean)value);
        } else if (array instanceof char[]) {
                Arrays.fill((char[])array, (char)value);
        } else if (array instanceof byte[]) {
            Arrays.fill((byte[])array, (byte)value);
        } else if (array instanceof short[]) {
            Arrays.fill((short[])array, (short)value);
        } else if (array instanceof int[]) {
            Arrays.fill((int[])array, (int)value);
        } else if (array instanceof long[]) {
            Arrays.fill((long[])array, (long)value);
        } else if (array instanceof float[]) {
            Arrays.fill((float[])array, (float)value);
        } else if (array instanceof double[]) {
            Arrays.fill((double[])array, (double)value);
        } else {
            Arrays.fill((Object[])array, value);
        }
    }
}

