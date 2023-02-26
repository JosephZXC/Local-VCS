package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Repository.OBJECTS;

public class Blob implements Serializable {
    private String id;
    private byte[] contents;
    private String filename;
    private File savedFile;
    private String short_id;

    public Blob(File file) {
        this.filename = file.getName();
        this.contents = Utils.readContents(file);
        this.id = generateID();
        this.short_id = this.id.substring(0, 6);
        this.savedFile = Utils.join(OBJECTS, short_id);
    }

    // TODO: MIGHT BE AN ISSUE
    public String generateID() {
        return Utils.sha1(contents);
    }

    public byte[] getContents() {
        return this.contents;
    }
    public String getFilename() {
        return this.filename;
    }
    public String getShortID() {
        return this.short_id;
    }
    public String getID() {return this.id;}

    public File getSavedFile() {
        return this.savedFile;
    }
    public void save() {
        File blobFile = Utils.join(OBJECTS, this.short_id);
        try {
            blobFile.createNewFile();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        Utils.writeObject(blobFile, this);
        //Utils.writeObject(savedFile, this);
    }

    public boolean same(Blob blob) {
        return this.id.equals(blob.getID());
    }

    public boolean same(String blobID) {
        return this.id.equals(blobID);
    }
}
