package me.haroldmartin.chat.util;

import java.lang.reflect.Field;
import java.util.HashMap;

import timber.log.Timber;

public class Objects {
    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    // Fake object downcast by using reflection to set public fields
    public static class Down<T> {
        private final Class<T> clazz;

        public Down(Class<T> clazz) {
            this.clazz = clazz;
        }

        public T cast(Object o) throws IllegalAccessException, InstantiationException {
            T t = clazz.newInstance();
            Field[] newFields = clazz.getFields();
            HashMap<String, Field> newFieldMap = new HashMap<String, Field>();
            for (int i = 0; i < newFields.length; i++) {
                Field field = newFields[i];
                newFieldMap.put(field.getName(), field);
                Timber.e("Found new : " + field.getName());
            }

            Field[] oldFields = o.getClass().getFields();
            for (int i = 0; i < oldFields.length; i++) {
                Field field = oldFields[i];
                String fieldName = field.getName();
                if (newFieldMap.containsKey(fieldName)) {
                    Timber.e("Setting : " + fieldName);
                    Field newField = newFieldMap.get(fieldName);
                    newField.set(t, field.get(o));
                } else {
                    Timber.e("Dropping : " + fieldName);
                }
            }

            return t;
        }
    }
}
