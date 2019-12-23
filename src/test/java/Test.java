import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
