public class testKey {
    int hash;

    public testKey(int hash){
        this.hash = hash;
    }
    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
