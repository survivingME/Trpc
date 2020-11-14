package utils.serialize;

public interface Serializer {

    /**
     *
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);

    /**
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
