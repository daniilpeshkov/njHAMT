import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HAMT<K, V> implements Map<K, V> {

    public static final int NODE_TABLE_SIZE = 5;

    private int size = 0;

    private enum NodeType{KV_NODE, TABLE_NODE}

    private Node root = new Node();

    private class Node implements Cloneable {
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

        Node(K key, V value, int height, int hash) {
            k = key;
            v = value;
            this.height = height;
            this.hash = hash;
            nodeType = NodeType.KV_NODE;
        }

        Node() {
            initTable();
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
            initTable();
        }
    }

    private int lowNBits(int n, int nBits) {
        return n & ((1 << nBits) - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();

        Node curNode = root;
        V prevValue = null;

        int keyHash = key.hashCode();
        int tmpHash = keyHash;
        int height = 0;

        while (curNode.nodeType != NodeType.KV_NODE && curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)] != null) {
            curNode = curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)];
            tmpHash >>= NODE_TABLE_SIZE;
            height++;
        }

        if (curNode.nodeType == NodeType.TABLE_NODE && curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)] == null) {
            curNode.table[lowNBits(tmpHash, NODE_TABLE_SIZE)] = new Node(key, value, height);
        } else if (curNode.nodeType == NodeType.KV_NODE) {
            if (key.equals(curNode.k)) {
                prevValue = curNode.v;
                curNode.v = value;
            } else {
                try {
                    Node tmpNode = (Node) curNode.clone();
                    tmpNode.height++;
                    curNode.makeNodeTable();
                    curNode.addNode(tmpNode);
                    curNode.addNode(new Node(key, value));
                    size++;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return prevValue;
    }

//    private getNode()

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
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
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
