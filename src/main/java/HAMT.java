import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class HAMT<K, V> implements Map<K, V> {

    public static final int NODE_TABLE_SIZE = 5;

    private int size = 0;

    private enum NodeType{KV_NODE, TABLE_NODE}

    private Node root = new Node();

    private class Node implements Cloneable {

        @Override
        public String toString() {
            return "Node{" +
                    "k=" + k +
                    ", v=" + v +
                    '}';
        }

        NodeType nodeType;
        K k;
        int hash;
        V v;
        Node[] table;
        int height;

        Node(K key, V value) {
            k = key;
            v = value;
            hash = k.hashCode();
            nodeType = NodeType.KV_NODE;
        }

        Node(K key, V value, int height) {
           k = key;
           this.height = height;
           v = value;
           hash = k.hashCode();
           nodeType = NodeType.KV_NODE;
        }

        Node(int hash, K key, V value) {
            k = key;
            v = value;
            this.hash = hash;
            nodeType = NodeType.KV_NODE;
        }

        Node(K key, V value, int height, int hash) {
            k = key;
            v = value;
            this.height = height;
            this.hash = hash;
            nodeType = NodeType.KV_NODE;
        }

        Node() {
            initTable();
            height = 0;
            nodeType = NodeType.TABLE_NODE;
        }

        //adds node using height'th NODE_TABLE_SIZE bits
        void addNode(Node node) {
            if (nodeType == NodeType.TABLE_NODE) {
                int i = lowNBits(node.hash >> (height * NODE_TABLE_SIZE), NODE_TABLE_SIZE);
                table[i] = node;
                node.height = height + 1;
            }
        }

        @SuppressWarnings("unchecked")
        private void initTable() {
            table = (Node[])Array.newInstance(Node.class, (int) Math.pow(2,NODE_TABLE_SIZE));
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new Node(k,v,height, hash);
        }

        void makeNodeTable() {
            k = null;
            v = null;
            nodeType = NodeType.TABLE_NODE;
            initTable();
        }
    }

    private int lowNBits(int n, int nBits) {
        return n & ((1 << nBits) - 1);
    }

    private Node getNodeOrClosest(K key, int hash) {
        Node curNode = root;
        Node prevNode = null;

        int tmpHash = hash;

        while (curNode.nodeType == NodeType.TABLE_NODE && curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)] != null) {
            prevNode = curNode;
            curNode = curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)];
            tmpHash >>= NODE_TABLE_SIZE;
        }

        if(prevNode == null) return root;

        if (curNode.nodeType == NodeType.TABLE_NODE) return curNode;
        else {
            if ( hash == curNode.hash && curNode.k.equals(key)) return curNode;
            else return prevNode;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        if ( key == null || value == null ) throw new NullPointerException();

        int hash = key.hashCode();
        Node curNode = getNodeOrClosest(key, hash);

        if ( curNode.nodeType == NodeType.TABLE_NODE ) {
            if ( curNode.table[lowNBits(hash >> (curNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE)] == null ) {
                curNode.table[lowNBits(hash >> (curNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE)] = new Node(key, value, curNode.height + 1, hash);
            } else {
                try {
                    Node nextNode = curNode.table[lowNBits(hash >> (curNode.height * NODE_TABLE_SIZE), NODE_TABLE_SIZE)];
                    Node tmpNode = (Node) nextNode.clone();
                    nextNode.makeNodeTable();
                    nextNode.addNode(tmpNode);
                    nextNode.addNode(new Node(hash, key, value));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            size++;
            return null;
        } else if ( curNode.nodeType == NodeType.KV_NODE ) {
            V prevValue = curNode.v;
            curNode.v = value;
            return prevValue;
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
        return get(key) != null;
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

        return node.nodeType == NodeType.KV_NODE ? node.v : null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        size = 0;
        root = new Node();
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
