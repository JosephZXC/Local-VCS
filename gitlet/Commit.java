package gitlet;

// TODO: any imports you need here


import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.OBJECTS;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private List<String> parent;
    private String timestamp;
    private TreeMap<String, String> tracked;
    private String id;
    private String short_id;
    private File file;

    /** The message of this Commit. */
    private String message;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String parent, String timestamp) {
        this.timestamp = timestamp;
        this.message = message;
        this.parent = new ArrayList<String>();
        this.parent.add(parent);
        this.tracked  = new TreeMap<String, String>();
        this.id = generateID();
        this.short_id = this.id.substring(0, 6);
        this.file = Utils.join(OBJECTS, this.short_id);
    }


    public String getFirstParent() {
        return this.parent.get(0);
    }
    public String getSecondParent() {return this.parent.get(1);}
    public boolean merged() {
        return this.parent.size() == 2;
    }
    public List<String> getParent() {
        return this.parent;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public String getID() {
        return this.id;
    }

    public String getShortID() {return this.short_id;}

    public File getFile() {
        return this.file;
    }

    public TreeMap<String, String> copyTracked() {
        TreeMap<String, String> newTracked = new TreeMap<String, String>();
        newTracked.putAll(this.tracked);
        return newTracked;
    }

    public TreeMap<String, String> getTracked() {
        return this.tracked;
    }
    // TODO: WHY NOT USING SERIALIZATION
    public String generateID() {
        return Utils.sha1(this.message, this.parent.toString(), this.timestamp, this.tracked.toString());
    }


    public void save() {
        File commitFile = Utils.join(OBJECTS, this.short_id);
        try {
            commitFile.createNewFile();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        Utils.writeObject(commitFile, this);
    }

    public Set<String> getFiles() {
        return this.tracked.keySet();
    }

    public void addParent(String parentID) {
        this.parent.add(parentID);
    }

    public String getFileID(String filename) {
        return this.tracked.get(filename);
    }

    public boolean containsFile(String filename) {
        return this.tracked.containsKey(filename);
    }
    // TODO:
    public boolean fileTracked(String filename, String fileID) {
        return this.tracked.containsKey(filename) && this.tracked.get(filename).equals(fileID);
    }


    public void setTracked(TreeMap<String, String> tracked) {
        this.tracked = tracked;
    }

    public void setMeta() {
        this.id = generateID();
        this.short_id = this.id.substring(0, 6);
        this.file = Utils.join(OBJECTS, this.short_id);
    }



    public void put(String filename, String fileID) {
        this.tracked.put(filename, fileID);
    }

    public void remove(String filename) {
        this.tracked.remove(filename);
    }


}
