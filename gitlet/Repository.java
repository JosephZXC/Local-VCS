package gitlet;



import java.io.File;
import static gitlet.Utils.*;

import java.text.SimpleDateFormat;
import java.util.*;
// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    public static final File OBJECTS = Utils.join(GITLET_DIR, "objects");
    public static final File INDEX = Utils.join(GITLET_DIR, "index");
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");
    public static final File REFS = Utils.join(GITLET_DIR, "refs");
    public static final File HIDDEN = Utils.join(REFS, ".hidden");
    //public static final File MASTER = Utils.join(REFS, "master");

    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        initDirectory();
        Commit initialCommit = new Commit("initial commit", "", dateFormat(new Date(0)));
        initialCommit.save();
        Head head = new Head("master");
        head.save();
        Staging staging = new Staging();
        staging.save();
        writeBranch(Utils.join(REFS, "master"), initialCommit.getID());
    }

    public static void add(String filename) {
        File file = Utils.join(CWD, filename);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(file);
        if (!blob.getSavedFile().exists()) {
            blob.save();
        }
        Commit curr = getCurr();
        Staging staging = Utils.readObject(INDEX, Staging.class);
        if (staging.stagedForRemoval(filename)) {
            staging.delRemove(filename);
        }
        if (curr.fileTracked(filename, blob.getID())) {
            if (staging.stagedForAddition(filename)) {
                staging.delAddition(filename);
            }
        } else {
            staging.putAddition(filename, blob.getID());
        }
        staging.save();
    }

    public static void commit(String message) {
        commit(message, null);
    }

    public static void commit(String message, String parentID) {
        Staging staging = Utils.readObject(INDEX, Staging.class);
        if (staging.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }

        if (message.equals("")) {
            Utils.message("Please enter a commit message.");
        }

        Commit parentCommit = getCurr();
        Commit curr = new Commit(message, parentCommit.getID(), dateFormat(new Date()));
        curr.setTracked(parentCommit.copyTracked());

        for (String filename : staging.getAdditionFiles()) {
            curr.put(filename, staging.getAdditionFileID(filename));
        }
        for (String filename : staging.getRemovalFiles()) {
            curr.remove(filename);
        }
        if (parentID != null) curr.addParent(parentID);
        staging.clear();
        curr.setMeta(); // real commitID needs to be set when all tracked files are sorted
        curr.save();
        staging.save();

        Head head = Utils.readObject(HEAD, Head.class);
        writeBranch(Utils.join(REFS, head.getContents()), curr.getID());
        /*
        File branchFile = head.getBranch();

        if (branchFile != null) {
            Utils.writeContents(branchFile, curr.getFile());
        } else {
            head.setContents(curr.getID());
            Utils.writeContents(HEAD, head);
        }*/

    }


    public static void rm(String filename) {
        File file = Utils.join(CWD, filename);
        Commit curr = getCurr();
        Staging staging = Utils.readObject(INDEX, Staging.class);
        if (!staging.stagedForAddition(filename) && !curr.containsFile(filename)) {
            Utils.message("No reason to remove the file");
            System.exit(0);
        }
        if (staging.stagedForAddition(filename)) {
            staging.delAddition(filename);
        }
        if (curr.containsFile(filename)) {
            staging.putRemoval(filename, null);
            if (file.exists()) {
                Utils.restrictedDelete(file);
            }
        }
        staging.save();
    }

    public static void log() {
        Commit curr = getCurr();
        while (curr != null) {
            printCommit(curr);
            curr = getCommitByID(curr.getFirstParent());
        }
    }

    public static void global_log() {
        // TODO: Gather all commits in one separate directory
        List<Commit> commits = getBranches();
        Set<String> seen = new HashSet<String>();
        for (Commit commit : commits) {
            dfsLog(commit, seen);
        }

    }

    public static void find(String message) {
        List<Commit> commits = getBranches();
        Set<String> seen = new HashSet<String>();
        int count = 0;
        for (Commit commit : commits) {
            count += dfsFind(commit, seen, message);
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        printBranches();
        printStaged();
        printModified();
        printUntracked();
    }

    public static void checkout_file(String filename) {
        Commit curr = getCurr();
        if (!curr.containsFile(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        overWriteCWDFile(filename, curr.getFileID(filename));
    }

    public static void checkout_commit(String commitID, String filename) {
        Commit commit = getCommitByID(commitID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!commit.containsFile(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        overWriteCWDFile(filename, commit.getFileID(filename));
    }

    public static void checkout_branch(String branch) {
        List<String> branches = Utils.plainFilenamesIn(REFS);
        Head head = Utils.readObject(HEAD, Head.class);
        if (!branches.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (head.getContents().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        checkout_commit(getBranchID(REFS, branch));

        /*
        Commit branchCommit = getCommitByID();
        Head head = Utils.readObject(HEAD, Head.class);

        if (untrackedFiles().size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        for (String workingFile : workingFiles) {
            if (!branchCommit.containsFile(workingFile)) {
                Utils.restrictedDelete(workingFile);
            }
        }
        for (String filename : branchCommit.getFiles()) {
            overWriteCWDFile(filename, branchCommit.getFileID(filename));
        }
        Staging staging = Utils.readObject(INDEX, Staging.class);
        staging.clear();
        staging.save();*/
        head.setContents(branch);
        head.save();
    }

    // TODO: WILL NEVER BE IN A DETACHED HEAD STATE
    public static void checkout_commit(String commitID) {
        //Head head = Utils.readObject(HEAD, Head.class);
        Commit commit = getCommitByID(commitID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }


        if (untrackedFiles().size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        for (String workingFile : workingFiles) {
            if (!commit.containsFile(workingFile)) {
                Utils.restrictedDelete(workingFile);
            }
        }
        for (String filename : commit.getFiles()) {
            overWriteCWDFile(filename, commit.getFileID(filename));
        }
        Staging staging = Utils.readObject(INDEX, Staging.class);
        staging.clear();
        staging.save();
        //head.setContents(commitID);
        //head.save();
    }
    public static void branch(String branchName) {
        File branchFile = Utils.join(REFS, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit curr = getCurr();
        writeBranch(Utils.join(REFS, branchName), curr.getID());
    }

    public static void rm_branch(String branchName) {
        File branchFile = Utils.join(REFS, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Head head = Utils.readObject(HEAD, Head.class);
        if (head.getContents().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branchFile.delete();
    }

    public static void reset(String commitID) {
        Head head = Utils.readObject(HEAD, Head.class);
        String branch = head.getContents();
        writeBranch(Utils.join(HIDDEN, "hidden " + branch), getBranchID(REFS, branch));
        checkout_commit(commitID);
        writeBranch(Utils.join(REFS, branch), commitID);
        Commit curr = getCurr();
        writeBranch(Utils.join(REFS, branch), curr.getID());
    }

    public static void merge(String branchName) {
        Staging staging = Utils.readObject(INDEX, Staging.class);
        if (!staging.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        File branchFile = Utils.join(REFS, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Head head = Utils.readObject(HEAD, Head.class);
        if (branchName.equals(head.getContents())) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (untrackedFiles().size() != 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        Commit curr = getCurr();
        String branchID = getBranchID(REFS, branchName);
        String splitID = getSplit(curr.getID(), branchID);

        if (splitID.equals(branchID)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitID.equals(curr.getID())) {
            Utils.message("Current branch fast-forwarded.");
        }
        Commit splitCommit = getCommitByID(splitID);
        Commit branchCommit = getCommitByID(branchID);
        boolean cond1 = checkSplit(splitCommit, curr, branchCommit); // branchName should be id
        boolean cond2 = checkCurr(splitCommit, curr, branchCommit);
        boolean cond3 = checkBranch(splitCommit, curr, branchCommit);
        commit("Merged " + branchName + " into " + head.getContents() + ".", branchID);
        if (cond1 || cond2 || cond3) {
            Utils.message("Encountered a merge conflict.");
        }
    }

    public static boolean checkSplit(Commit split, Commit curr, Commit branch) {
        boolean flag = false;
        for (String filename : split.getFiles()) {
            if (curr.fileTracked(filename, split.getFileID(filename)) && !branch.containsFile(filename)) {
                //Utils.restrictedDelete(filename);
                rm(filename);
            } else if (branch.fileTracked(filename, split.getFileID(filename)) && !curr.containsFile(filename)) {
                continue;
            } else if (curr.fileTracked(filename, split.getFileID(filename)) && branch.containsFile(filename) && !branch.fileTracked(filename, split.getFileID(filename))) {
                //Utils.restrictedDelete(filename);
                overWriteCWDFile(filename, branch.getTracked().get(filename));
                add(filename);
            } else if (branch.fileTracked(filename, split.getFileID(filename)) && curr.containsFile(filename) && !curr.fileTracked(filename, split.getFileID(filename))) {
                continue;
            } else if ((!branch.containsFile(filename) && !curr.containsFile(filename)) || (curr.getFileID(filename).equals(branch.getFileID(filename)))) {
                continue;
            } else {
                flag = true;
                solveConflicts(filename, curr, branch);
            }
        }
        return flag;
    }

    public static boolean checkBranch(Commit split, Commit curr, Commit branch) {
        boolean flag = false;
        for (String filename : branch.getFiles()) {
            if (!split.containsFile(filename)) {
                if (!curr.containsFile(filename)) {
                    overWriteCWDFile(filename, branch.getFileID(filename));
                    add(filename);
                } else if (curr.containsFile(filename) && !curr.fileTracked(filename, branch.getFileID(filename))) {
                    flag = true;
                    solveConflicts(filename, curr, branch);
                }
            }
        }
        return flag;
    }

    public static boolean checkCurr(Commit split, Commit curr, Commit branch) {
        boolean flag = false;
        for (String filename : curr.getFiles()) {
            if (!split.containsFile(filename)) {
                if (curr.containsFile(filename) && curr.fileTracked(filename, branch.getFileID(filename))) {
                    flag = true;
                    solveConflicts(filename, curr, branch);
                }
            }
        }
        return flag;

    }


    public static void solveConflicts(String filename, Commit curr, Commit branch) {
        String currText = curr.containsFile(filename) ? new String(getBlobByID(curr.getFileID(filename)).getContents()) : "";
        String branchText = branch.containsFile(filename) ? new String(getBlobByID(branch.getFileID(filename)).getContents()): "";
        conflictText(filename, currText, branchText);
        add(filename);
    }

    public static void conflictText(String filename, String currText, String branchText) {
        String text = "<<<<<<< HEAD\n" + currText + "=======" + "\n" + branchText + ">>>>>>>" + "\n";
        // TODO: MIGHT BE SOMETHING
        Utils.writeContents(Utils.join(CWD, filename), text);

    }
    public static String getSplit(String curr, String branch) {
        Queue<String> queue1 = new LinkedList<String>();
        Queue<String> queue2 = new LinkedList<String>();
        queue1.offer(curr); queue2.offer(branch);
        Set<String> visited1 = new HashSet<String>();
        Set<String> visited2 = new HashSet<String>();
        visited1.add(curr); visited2.add(branch);
        while (!queue1.isEmpty() && !queue2.isEmpty()) {
            int size1 = queue1.size();
            for (int i = 0; i < size1; i++) {
                String commitID1 = queue1.poll();
                if (visited2.contains(commitID1)) {
                    return commitID1;
                }
                Commit commit1 = getCommitByID(commitID1);
                for (String parent : commit1.getParent()) {
                    if (!visited1.contains(parent)) {
                        queue1.offer(parent);
                        visited1.add(parent);
                    }
                }

                String commitID2 = queue2.poll();
                if (visited1.contains(commitID2)) {
                    return commitID2;
                }
                Commit commit2 = getCommitByID(commitID2);
                for (String parent : commit2.getParent()) {
                    if (!visited2.contains(parent)) {
                        queue2.offer(parent);
                        visited2.add(parent);
                    }
                }
            }
        }
        return null;
    }

    public static Commit getCommitByID(String commitID) {
        // TODO: change the OBJECT directory and commitID size needs to be larger than 6
        /*
        if (commitID.length() == 40) {
            return Utils.readObject(Utils.join(OBJECTS, commitID), Commit.class);
        } else {
            return Utils.readObject(Utils.join(OBJECTS, commitID.substring(0, 6)), Commit.class);
        }*/
        if (commitID.length() < 6) {
            return null;
        }
        File file = Utils.join(OBJECTS, commitID.substring(0, 6));
        if (file.exists()) {
            return Utils.readObject(file, Commit.class);
        } else {
            return null;
        }
    }

    public static Blob getBlobByID(String blobID) {
        File file = Utils.join(OBJECTS, blobID.substring(0, 6));
        if (file.exists()) {
            return Utils.readObject(file, Blob.class);
        } else {
            return null;
        }
        //return Utils.readObject(Utils.join(OBJECTS, blobID.substring(0, 6)), Blob.class);
        /*
        if (blobID.length() == 40) {
            return Utils.readObject(Utils.join(OBJECTS, blobID), Blob.class);
        } else {
            return Utils.readObject(Utils.join(OBJECTS, blobID.substring(0, 6)), Blob.class);
        }*/
    }

    public static void overWriteCWDFile(String filename, String BlobID) {
        File file = Utils.join(CWD, filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
        Blob blob = getBlobByID(BlobID);
        writeContents(file, blob.getContents());
    }
    public static void printBranches() {
        Head head = Utils.readObject(HEAD, Head.class);
        String headBranch = head.getContents();
        List<String> branches = Utils.plainFilenamesIn(REFS);
        Collections.sort(branches);
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (headBranch.equals(branch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();
    }

    public static void printStaged() {
        Staging staging = Utils.readObject(INDEX, Staging.class);
        System.out.println("=== Staged Files ===" );
        for (String workingFile : staging.getAdditionFiles()) {
                System.out.println(workingFile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String workingFile : staging.getRemovalFiles()) {
            System.out.println(workingFile);
        }
        System.out.println();
    }

    public static void printModified() {
        Staging staging = Utils.readObject(INDEX, Staging.class);
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        Commit curr = getCurr();
        List<String> modified = new ArrayList<String>();
        for (String workingFile : workingFiles) {
            Blob blob = new Blob(Utils.join(CWD, workingFile));
            boolean cond1 = curr.containsFile(workingFile) && !staging.stagedForAddition(workingFile) && !blob.same(curr.getFileID(workingFile));
            boolean cond2 = staging.stagedForAddition(workingFile) && !blob.same(staging.getAdditionFileID(workingFile));
            if (cond1 || cond2) {
                modified.add(workingFile + " (modified)");
            }
        }
        for (String filename: staging.getAdditionFiles()) {
            if (!workingFiles.contains(filename)) {
                modified.add(filename + " (deleted)");
            }
        }
        for (String filename : curr.getFiles()) {
            if (!staging.stagedForRemoval(filename) && !workingFiles.contains(filename)) {
                modified.add(filename + " (deleted)");
            }
        }
        Collections.sort(modified);
        System.out.println("=== Modifications Not Staged For Commit ===" );
        for (String modifiedFile : modified) {
            System.out.println(modifiedFile);
        }
        System.out.println();
    }

    public static List<String> untrackedFiles() {
        Staging staging = Utils.readObject(INDEX, Staging.class);
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        Collections.sort(workingFiles);
        Commit curr = getCurr();
        List<String> untracked = new ArrayList<String>();
        for (String workingFile : workingFiles) {
            boolean cond1 = !staging.stagedForAddition(workingFile) && !curr.containsFile(workingFile);
            boolean cond2 = staging.stagedForRemoval(workingFile);
            if (cond1 || cond2) {
                untracked.add(workingFile);
            }
        }
        return untracked;
    }

    public static void printUntracked() {
        List<String> untrackedFiles = untrackedFiles();
        System.out.println("=== Untracked Files ===" );
        for (String untrackedFile : untrackedFiles) {
            System.out.println(untrackedFile);
        }
        System.out.println();
    }

    public static List<Commit> getBranches() {
        List<String> branchNames = Utils.plainFilenamesIn(REFS);
        List<String> hiddenBranchNames = Utils.plainFilenamesIn(HIDDEN);
        List<Commit> branches = new ArrayList<Commit>();
        for (String branchName : branchNames) {
            branches.add(getCommitByID(getBranchID(REFS, branchName)));
        }
        for (String branchName : hiddenBranchNames) {
            branches.add(getCommitByID(getBranchID(HIDDEN, branchName)));
        }
        return branches;
    }

    public static void dfsLog(Commit commit, Set<String> seen) {
        seen.add(commit.getID());
        printCommit(commit);
        for (String parent : commit.getParent()) {
            Commit parentCommit = getCommitByID(parent);
            if (parentCommit != null && !seen.contains(parentCommit.getID())) {
                dfsLog(parentCommit, seen);
            }
        }
    }

    public static int dfsFind(Commit commit, Set<String> seen, String message) {
        int count = 0;
        seen.add(commit.getID());
        if (commit.getMessage().equals(message)) {
            count++;
            System.out.println(commit.getID());
        }
        for (String parent : commit.getParent()) {
            Commit parentCommit = getCommitByID(parent);
            if (parentCommit != null && !seen.contains(parentCommit.getID())) {
                count += dfsFind(parentCommit, seen, message);
            }
        }
        return count;
    }

    public static void printCommit(Commit curr) {
        System.out.println("===");
        System.out.println("commit " + curr.getID());
        if (curr.merged()) {
            System.out.println("Merge: " + curr.getFirstParent().substring(0, 7) + " " + curr.getSecondParent().substring(0, 7));
        }
        System.out.println("Date: " + curr.getTimestamp());
        System.out.println(curr.getMessage());
        System.out.println();
    }


    private static void initDirectory() {
        try {
            GITLET_DIR.mkdir();
            OBJECTS.mkdir();
            INDEX.createNewFile();
            HEAD.createNewFile();
            REFS.mkdir();
            HIDDEN.mkdir();
            //MASTER.createNewFile();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static Commit getCurr() {
        Head head = Utils.readObject(HEAD, Head.class);
        return getCommitByID(getBranchID(REFS, head.getContents()));
    }

    public static String dateFormat(Date date) {
        String pattern = "E MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return sdf.format(date);
    }

    public static void writeBranch(File branch, String commitID) {
        //File branchFile = Utils.join(REFS, branch);
        if (!branch.exists()) {
            try {
                branch.createNewFile();
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
        Utils.writeContents(branch, commitID);
    }




    public static String getBranchID(File file, String branch) {
        File branchFile = Utils.join(file, branch);
        return Utils.readContentsAsString(branchFile);
    }




}
