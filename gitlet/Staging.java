package gitlet;


import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;
import static gitlet.Repository.INDEX;
public class Staging implements Serializable {
    private TreeMap<String, String> additionTree;
    private TreeMap<String, String> removalTree;

    public Staging() {
        this.additionTree = new TreeMap<String, String>();
        this.removalTree = new TreeMap<String, String>();
    }

    public void save() {
        Utils.writeObject(INDEX, this);
    }

    public boolean stagedForAddition(String filename) {return additionTree.containsKey(filename);}
    public boolean stagedForRemoval(String filename) {
        return removalTree.containsKey(filename);
    }

    public void putAddition(String filename, String id) {
        additionTree.put(filename, id);
    }

    public void putRemoval(String filename, String id) {
        removalTree.put(filename, id);
    }

    public void delAddition(String filename) {
        additionTree.remove(filename);
    }

    public void delRemove(String filename) {
        removalTree.remove(filename);
    }

    public void clear() {
        this.additionTree.clear();
        this.removalTree.clear();
    }

    public boolean isEmpty() {
        return this.additionTree.isEmpty() && this.removalTree.isEmpty();
    }

    public TreeMap<String, String> getAdditionTree() {
        return this.additionTree;
    }

    public TreeMap<String, String> getRemovalTree() {
        return this.removalTree;
    }

    public Set<String> getAdditionFiles() {
        return this.additionTree.keySet();
    }

    public Set<String> getRemovalFiles() {
        return this.removalTree.keySet();
    }

    public String getAdditionFileID(String filename) {
        return this.additionTree.get(filename);
    }
}
