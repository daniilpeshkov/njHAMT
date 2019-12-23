import static org.junit.jupiter.api.Assertions.*;

public class Test {

    @org.junit.jupiter.api.Test
    void testPut() {
        HAMT<Integer, Integer> m = new HAMT<>();
        int first = 6;
        int key = 423423;
        m.put(key, first);
        assertEquals((int) m.put(key, 234), first);



        for (int i = 0; i < 10000; i++) {
            m.put((int) (Math.random() * 100000.0d), i);
        }
    }


    @org.junit.jupiter.api.Test
    void testContainsKey() {
        HAMT<String, Integer> m = new HAMT<>();

        assertFalse(m.containsKey("abc"));
        m.put("abc", 1);
        m.put("aac", 1);
        m.put("adc", 1);
        m.put("ajc", 1);
        m.put("agc", 1);
        m.put("aqc", 1);

        assertTrue(m.containsKey("abc"));
    }

    @org.junit.jupiter.api.Test
    void testGet() {
        HAMT<String, Integer> m = new HAMT<>();
        int in = 5;
        m.put("abcd", in);
        assertEquals(in, (int)m.get("abcd"));

    }

}
