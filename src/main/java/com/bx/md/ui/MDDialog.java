package com.bx.md.ui;

import com.bx.md.constant.MDConstant;
import com.bx.md.utils.MDUtils;
import com.bx.md.utils.ProjectUtils;
import com.intellij.database.model.DasTable;
import com.intellij.openapi.ui.DialogWrapper;
//import com.youbenzi.md2.export.FileFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MDDialog extends DialogWrapper {
    /**
     * 主面板
     */
    private JPanel mainPanel;

    private List<DasTable> tables;

    /**
     * 布局块
     */
    private JPanel jPanelTop = new JPanel();
    private JPanel jPanelDown = new JPanel();

    /**
     * 导出类型按钮
     */
    private JCheckBox mysql,osrdb,md;

    /**
     * 保存路径
     */
    private JLabel label = new JLabel("保存路径:");
    private JTextField path = new JTextField(); // 创建一个单行输入框
    /**
     * 是否是第一次进入
     */
    private boolean flag = false;


    /**
     * 初始化
     */
    public MDDialog(List<DasTable> tables) {
        super(ProjectUtils.getCurrProject());
        //装载数据
        this.tables = tables;
        //创建主面板
        this.mainPanel = new JPanel(new BorderLayout(3,1));
        //初始化方法
        this.initPanel();
    }


    private void initPanel() {
        //初始化
        init();

        //非第一次跳出
        if(flag) return;

        this.mysql = new JCheckBox(MDConstant.MYSQL);
        this.osrdb = new JCheckBox(MDConstant.OSRDB);
        this.md = new JCheckBox(MDConstant.MD);

        this.mysql.setLocale(Locale.forLanguageTag(BorderLayout.CENTER));
        this.osrdb.setLocale(Locale.forLanguageTag(BorderLayout.CENTER));
        this.md.setLocale(Locale.forLanguageTag(BorderLayout.CENTER));
//        //默认true
        this.md.setSelected(true);
        this.mysql.setSelected(true);
        this.osrdb.setSelected(true);
//        this.md.setEnabled(false);

        //上面布局设置
        this.jPanelTop.add(this.mysql);
        this.jPanelTop.add(this.osrdb);
        this.jPanelTop.add(this.md);
        this.jPanelTop.setLocale(Locale.forLanguageTag(BorderLayout.BEFORE_FIRST_LINE));


        //下面布局设置 禁止输入
        this.path.setText(new File("").getAbsolutePath());
        this.path.setEnabled(false);
        //设置选择路径
        this.path.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jf = new JFileChooser();
                jf.setVisible(true);
                jf.setDialogTitle("选择文件");
                jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = jf.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    path.setText(jf.getSelectedFile().getAbsolutePath());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        this.jPanelDown.add(this.label);
        this.jPanelDown.add(this.path);
        this.jPanelTop.setLocale(Locale.forLanguageTag(BorderLayout.AFTER_LAST_LINE));


        this.jPanelDown.add(this.jPanelTop);
        this.mainPanel.add(this.jPanelDown);
        this.mainPanel.setAlignmentX(SwingConstants.CENTER); //水平居中
        this.mainPanel.setAlignmentY(SwingConstants.CENTER); //垂直居中
        this.mainPanel.setMinimumSize(new Dimension(600, 300));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }


    @Override
    protected void doOKAction() {
        try {
            String filePath = this.path.getText();
            if(this.md.isSelected()){
                //生成md文件
                new MDUtils().exportFile(this.tables, filePath);
            }
            //生成Mysql文件
            if(this.mysql.isSelected()){
                new MDUtils().mysqlSql(this.tables, filePath);
            }
            //生成神通数据库文件
            if(this.osrdb.isSelected()){
                new MDUtils().OsrdbSql(this.tables, filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //按钮OK
        super.doOKAction();
    }
}
