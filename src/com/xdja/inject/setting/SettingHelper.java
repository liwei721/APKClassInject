package com.xdja.inject.setting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlw on 2017/6/26.
 */
public class SettingHelper {
    private final static String SETTING_PATH = System.getProperty("user.dir") + File.separator + "\\resource\\InjectSetting.json" ;
    public static SettingEntity getSetting(){
        Gson gson = new Gson();
        SettingEntity settingEntity = null;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(new FileInputStream(SETTING_PATH)));
//            JsonParser jsonParser = new JsonParser();
//            // 将json转成json Array
//            JsonArray jsonArray = jsonParser.parse(reader).getAsJsonArray();
//            for (JsonElement entity : jsonArray){
//                SettingEntity settingEntity = gson.fromJson(entity, SettingEntity.class);
//                settingEntitys.add(settingEntity);
//            }
            settingEntity = gson.fromJson(reader, SettingEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return settingEntity;
    }



    public static void main(String[] args){
        getSetting();
    }
 }

