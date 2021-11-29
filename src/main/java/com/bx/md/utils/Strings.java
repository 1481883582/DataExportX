package com.bx.md.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
// <!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
//        <dependency>
//            <groupId>org.apache.commons</groupId>
//           <artifactId>commons-lang3</artifactId>
//            <version>3.9</version>
//        </dependency>


@Slf4j
public class Strings{

    public final static String EMPTY = "";

    public static String UUId16(){
        String uuId = Strings.UUId();
        uuId = uuId.replaceAll("-", "");
        return uuId.substring(0,16);
    }

    public static String currentTimeMillis(){
        return String.valueOf(System.currentTimeMillis());
    }

    public static String UUId(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-","");
    }
    /**
     * min-max之间的随机数
     * @param min
     * @param max
     * @return
     */
    public static int Random(Integer min, Integer max){
        return (int)((max-min)*Math.random())+min;
    }

    /**
     * 字符串转int类型
     * @param str
     * @return
     */
    public static Integer StrToInteger(String str){
        if(Strings.isEmpty(str)){
            return 0;
        }
        try{
            return Integer.valueOf(str).intValue();
        }catch (NumberFormatException e){
            log.error(e.toString());
        }

        return 0;
    }

    public static boolean isEmpty(List<?> value) {
        if (value == null || value.isEmpty() || value.size() == 0){
            return true;
        }
        return false;
    }

    public static boolean isEmpty(Object[] value) {
        if (value == null || value.length == 0){
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public static boolean isEmpty(Object object) {
        if (object == null || EMPTY.equals(object)){
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Object[] value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(List<?> value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(CharSequence value) {
        return !isEmpty(value);
    }
    /**
     * @Title: join
     * @Description: 用指定字符串数组相连接，并返回
     * @param strs 字符串数组
     * @param splitStr 连接数组的字符串
     * @return
     * @return: String
     */
    public static String join(String[] strs, String splitStr){
        if(strs!=null){
            if(strs.length==1){
                return strs[0];
            }
            StringBuffer sb = new StringBuffer();
            for (String str : strs) {
                sb.append(str).append(splitStr);
            }
            if(sb.length()>0){
                sb.delete(sb.length()-splitStr.length(), sb.length());
            }
            return sb.toString();
        }
        return null;
    }
    /**
     * @Title: subStrStart
     * @Description: 从头开始截取
     * @param str 字符串
     * @param end 结束位置
     * @return
     * @return: String
     */
    public static String subStrStart(String str, int end){
        return subStr(str, 0, end);
    }

    /**
     *
     * @Title: subStrEnd
     * @Description: 从尾开始截取
     * @param str 字符串
     * @param start 开始位置
     * @return
     * @return: String
     */
    public static String subStrEnd(String str, int start){
        return subStr(str, str.length()-start, str.length());
    }

    /**
     * @Title: subStr
     * @Description: 截取字符串 （支持正向、反向截取）
     * @param str 待截取的字符串
     * @param length 长度 ，>=0时，从头开始向后截取length长度的字符串；<0时，从尾开始向前截取length长度的字符串
     * @return
     * @throws RuntimeException
     * @return: String
     */
    public static String subStr(String str, int length) throws RuntimeException{
        if(str==null){
            throw new NullPointerException("字符串为null");
        }
        int len = str.length();
        if(len<Math.abs(length)){
            throw new StringIndexOutOfBoundsException("最大长度为"+len+"，索引超出范围为:"+(len-Math.abs(length)));
        }
        if(length>=0){
            return  subStr(str, 0,length);
        }else{
            return subStr(str, len-Math.abs(length), len);
        }
    }


    /**
     * 截取字符串 （支持正向、反向选择）
     * @param str  待截取的字符串
     * @param start 起始索引 ，>=0时，从start开始截取；<0时，从length-|start|开始截取
     * @param end 结束索引 ，>=0时，从end结束截取；<0时，从length-|end|结束截取
     * @return 返回截取的字符串
     * @throws RuntimeException
     */
    public static String subStr(String str, int start, int end) throws RuntimeException{
        if(str==null){
            throw new NullPointerException("");
        }
        int len = str.length();
        int s = 0;//记录起始索引
        int e = 0;//记录结尾索引
        if(len<Math.abs(start)){
            throw new StringIndexOutOfBoundsException("最大长度为"+len+"，索引超出范围为:"+(len-Math.abs(start)));
        }else if(start<0){
            s = len - Math.abs(start);
        }else if(start<0){
            s=0;
        }else{//>=0
            s = start;
        }
        if(len<Math.abs(end)){
            throw new StringIndexOutOfBoundsException("最大长度为"+len+"，索引超出范围为:"+(len-Math.abs(end)));
        }else if (end <0){
            e = len - Math.abs(end);
        }else if (end==0){
            e = len;
        }else{//>=0
            e = end;
        }
        if(e<s){
            throw new StringIndexOutOfBoundsException("截至索引小于起始索引:"+(e-s));
        }

        return str.substring(s, e);
    }
}