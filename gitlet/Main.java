package gitlet;

import static gitlet.Repository.GITLET_DIR;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if (args.length == 1) {
                    Utils.message("Please enter a commit message.");
                    System.exit(0);
                }
                if (args.length > 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.rm(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.global_log();
                break;
            case "find":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.status();
                break;
            case "checkout":
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length == 2) {
                    Repository.checkout_branch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkout_file(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkout_commit(args[1], args[3]);
                } else {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                if (!GITLET_DIR.exists()) {
                    Utils.message("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
                break;
            default:
                Utils.message("No command with that name exists.");

        }
        // TODO: SOME MORE FAILURE CASES IN THE OVERALL SPEC
    }
}
