package com.nowcoder.community.util;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
@Component
public class SensetiveFilter {
    //定义日志
        private static final Logger logger = LoggerFactory.getLogger(SensetiveFilter.class);
    //定义替换符
        private static final String replaceWord = "***";
    //定义根节点
        private TrieNode rootNode = new TrieNode();
    //初始化方法
    @PostConstruct
    public void init(){
        //将敏感词添加到前缀树


            try( //从txt中获取字节流
                 InputStream Is = this.getClass().getClassLoader().getResourceAsStream("sensetive-word.txt");
                 //从bufferedreader中读敏感词
                 BufferedReader reader = new BufferedReader(new InputStreamReader(Is));
                    ) {
                        String keyword;
                        while ((keyword = reader.readLine()) != null){
                            this.addKeyword(keyword);
                        }
            } catch (IOException e) {
                logger.error("敏感词读取错误！" + e.getMessage());
            }
    }

    //将敏感词添加到前缀树
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            //初始化子节点
            TrieNode subNodes = tempNode.getSubNodes(c);
            //如果当前节点无对应字符的子节点 则将该子节点创建并连接到当前节点上
            if(subNodes == null){
                subNodes = new TrieNode();
                tempNode.addSubNodes(c,subNodes);
            }
            //将当前节点位置指向子节点 继续添加操作
            tempNode = subNodes;
            //如果到了字符串末尾 则添加标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
//            // 空值判断
//            if (StringUtils.isBlank(text)){
//                return null;
//            }
//            // 不为空的情况，开始对文本进行过滤处理；用三个指针，分别指向的是前缀树、文本；
//            // 在文本中，相当于用双指针，找到前缀树中出现的路径，然后对双指针指向的区域进行文本替换
//            TrieNode tempNode= rootNode; // 指针 1
//            int begin=0,position=0;  // 指针2 3
//            // 结果
//            StringBuilder sb=new StringBuilder();
//
//            while(position < text.length()){
//
//                Character c = text.charAt(position);
//
//                // 跳过符号
//                if (isSymbol(c)) {
//                    if (tempNode == rootNode) {
//                        begin++;
//                        sb.append(c);
//                    }
//                    position++;
//                    continue;
//                }
//                // 检查下级节点
//                tempNode = tempNode.getSubNodes(c);
//                if (tempNode == null) {
//                    // 以begin开头的字符串不是敏感词
//                    sb.append(text.charAt(begin));
//                    // 进入下一个位置
//                    position = ++begin;
//                    // 重新指向根节点
//                    tempNode = rootNode;
//                }
//                // 发现敏感词
//                else if (tempNode.isKeywordEnd()) {
//                    sb.append(replaceWord);
//                    begin = ++position;
//                }
//                // 检查下一个字符
//                else {
//                    position++;
//                }
//
//                // 提前判断postion是不是到达结尾，要跳出while,如果是，则说明begin-position这个区间不是敏感词，但是里面不一定没有
//                if (position==text.length() && begin!=position){
//                    // 说明还剩下一段需要判断，则把position==++begin
//                    // 并且当前的区间的开头字符是合法的
//                    sb.append(text.charAt(begin));
//                    position=++begin;
//                    tempNode=rootNode;  // 前缀表从头开始了
//                }
//            }
//            return sb.toString();
//
//        //空值处理
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指向前缀树的指针1
        TrieNode s1 = rootNode;
        //指针2
        int s2 = 0;
        //指针3
        int s3 = 0;
        int end = text.length();
        StringBuilder sb = new StringBuilder();
        while(s2 < end){
            char c = text.charAt(s3);
            //遇到特殊字符
            if(isSymbol(c)){
                //如果没有进入敏感词判断阶段 s2++ s3++ 将特殊字符加入字符串
                if(s1 == rootNode){
                    sb.append(c);
                    s2++;
                }
                //否则只移动s3
                s3++;
                continue;
            }
            //获取下一节点的状态
            s1 = s1.getSubNodes(c);
            //如果前缀树内不含当前字符s2与s3同时前移1位并重置s1指针
            if(s1 == null){
                sb.append(text.charAt(s2));
                s2++;
                s3 = s2;
                s1 = rootNode;
            }else{
                //进入敏感词判断
                    //如果已经到达敏感词的末尾位置 则替换为***
                    if(s1.isKeywordEnd()){
                        sb.append(replaceWord);
                        s2 = s3 + 1;
                    }else{
                        //否则继续寻找敏感词的末尾
                        s3++;
                    }
            }
        }
        return sb.toString();
    }

    private boolean isSymbol(Character c){
        //判断字符c是否为特殊符号
        //0x9FFF ~ 0x2E80 范围为东亚字符 不属于特殊符号
        return !CharUtils.isAsciiAlphanumeric(c) && (c >0x9FFF || c< 0x2E80);
    }

    private class TrieNode {
        //敏感词结束标识
            private boolean isKeywordEnd = false;
        //子节点
            private Map<Character, TrieNode> subNodes = new HashMap<>();

            private boolean isKeywordEnd(){
                return isKeywordEnd;
            }

            private void setKeywordEnd(boolean KeywordEnd){
                isKeywordEnd = KeywordEnd;
            }
        //添加子节点
            private void addSubNodes(Character c, TrieNode node){
                subNodes.put(c,node);
            }
        //获取子节点
            private TrieNode getSubNodes(Character c){
                return subNodes.get(c);
            }
    }
}
