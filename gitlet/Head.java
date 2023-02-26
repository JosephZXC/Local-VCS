package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.*;
// TODO: POINTS TO A BRANCH ALL THE TIME
public class Head implements Serializable {
    private String contents;

    public Head(String contents) {
        this.contents = contents;
    }

    /*
    public boolean atBranch() {
        if (Utils.join(REFS, this.contents).exists()) {
            return true;
        }
        return false;
    }

    // TODO: what if branch ref is empty
    public String getHEADID() {
        if (atBranch()) {
            return Utils.readObject(Utils.join(REFS, this.contents), String.class);
        } else {
            return this.contents;
        }
    }*/

    public File getBranch() {
        //if (atBranch()) return Utils.join(REFS, this.contents);
        //return null;
        return Utils.join(REFS, this.contents);
    }
    /*
    public File getHEADFile() {
        return Utils.join(OBJECTS, getHEADID());
    }*/

    public void setContents(String refID) {
        this.contents = refID;
    }

    public String getContents() {
        return this.contents;
    }

    public void save() {
        Utils.writeObject(HEAD, this);
    }

}
