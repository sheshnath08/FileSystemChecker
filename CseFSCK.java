/**
 * Requires 1 command line argument, which is path of file's directory!
 */

package FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Sheshnath on 11/11/2015.
 */
public class CseFSCK {

    static int maxBlocks = 10000;
    static int freeBlockList[] = new int[maxBlocks];
    static int freeBlockStart;
    static int freeBlockEnd;
    static int devID;
    static int n = 1; // stores total  number of files in directory
    static int root;
    static long currentTime;
    static long creationTime;
    static String PATH="\\FS";



    public static void main(String args[]){
        if(args.length >=0){
            PATH = args[0];
        }
        for (int i = 0; i< maxBlocks;i++){
            freeBlockList[i]=0; // initally marking all the blocks full;
        }
        currentTime = System.currentTimeMillis()/1000L;
        n = new File(PATH).listFiles().length;
        //System.out.println("Total number of Blocks are : "+n);
        System.out.println("Reading Content of SuperBlock");
        File superBlock = new File(PATH+"\\fusedata.0");
        try{
            Scanner sc = new Scanner(superBlock);
            String content  = sc.nextLine();
            getSuperBlockData(content);
            sc.close();
        } catch (FileNotFoundException e){
            System.out.println(e);
        }

        System.out.println("Updating free block list");
        updateFreeBlockList(freeBlockStart, freeBlockEnd);
        System.out.println("Checking for DeviceID");
        checkDeviceId();
        System.out.println("validating time starts");
        validateAllTime();
        System.out.println("validating free Block List");
        validateFreeBlockList();
        System.out.println("Validating Directories!");
        validateDirectories(root);
        System.out.println("Validating LinkCount!");
        validateLinkCount();
    }



    // Method to validate Link count
    private static void validateLinkCount() {
        File f;
        for(int i = 26; i<n;i++)
        {
            f = new File(PATH+"\\fusedata."+i);
            try{
                Scanner sc = new Scanner(f);
                String s  = sc.nextLine();
                if(s.contains("filename_to_inode_dict"))// checking whether it is a directory or not
                {
                    //checking linkcount in directories
                    checkLinkCount(s, i);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private static void checkLinkCount(String s, int blockNo) {
        int linkCount;
        String contentArray[] = s.split(",");
        linkCount = Integer.parseInt(contentArray[7].substring(contentArray[7].indexOf(':') + 1));
        s=s.substring(s.indexOf('{',1));
        String temp[] = s.split(",");
        System.out.println("LinkCount at block #"+ blockNo +" is "+ linkCount);
        if(linkCount == temp.length){
            System.out.println("LinkCount for directory at block # "+blockNo+" is correct");
        }
        else{
            System.out.println("LinkCount for directory at block # "+blockNo+" is incorrect");
            System.out.println("LinkCount found: "+ temp.length);
        }
    }



    // Validating entries in directory
    private static void validateDirectories(int root){
        File f;
        f = new File(PATH+"\\fusedata."+root);
        try{
            Scanner sc = new Scanner(f);
            String s  = sc.nextLine();
            if(s.contains("filename_to_inode_dict"))// checking whether it is a directory or not
            {
                s=s.substring(s.lastIndexOf("{")+1);
                processInodeRootDirectory(s, root);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //processing iNode of rootDirectory
    private static void processInodeRootDirectory(String s, int dirNo) {
        s= s.substring(0,s.length()-2);
        String contentArray[] = s.split(",");
        for(int i = 0;i<contentArray.length;i++){
            String temp[] = contentArray[i].split(":");
            if(temp[0].contains("f")) // file found
            {

            }
            else if(temp[0].contains("d")) // directory found
            {
                if(temp[1].equals("."))// checkin for current directory
                {
                    if(Integer.parseInt(temp[2]) == dirNo){
                        System.out.println(". entry for directory "+dirNo+" is correct");
                    }
                    else {
                        System.out.println(". entry for directory "+dirNo+" is incorrect");
                    }
                }

                else if(temp[1].equals("..") && dirNo == root)// checkin for parent directory for root
                {

                    if(Integer.parseInt(temp[2]) == dirNo){
                        System.out.println(".. entry for directory "+dirNo+" is correct");
                    }
                    else {
                        System.out.println(".. entry for directory "+dirNo+" is incorrect");
                    }
                }
                else{
                    validateSubDirectory(dirNo,Integer.parseInt(temp[2]));
                    // create method to check for subdirectories. that take dir no and parent as input
                }
            }
            else if(temp[0].contains("s"))// Special file found
            {
                System.out.println("Special file has been encountered");
            }
        }
    }


    //function to validate subDirectory
    private static void validateSubDirectory(int parentDirectory, int currentDirecotry) {
        File f = new File(PATH+"\\fusedata."+currentDirecotry);
        try{
            Scanner sc = new Scanner(f);
            String content = sc.nextLine();
            if(content.contains("filename_to_inode_dict"))// checking whether it is a directory or not
            {
                content=content.substring(content.lastIndexOf("{")+1);
                processInodeSubdirectory(parentDirectory, currentDirecotry, content);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void processInodeSubdirectory(int parentDirectory,int currentDirectory,String content){
        content = content.substring(0, content.length()-2);
        String contentArray[] = content.split(",");
        for(int i = 0;i<contentArray.length;i++){
            String temp[] = contentArray[i].split(":");
            if(temp[0].contains("f")) // file found
            {

            }
            else if(temp[0].contains("d")) // directory found
            {
                if(temp[1].equals("."))// checkin for current directory
                {
                    if(Integer.parseInt(temp[2]) == currentDirectory){
                        System.out.println(". entry for directory "+currentDirectory+" is correct");
                    }
                    else {
                        System.out.println(". entry for directory "+currentDirectory+" is incorrect");
                    }
                }

                else if(temp[1].equals(".."))// checkin for parent directory for root
                {

                    if(Integer.parseInt(temp[2]) == parentDirectory){
                        System.out.println(".. entry for directory "+currentDirectory+" is correct");
                    }
                    else {
                        System.out.println(".. entry for directory "+currentDirectory+" is incorrect");
                    }
                }
                else{
                    validateSubDirectory(parentDirectory,Integer.parseInt(temp[2]));
                    // create method to check for subdirectories. that take dir no and parent as input
                }
            }
            else if(temp[0].contains("s"))// Special file found
            {
                System.out.println("Special file has been encountered");
            }
        }
    }




    private static void validateFreeBlockList() {

    }

    private static void validateAllTime() {
        boolean flag = true;
        long aTime = 0;
        long cTime = 0;
        long mTime = 0;
        if(creationTime>currentTime){
            System.out.println("Creation time for superBlock is in future");
            flag = false;
        }
        else{
            System.out.println("Creation time for superBlock is valid time");
        }
        File f;
        for(int i = 1; i<n;i++)//checking each file after SuperBlock for valid time
        {
            if(i>=freeBlockStart && i<= freeBlockEnd)
                continue;
            else{
                f = new File(PATH+"\\fusedata."+i);
                try{
                    Scanner sc = new Scanner(f);
                    String s  = sc.nextLine();
                    if(s.contains("time")){
                        String contentArray[] = s.split(",");
                        for(int j = 0;j<contentArray.length;j++){
                            if(contentArray[j].contains("time")){
                                aTime = Integer.parseInt(contentArray[j].substring(contentArray[j].indexOf(':') + 1));
                                j= j+1;
                                cTime = Integer.parseInt(contentArray[j].substring(contentArray[j].indexOf(':') + 1));
                                j = j+1;
                                mTime = Integer.parseInt(contentArray[j].substring(contentArray[j].indexOf(':') + 1));
                                break;
                            }
                            else{
                                continue;
                            }
                        }

                        if(aTime>currentTime){
                            System.out.println("aTime for file at block "+i+" is in future");
                            flag = false;
                        }
                        if(cTime>currentTime){
                            System.out.println("cTime for file at block "+i+" is in future");
                            flag = false;
                        }
                        if(mTime>currentTime){
                            System.out.println("mTime for file at block "+i+" is in future");
                            flag = false;
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        if(flag){
            System.out.print("All times are in the past, nothing in the future.");
        }
        else{
            System.out.print("All times are not in the past!!");
        }
    }
    private static void checkDeviceId() {
        if(devID == 20){
            System.out.println("DevID is correct : "+devID);
        }
        else
        {
            System.out.println("DevID is incorrect : "+devID);
        }
    }

    static void updateFreeBlockList(int startBlock, int endBlock){
        File block;
        Scanner sc;
        for(int i= startBlock;i<endBlock;i++) {
            block = new File(PATH+"\\fusedata."+i);
            try {
                sc = new Scanner(block);
                while (sc.hasNext()){
                    String s = sc.next();
                    int t = Integer.parseInt(s.substring(0,s.length()-1));
                    freeBlockList[t]=1; // updating free block list.
                    // System.out.println(t);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Free Block list has been updated");
    }


    static void getSuperBlockData(String contentOfSuperBlock){
        String contentArray[] = contentOfSuperBlock.split(",");
        freeBlockStart = Integer.parseInt(contentArray[3].substring(contentArray[3].indexOf(':')+1));
        freeBlockEnd = Integer.parseInt(contentArray[4].substring(contentArray[4].indexOf(':') + 1));
        devID = Integer.parseInt(contentArray[2].substring(contentArray[2].indexOf(':') + 1));
        creationTime = Long.parseLong(contentArray[0].substring(contentArray[0].indexOf(':') + 2));
        root = Integer.parseInt(contentArray[5].substring(contentArray[5].indexOf(':') + 1));
        System.out.println("Free block List start:" + freeBlockStart);
        System.out.println("Free block list end:" + freeBlockEnd);
        System.out.println("DevID is:" + devID);
    }
}