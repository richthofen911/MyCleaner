package net.callofdroidy.mycleaner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;

/**
 * Created by admin on 20/11/15.
 */
public class ThreadCleanAppGarbage implements Runnable{
    private File targetFile;
    private Handler handler;

    public ThreadCleanAppGarbage(String filePath, Handler mHandler){
        targetFile = new File(filePath);
        handler = mHandler;
    }

    @Override
    public void run(){
        handleMessage(Constants.MESSAGE_STATUS, "start to clean, trying to delete the target file...");
        if(targetFile.isDirectory()) {
            deleteDir(targetFile);
            checkTaskResult();
        }else if(targetFile.isFile()) {
            deleteFile(targetFile);
            checkTaskResult();
        }else if(!targetFile.exists()){
            handleMessage(Constants.MESSAGE_FINISH, "Target file does not exist");
        }else
            handleMessage(Constants.MESSAGE_ERROR, "Target file is not a File Object");
    }

    // called when the file object is a -------------- File
    private void deleteFile(File file){
        String fileName = file.getName();
        if(file.delete())
            handleMessage(Constants.MESSAGE_STATUS, "a file is successfully deleted: " + fileName);
        else
            handleMessage(Constants.MESSAGE_STATUS, "failed to delete file: " + fileName);
    }

    // called when the file object is a -------------- Directory
    private void deleteDir(File subDir){
        handleMessage(Constants.MESSAGE_STATUS, "meet a subdirectory, entering it to delete all files inside...");
        for(File file: subDir.listFiles()){
            if(file.isFile())
                deleteFile(file);
            else if(file.isDirectory())
                deleteDir(file);
        }
        handleMessage(Constants.MESSAGE_STATUS, "all files in this directory have been deleted, quit from inside and trying to delete it...");
        if(subDir.delete())
            handleMessage(Constants.MESSAGE_STATUS, "the directory has been successfully deleted");
        else
            handleMessage(Constants.MESSAGE_ERROR, "failed to deleted the directory at: " + subDir.getAbsolutePath());
    }

    // in the end, check whether the cleaning job is done or not
    private void checkTaskResult(){
        if(!targetFile.exists())
            handleMessage(Constants.MESSAGE_FINISH, "cleaning job is successfully done");
        else
            handleMessage(Constants.MESSAGE_FINISH, "failed to delete the target file");
    }

    private void handleMessage(int what, String content){
        Message msg = handler.obtainMessage(what);
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
