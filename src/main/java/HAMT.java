import java.lang.reflect.Array;
import java.util.*;

public class HAMT<K, V> implements Map<K, V> {

    private static final int NODE_TABLE_SIZE = 5;

    private static final int MAX_HEIGHT = (Integer.SIZE / NODE_TABLE_SIZE) + 1;

    private int size = 0;

    private enum NodeType{KV_NODE, TABLE_NODE, NODE_LIST_NODE}

    private class KVPair {
        K k;
        V v;

        public KVPair(K k, V v) {
            this.k = k;
            this.v = v;
        }

        @Override
        protected Object clone() {
            return new KVPair(k ,v);
        }
    }

    private Node root = new Node(0);

    private class Node {

        @Override
        public String toString() {
            return "Node{" +
                    "k=" + kv.k +
                    ", v=" + kv.v +
                    '}';
        }

        NodeType nodeType;
        KVPair kv;
        int hash;

        Node[] table;
        List<KVPair> entries;

        int height;

        Node makeTableNode() {
            kv = null;
            entries = null;
            initTable();
            nodeType = NodeType.TABLE_NODE;
            return this;
        }

        Node makeKVNode(KVPair kv, int hash) {
            table = null;
            entries = null;
            this.kv = kv;
            this.hash = hash;
            nodeType = NodeType.KV_NODE;
            return this;
        }

        Node makeNodeListNode() {
            table = null;
            nodeType = NodeType.NODE_LIST_NODE;
            entries = new LinkedList<>();
            if ( kv != null) {
                entries.add(kv);
            }
            kv = null;
            return this;
        }

        Node(int height) {
            this.height = height;
            if (this.height == 0) {
                makeTableNode();
            }
        }

        @SuppressWarnings("unchecked")
        private void initTable() {
            table = (Node[])Array.newInstance(Node.class, (int) Math.pow(2,NODE_TABLE_SIZE));
        }
    }

    private static int lowNBits(int n, int nBits) {
        return n & ((1 << nBits) - 1);
    }

    private Node getNodeOrClosest(K key, int hash) {
        Node curNode = root;
        Node prevNode = null;

        int tmpHash = hash;

        while (  curNode.nodeType == NodeType.TABLE_NODE
                && curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)] != null) {
            prevNode = curNode;
            curNode = curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)];
            tmpHash >>= NODE_TABLE_SIZE;
        }

        if (prevNode == null) return root;

        if(curNode.nodeType == NodeType.NODE_LIST_NODE) {
            if ( hash == curNode.hash) {
                return curNode;
            } else return prevNode;
        } else if (curNode.nodeType == NodeType.KV_NODE) {
            if (curNode.kv == null) {
                prevNode.table[lowNBits(curNode.hash >> (prevNode.height), NODE_TABLE_SIZE)] = null;
                return prevNode;
            } else if ( curNode.hash == hash && curNode.kv.k.equals(key) ) {
                return curNode;
            } else {
                return prevNode;
            }
        } else {
            return curNode;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();

        int hash = key.hashCode();
        Node curNode = getNodeOrClosest(key, hash);

        switch (curNode.nodeType) {
            case TABLE_NODE:
                int i = lowNBits(hash >> (curNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE);
                if (curNode.table[i] == null) {
                    Node tmp = new Node(curNode.height + 1).makeKVNode(new KVPair(key, value), hash);
                    curNode.table[i] = tmp;
                } else {
                    Node nextNode = curNode.table[i];
                    if (hash == nextNode.hash) {
                        nextNode.makeNodeListNode();
                        nextNode.entries.add(new KVPair(key, value));
                    } else {
                        Node tmp = new Node(nextNode.height + 1);
                        tmp.kv = nextNode.kv;
                        tmp.entries = nextNode.entries;
                        tmp.hash = nextNode.hash;
                        tmp.nodeType = nextNode.nodeType;
                        nextNode.makeTableNode();
                        Node newKv = new Node(nextNode.height + 1);
                        newKv.makeKVNode(new KVPair(key, value), hash);
                        nextNode.table[lowNBits(hash >>(nextNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE)] = newKv;
                        nextNode.table[lowNBits(tmp.hash >>(nextNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE)] = tmp;
                    }
                }
                size++;
                return null;
            case KV_NODE:
                V prevValue = curNode.kv.v;
                curNode.kv.v = value;
                return prevValue;
            case NODE_LIST_NODE:
                for (int j = 0; j < curNode.entries.size(); j++) {
                    if (curNode.entries.get(j).k.equals(key)) {
                        prevValue = curNode.entries.get(j).v;
                        curNode.entries.get(j).v = value;
                        return prevValue;
                    }
                }
                curNode.entries.add(new KVPair(key, value));
                size++;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int hash = ((K)key).hashCode();
        Node tmpNode = getNodeOrClosest((K)key, hash);
        switch (tmpNode.nodeType) {
            case KV_NODE:
                return true;
            case NODE_LIST_NODE:
                Iterator<KVPair> it = tmpNode.entries.iterator();
                KVPair curKv;
                while (it.hasNext()) {
                    curKv = it.next();
                    if (curKv.k.equals((K)key)) {
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        if ( key == null ) throw new NullPointerException();
        int hash = key.hashCode();
        Node node = getNodeOrClosest((K)key, hash);

        return node.nodeType == NodeType.KV_NODE ? node.kv.v : null;
    }

    @Override
    public V remove(Object key) {
        int hash = ((K)key).hashCode();
        Node rmNode = getNodeOrClosest((K)key, hash);

        switch (rmNode.nodeType) {
            case NODE_LIST_NODE:
                Iterator<KVPair> it = rmNode.entries.iterator();
                KVPair curKv;
                while (it.hasNext()) {
                    curKv = it.next();
                    if (curKv.k.equals((K)key)) {
                        it.remove();
                        size--;
                        return curKv.v;
                    }
                }
                break;
            case KV_NODE:
                V rmVal = rmNode.kv.v;
                rmNode.kv = null;
                size--;
                return rmVal;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Set<? extends Entry<? extends K, ? extends V>> entries = m.entrySet();
        for (Entry<? extends K, ? extends V> entry : entries) {
           put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        size = 0;
        root = new Node(0);
    }

    @Override
    public Set<K> keySet() {
        return new Set<K>() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return HAMT.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return HAMT.this.containsKey(o);
            }

            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    @Override
                    public boolean hasNext() {
                        testFirst();
                        cashedNext = next();

                        return cashedNext != null;
                    }

                    private Deque<Node> nodeStack           = new LinkedList<>();
                    private Deque<Integer> outgoingStack    = new LinkedList<>();
                    private boolean first                           = true;
                    private K cashedNext;
                    private K lastReturned;

                    private void testFirst() {
                        if ( first ) {
                            first = false;
                            nodeStack.push(root);
                            outgoingStack.push(0);
                        }
                    }

                    @Override
                    public K next() {
                        testFirst();

                        if (cashedNext == null) {
                            while (!nodeStack.isEmpty()) {
                                if (nodeStack.peek().nodeType == NodeType.KV_NODE) {
                                    if ( nodeStack.peek().kv != null ) {
                                        lastReturned = nodeStack.peek().kv.k;
                                        nodeStack.pop();
                                        outgoingStack.pop();
                                        return lastReturned;
                                    } else {
                                        nodeStack.pop();
                                        outgoingStack.pop();
                                    }
                                } else if (nodeStack.peek().nodeType == NodeType.NODE_LIST_NODE) {
                                    int i = outgoingStack.peek();
                                    if (i < nodeStack.peek().entries.size()) {
                                        lastReturned = nodeStack.peek().entries.get(i).k;
                                        outgoingStack.pop();
                                        outgoingStack.push(++i);
                                        return lastReturned;
                                    } else {
                                        nodeStack.pop();
                                        outgoingStack.pop();
                                    }
                                } else if (nodeStack.peek().nodeType == NodeType.TABLE_NODE) {
                                    int i = outgoingStack.peek();
                                    if (i < nodeStack.peek().table.length) {

                                        while (i < nodeStack.peek().table.length && nodeStack.peek().table[i] == null) {
                                            i++;
                                        }
                                        if (i < nodeStack.peek().table.length) {
                                            nodeStack.push(nodeStack.peek().table[i]);
                                            outgoingStack.pop();
                                            outgoingStack.push(++i);
                                            outgoingStack.push(0);
                                        } else {
                                            nodeStack.pop();
                                            outgoingStack.pop();
                                        }
                                    } else {
                                        nodeStack.pop();
                                        outgoingStack.pop();
                                    }
                                }
                            }
                        } else {
                            K tmp = cashedNext;
                            cashedNext = null;
                            return tmp;
                        }
                        return null;
                    }

                    @Override
                    public void remove() {
                        if (lastReturned != null) {
                            HAMT.this.remove(lastReturned);
                            lastReturned = null;
                        } else throw new IllegalStateException();
                    }
                };
            }

            @Override
            public Object[] toArray() {
                Object[] tmp = new Object[size];
                int i =0;
                for (K k : this) {
                    tmp[i] = k;
                    i++;
                }
                return tmp;
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(K k) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                return HAMT.this.remove(o) != null;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                if ( c == null ) throw new NullPointerException();
                Iterator<K> it = (Iterator<K>) c.iterator();
                while (it.hasNext()) {
                    if (! HAMT.this.containsKey(it.next()) ) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends K> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                boolean changed = false;
                Iterator<K> it = iterator();
                while ( it.hasNext() ) {
                    K tmp = it.next();
                    if (!c.contains(tmp)) {
                        it.remove();
                        changed = true;
                    }
                }
                return changed;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                Iterator<K> it = (Iterator<K>) c.iterator();
                boolean changed = false;
                while (it.hasNext()) {
                    if (remove(it.next())) {
                        changed = true;
                    }
                }
                return changed;
            }

            @Override
            public void clear() {
                HAMT.this.clear();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new Collection<V>() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return HAMT.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<V> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(V v) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new Set<Entry<K, V>>() {
            @Override
            public int size() {
                return HAMT.this.size;
            }

            @Override
            public boolean isEmpty() {
                return HAMT.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return HAMT.this.get(((Entry<K,V>)o).getKey()).equals(((Entry<K,V>)o).getValue());
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    private Iterator<K> it = keySet().iterator();

                    private K lastReturned;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        K k = it.next();
                        V v = HAMT.this.get(k);
                        lastReturned = k;
                        return new AbstractMap.SimpleEntry<K, V>(k, v);
                    }

                    @Override
                    public void remove() {
                        if ( lastReturned == null ) throw new IllegalStateException();
                        else {
                            HAMT.this.remove(lastReturned);
                            lastReturned = null;
                        }
                    }
                };
            }

            @Override
            public Object[] toArray() {
                Object[] tmp = new Object[size];
                int i =0;
                for (Entry<K, V> k : this) {
                    tmp[i] = k;
                    i++;
                }
                return tmp;
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(Entry<K, V> kvEntry) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                Entry<K, V> e = ((Entry<K, V>)o);
                int hash = e.getKey().hashCode();
                Node node = getNodeOrClosest(e.getKey(), hash);

                switch (node.nodeType) {
                    case KV_NODE:
                        if (e.getKey().equals(node.kv.k) && e.getValue().equals(node.kv.v)) {
                            HAMT.this.remove(e.getKey());
                            return true;
                        }
                        break;
                    case NODE_LIST_NODE:
                        Iterator<KVPair> it = node.entries.iterator();
                        while ( it.hasNext() ) {
                            KVPair kv = it.next();
                            if (e.getKey().equals(kv.k) && e.getValue().equals(kv.v)) {
                                HAMT.this.remove(e.getKey());
                                return true;
                            }
                        }
                }

                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean containsAll(Collection<?> c) {
                if ( c == null ) throw new NullPointerException();
                Iterator<Entry<K, V>> it = (Iterator<Entry<K, V>>) c.iterator();
                while (it.hasNext()) {
                    if (! HAMT.this.containsKey(it.next()) ) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends Entry<K, V>> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                boolean changed = false;
                Iterator<Entry<K, V>> it = iterator();
                while ( it.hasNext() ) {
                    Entry<K, V> tmp = it.next();
                    if (!c.contains(tmp)) {
                        it.remove();
                        changed = true;
                    }
                }
                return changed;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                if (c == null) throw new NullPointerException();
                Iterator<Entry<K, V>> it = (Iterator<Entry<K, V>>)c.iterator();
                boolean changed = false;
                while ( it.hasNext() ) {
                    if (remove(it.next())) {
                        changed = true;
                    }
                }
                return changed;
            }

            @Override
            public void clear() {
                HAMT.this.clear();
            }
        };
    }
}
