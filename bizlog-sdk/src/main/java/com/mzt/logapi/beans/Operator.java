package com.mzt.logapi.beans;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;

/**
 * @author muzhantong
 * create on 2020/4/29 5:39 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operator {
    private String operatorId;


    private static Splitter splitter = Splitter.on(",").trimResults();
    public static void main(String[] args) throws IOException {
        init();
        List<String> lines = Files.readLines(new File("/Users/mzt/Downloads/tmpp/temp_data.txt"), Charset.defaultCharset());
        StringBuffer stringBuffer = new StringBuffer();
        int count = 0;
        for (String line : lines) {
            if(line.startsWith("id")){
                continue;
            }
            String[] lineArr = line.split("\t");
            for (int i = 0; i < 7; i++) {
                if(i >= lineArr.length){
                    break;
                }
                stringBuffer.append(lineArr[i]).append("\t");
            }
            for (int i = 7; i < 10; i++) {
                if(i >= lineArr.length){
                    break;
                }
                String cellStr = lineArr[i];
                if(cellStr.contains(",")){
                    for (String item : splitter.split(cellStr)) {
                        stringBuffer.append(map.getOrDefault(item,item)).append(",");
                    }
                    stringBuffer.append("\t");
                }else {
                    stringBuffer.append(map.getOrDefault(cellStr, cellStr)).append("\t");
                }
            }
            System.out.println(count++);
            stringBuffer.append("\n");
        }
        BufferedWriter bufferedWriter = Files.newWriter(new File("/Users/mzt/Downloads/tmpp/result.txt"), Charset.defaultCharset());
        bufferedWriter.write(stringBuffer.toString());
        bufferedWriter.flush();
    }

    public static Map<String, String> map = Maps.newHashMap();

    public static void init() throws IOException {

        List<String> lines = Files.readLines(new File("/Users/mzt/Downloads/tmpp/temp_data_map.txt"), Charset.defaultCharset());
        for (String line : lines) {
            if(line.startsWith("node_name")){
                continue;
            }
            String[] strings = line.split("\t");
            map.put(strings[1], strings[0]);

        }
    }
}
