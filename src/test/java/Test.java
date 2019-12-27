import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Test {

    @org.junit.jupiter.api.Test
    void testPut() {
        HAMT<Integer, Integer> m = new HAMT<>();

        for (int i = 0; i < 10000; i++) {
            m.put((int) (Math.random() * 100000.0d), i);
        }

        int first = 6;
        int key = 423423;
        m.put(key, first);
        assertEquals((int) m.put(key, 234), first);

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

    @org.junit.jupiter.api.Test
    void testCollision() {
        HAMT<testKey, Integer> m = new HAMT<>();
        testKey k1 = new testKey(1);
        testKey k2 = new testKey(1);
        testKey k3 = new testKey(0b100001);
        m.put(k1, 1);
        m.put(k2, 2);
        m.put(k3, 3);

        assertTrue(m.containsKey(k3));
        assertEquals(3, (int)m.remove(k3));
        assertFalse(m.containsKey(k3));
    }

    @org.junit.jupiter.api.Test
    void testKeySet() {
        HAMT<String, Integer> m = new HAMT<>();
        m.put("fdewre", 1);
        m.put("fasre", 2);
        m.put("frdee", 3);
        m.put("frefw", 4);
        m.put("frefer", 5);
        Iterator<String> it = m.keySet().iterator();

        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    @org.junit.jupiter.api.Test
    void itRemove() {
        HAMT<String, Integer> m = new HAMT<>();
        m.put("fdewre", 1);
        m.put("fasre", 2);
        m.put("frdee", 3);
        m.put("frefw", 4);
        m.put("frefer", 5);
        Iterator<String> it = m.keySet().iterator();

        System.out.println("loop 1");
        while (it.hasNext()) {
            System.out.println(it.next());
            it.remove();
        }
        System.out.println("size: " + m.keySet().size());
        System.out.println("loop 2");
        it = m.keySet().iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    @org.junit.jupiter.api.Test
    void testKeySet2() {
        HAMT<testKey, Integer> m = new HAMT<>();
        testKey k1 = new testKey(1);
        testKey k2 = new testKey(1);
        testKey k3 = new testKey(0b100001);
        m.put(k1, 1);
        m.put(k2, 2);
        m.put(k3, 3);
        Iterator<testKey> it = m.keySet().iterator();

        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    @org.junit.jupiter.api.Test
    void testEntryIt() {
        HAMT<String, Integer> m = new HAMT<>();
        m.put("fdewre", 1);
        m.put("fasre", 2);
        m.put("frdee", 3);
        m.put("frefw", 4);
        m.put("frefer", 5);
        Iterator<Map.Entry<String, Integer>> it = m.entrySet().iterator();

        System.out.println("loop 1");
        while (it.hasNext()) {
            System.out.println(it.next());
            it.remove();
        }
        System.out.println("size: " + m.keySet().size());
        System.out.println("loop 2");
        it = m.entrySet().iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

}
