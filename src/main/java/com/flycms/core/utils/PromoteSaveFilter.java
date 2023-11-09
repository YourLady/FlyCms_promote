package com.flycms.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.flycms.module.article.model.Article;
import com.flycms.module.user.model.User;
//import com.flycms.module.user.service.UserService;
import com.flycms.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PromoteSaveFilter {

    public void SaveFileToFolder(String path, Article article,UserService userService)throws IOException{

        //String filePath = "C:/example.txt";
       // String content = "Hello, world!";
        User user = userService.findUserById(article.getUserId(),0);
        String fileName = path + user.getUserId().toString()+File.separator+article.getTitle()+File.separator+article.getTitle()+ (article.getPromoteVersion()==null?article.getPromote_version():article.getPromoteVersion())+".txt";
        File file = new File(fileName);
        if(!file.isFile() && !file.exists())
        {
            String  path1 = fileName.substring(0,fileName.lastIndexOf(File.separator));
            new File(path1).mkdirs();
            String name1 = fileName.substring(fileName.lastIndexOf(File.separator));
            new File(name1).createNewFile();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(article.getContent());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
    //public static void code1()
    //{//创建一个文件。定义需要操作的文件路径//eg：创建一个G:\javaTest\java.txt
        // File file = new File("G" + File.separator + "javaTest" + File.separator + "java.txt");
        // 首先得进行路径的判断，如果路径存在就直接判断文件是否存在
        // 如果路径不存在，这个时候就可以调用mkdirs函数进行创建路径
        // if (!file.getParentFile().exists()) {file.getParentFile().mkdirs();}
        // 判断这个文件是否存在。如果该文件存在，进行删除。//如果不存在，则创建
        // if (file.exists()) {file.delete();} else {try {file.createNewFile();}
        // catch (IOException e) {e.printStackTrace();}}
        // }


    public void EditFileToFolder(String path, Article article, UserService userService){

        //String filePath = "C:/example.txt";
        // String content = "Hello, world!";
        User user = userService.findUserById(article.getUserId(),0);
        String fileName = path + user.getUserId().toString()+File.separator+article.getTitle()+File.separator+article.getTitle()+ (article.getPromoteVersion()==null?article.getPromote_version():article.getPromoteVersion())+".txt";

        File file = new File(fileName);
        FileWriter writer = null;
        if (file.exists()) {file.delete();}

        try {
            writer = new FileWriter(file);
            writer.write(article.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
