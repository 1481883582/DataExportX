package com.bx.md.utils;

import com.bx.md.constant.MDConstant;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.DasTableKey;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import static org.apache.poi.hslf.record.OEPlaceholderAtom.Table;

@Slf4j
public class MDUtils {

    private File file = null;

    private String name = "";

    /**
     * 前置处理  准备文件
     * @return
     * @throws IOException
     */
    public FileWriter begin(String dir) throws IOException {
        //获取文件名
        name = CacheDataUtils.getInstance().getSelectDbTable().getParent().getName();
        log.info("数据库:" + name);

        //写入文件名
        file = getOutputFile(dir, name, MDConstant.MD);

        //判断是否创建成功
        FileWriter fileWriter = new FileWriter(file, false);

        //库名
        String title = String.format(MDConstant.TITLE, name);
        fileWriter.write(title);
        writeLineSeparator(fileWriter, 2);

        //时间
        String exportDate = String.format(MDConstant.DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        fileWriter.write(exportDate);
        writeLineSeparator(fileWriter, 2);

        return fileWriter;
    }

    /**
     * sql语句前置处理  准备文件
     * @return
     * @throws IOException
     */
    public FileWriter beginSql(String dir, String suffix) throws IOException {
        //获取文件名
        name = CacheDataUtils.getInstance().getSelectDbTable().getParent().getName();
        log.info("数据库:" + name);

        //写入文件名
        file = getOutputFile(dir, name, suffix);

        //判断是否创建成功
        FileWriter fileWriter = new FileWriter(file, false);

        //sql代码
        fileWriter.write(MDConstant.SQL);
        writeLineSeparator(fileWriter, 1);
        return fileWriter;
    }

    public File exportFile(List<DasTable> tables, String dir) throws IOException {

        //前置处理  准备文件
        FileWriter fileWriter = begin(dir);

        tables.stream().forEach((t) ->{
            try {
                //只输出  当前库的
                if(name.equalsIgnoreCase(DasUtil.getSchema(t.getDasParent()))) {

                    fileWriter.write(String.format(MDConstant.CATALOG, t.getName()));
                    writeLineSeparator(fileWriter, 1);
                    fileWriter.write(String.format(MDConstant.COMMENT, t.getComment()));
                    writeLineSeparator(fileWriter, 2);
                    fileWriter.write(String.format(MDConstant.TABLE_HEADER));
                    writeLineSeparator(fileWriter, 1);
                    fileWriter.write(String.format(MDConstant.TABLE_SEPARATOR));
                    writeLineSeparator(fileWriter, 1);

                    JBIterable<? extends DasColumn> columns = DasUtil.getColumns(t);

                    for (int i = 0; i < columns.size(); i++) {

                        //每张表
                        DasColumn dasColumn = columns.get(i);
                        try {
                            Object[] values = new Object[6];
                            log.info("序号:" + i + 1);
                            values[0] = i + 1;
                            log.info("字段名称：" + dasColumn.getName());
                            values[1] = dasColumn.getName();
                            log.info("字段类型" + dasColumn.getDataType());
                            values[2] = dasColumn.getDataType();
                            log.info("是否为空" + dasColumn.isNotNull());
                            values[3] = dasColumn.isNotNull();
                            log.info("默认值:" + dasColumn.getDefault());
                            values[4] = dasColumn.getDefault();
                            log.info("备注:" + dasColumn.getComment());
                            values[5] = dasColumn.getComment();
                            fileWriter.write(String.format(MDConstant.TABLE_BODY, values));
                            writeLineSeparator(fileWriter, 1);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    //每张表循环完
                    writeLineSeparator(fileWriter, 2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        //刷新缓冲区
        fileWriter.flush();
        //关闭
        fileWriter.close();

        //返回文件
        return file;
    }



    public File getOutputFile(String dir, String database, String type) {
        return new File(dir, database + type);
    }

    private void writeLineSeparator(FileWriter fileWriter, int number) throws IOException {
        for (int i = 0; i < number; i++) {
            fileWriter.write(System.lineSeparator());
        }
    }

    /**
     * 得到Mysql数据库语句
     * @param tables
     * @param dir
     * @return
     * @throws IOException
     */
    public File mysqlSql(List<DasTable> tables, String dir) throws IOException {
        return null;
    }

    /**
     * 得到神通数据库sql语句
     * @param tables
     * @param dir
     * @throws IOException
     * @return
     */
    public File OsrdbSql(List<DasTable> tables, String dir) throws IOException {

        //前置处理  准备文件
        FileWriter fileWriter = beginSql(dir, MDConstant.OSRDB);

        tables.stream().forEach((t) ->{
            //获取表名
            String tableName = t.getName().toUpperCase();
            //主键名称
            String primaryStr = StringUtils.EMPTY;
            try {
                //只输出  当前库的所有表  表名对应了再进入一下步
                if(name.equalsIgnoreCase(DasUtil.getSchema(t.getDasParent()))) {

                    fileWriter.write("--Drop");
                    writeLineSeparator(fileWriter, 1);

                    //删除表
                    StringBuilder dropSB = new StringBuilder();
                    dropSB.append("Drop Table IF EXISTS ");
                    dropSB.append(tableName);
                    dropSB.append(";");
                    fileWriter.write(dropSB.toString());
                    writeLineSeparator(fileWriter, 1);

                    fileWriter.write("--Table");
                    writeLineSeparator(fileWriter, 1);
                    fileWriter.write("CREATE TABLE " + tableName);
                    writeLineSeparator(fileWriter, 1);
                    fileWriter.write("(");
                    writeLineSeparator(fileWriter, 1);
                    JBIterable<? extends DasColumn> columns = DasUtil.getColumns(t);

                    for (int i = 0; i < columns.size(); i++) {

                        //每张表
                        DasColumn dasColumn = columns.get(i);

                        try {
                            //是否是主键
                            boolean primary = DasUtil.isPrimary(dasColumn);
                            //是否自动生成
                            boolean autoGenerated = DasUtil.isAutoGenerated(dasColumn);
                            //不为null
                            boolean notNull = dasColumn.isNotNull();

                            //获取主键名称
                            if(primary) primaryStr = dasColumn.getName().toUpperCase();

                            log.info("字段名称：" + dasColumn.getName());
                            String columnName = dasColumn.getName().toUpperCase();
                            log.info("字段类型" + dasColumn.getDataType());

                            log.info("是否为空" + dasColumn.isNotNull());
                            //类型名称
                            String typeName = dasColumn.getDataType().typeName;
                            //类型大小
                            int size = dasColumn.getDataType().size;

                            //字段默认属性
                            String aDefault = dasColumn.getDefault();

                            //int
                            if(typeName.equalsIgnoreCase("int") || typeName.equalsIgnoreCase("int unsigned")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" integer");

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                if(autoGenerated) mappingSB.append(" AUTO_INCREMENT");
                                fileWriter.write(mappingSB.toString());

                            }
                            if(typeName.equalsIgnoreCase("varchar")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" character varying(" );
                                mappingSB.append(size);
                                mappingSB.append(")" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }
                            if(typeName.equalsIgnoreCase("tinyint")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" tinyint" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }
                            if(typeName.equalsIgnoreCase("timestamp") || typeName.equalsIgnoreCase("datetime") || typeName.equalsIgnoreCase("date")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" timestamp without time zone" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }
                            if(typeName.equalsIgnoreCase("decimal")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" decimal" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }
                            if(typeName.equalsIgnoreCase("longblob")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" BLOB" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }
                            if(typeName.equalsIgnoreCase("double")){
                                StringBuffer mappingSB = new StringBuffer();
                                mappingSB.append("\t");
                                mappingSB.append(columnName);
                                mappingSB.append(" double precision" );

                                //如果有默认值就加上
                                if(Strings.isNotEmpty(aDefault)) mappingSB.append(" DEFAULT ").append("'NULL'".equalsIgnoreCase(aDefault)?"NULL":aDefault);
                                //如果不等于null 就加上
                                if(notNull) mappingSB.append(" NOT NULL");
                                //如果2个都没加上  就加默认null
                                if(Strings.isEmpty(aDefault) && !notNull) mappingSB.append(" DEFAULT NULL");

                                fileWriter.write(mappingSB.toString());
                            }




                            fileWriter.write(",");
                            writeLineSeparator(fileWriter, 1);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    //声明主键
                    StringBuilder primarySB = new StringBuilder();
                    primarySB.append("\t");
                    primarySB.append("CONSTRAINT ");
                    primarySB.append(tableName);
                    primarySB.append("_PKEY PRIMARY KEY (");
                    primarySB.append(primaryStr);
                    primarySB.append(") ");
                    fileWriter.write(primarySB.toString());
                    writeLineSeparator(fileWriter, 1);

                    fileWriter.write(")");
                    writeLineSeparator(fileWriter, 1);

                    fileWriter.write("BINLOG ON ;");
                    writeLineSeparator(fileWriter, 1);

                    fileWriter.write("--Table Comment");
                    //每张表循环完
                    writeLineSeparator(fileWriter, 1);

                    //表注释
                    String tableComment = t.getComment();
                    if(Strings.isNotEmpty(tableComment) && !"NUll".equalsIgnoreCase(tableComment)) {
                        StringBuilder tableSB = new StringBuilder();
                        tableSB.append("COMMENT ON TABLE ");
                        tableSB.append(tableName);
                        tableSB.append(" IS ");
                        tableSB.append("'");
                        tableSB.append(t.getComment());
                        tableSB.append("';");
                        fileWriter.write(tableSB.toString());
                        //每张表循环完
                        writeLineSeparator(fileWriter, 1);
                    }


                    //输出注释
                    for (int i = 0; i < columns.size(); i++) {

                        //每张表
                        DasColumn dasColumn = columns.get(i);
                        try {
                            String columnComment = dasColumn.getComment();
                            if(Strings.isNotEmpty(columnComment) && !"NUll".equalsIgnoreCase(columnComment)){
                                StringBuilder columnSB = new StringBuilder();
                                columnSB.append("COMMENT ON COLUMN ");
                                columnSB.append(tableName);
                                columnSB.append(".");
                                columnSB.append(dasColumn.getName().toUpperCase());
                                columnSB.append(" IS ");
                                columnSB.append("'");
                                columnSB.append(columnComment);
                                columnSB.append("';");
                                fileWriter.write(columnSB.toString());
                                writeLineSeparator(fileWriter, 1);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }

                    fileWriter.write("--Indexes DDL");
                    writeLineSeparator(fileWriter, 1);

                    fileWriter.write("--这玩意老保存，给注释掉了，需要就打开，是创建索引的,错误原因：重复创建索引");
                    writeLineSeparator(fileWriter, 1);
                    //拼接最后一句话 之后
                    StringBuilder afterSB = new StringBuilder();
                    afterSB.append("--CREATE UNIQUE INDEX");
                    afterSB.append(" ");
                    afterSB.append(tableName);
                    afterSB.append("_PKEY ON");
                    afterSB.append(" ");
                    afterSB.append(tableName);
                    afterSB.append(" USING");
                    afterSB.append(" BTREE(" );
                    afterSB.append(primaryStr);
                    afterSB.append(")");
                    afterSB.append(" TABLESPACE SYSTEM INIT 64K NEXT 64K MAXSIZE UNLIMITED FILL 70 SPLIT 50;");
                    fileWriter.write(afterSB.toString());
                    //每张表循环完
                    writeLineSeparator(fileWriter, 2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        fileWriter.write(MDConstant.CODE_AFTER);

        //刷新缓冲区
        fileWriter.flush();
        //关闭
        fileWriter.close();

        //返回文件
        return file;
    }
}
