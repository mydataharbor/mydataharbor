/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


package mydataharbor.plugin.base.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(dateFormat);
        objMapper.registerModule(new JavaTimeModule());
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    public static <T> byte[] serialize(T obj) {
        byte[] bytes = new byte[0];
        try {
            bytes = objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return bytes;
    }

    public static <T> T deserialize(byte[] data, Class<T> cls) {
        T obj = null;
        try {
            obj = objMapper.readValue(data, cls);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <type> type jsonToObject(String json, Class<type> cls) {
        if (cls.equals(String.class)) {
            return cls.cast(json);
        } else if (cls.equals(Byte.class))
            return cls.cast(Byte.valueOf(json));
        else if (cls.equals(Short.class))
            return cls.cast(Short.valueOf(json));
        else if (cls.equals(Integer.class))
            return cls.cast(Integer.valueOf(json));
        else if (cls.equals(Long.class))
            return cls.cast(Long.valueOf(json));
        else if (cls.equals(Float.class))
            return cls.cast(Float.valueOf(json));
        else if (cls.equals(Double.class))
            return cls.cast(Double.valueOf(json));
        else if (cls.equals(Boolean.class))
            return cls.cast(Boolean.valueOf(json));
        else if (cls.equals(Character.class))
            return cls.cast(json.toCharArray()[0]);
        type obj = null;
        JavaType javaType = objMapper.getTypeFactory().constructType(cls);
        try {
            obj = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <type> type jsonToObjectList(String json,
                                               Class<?> collectionClass, Class<?>... elementClass) {
        type obj = null;
        JavaType javaType = objMapper.getTypeFactory().constructParametricType(
                collectionClass, elementClass);
        try {
            obj = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <type> type jsonToObjectHashMap(String json,
                                                  Class<?> keyClass, Class<?> valueClass) {
        type obj = null;
        JavaType javaType = objMapper.getTypeFactory().constructParametricType(HashMap.class, keyClass, valueClass);
        try {
            obj = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static String objectToJson(Object o) {
        String json = "";
        try {
            json = objMapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return json;
    }
}