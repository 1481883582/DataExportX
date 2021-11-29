package com.bx.md.actions;

import com.bx.md.ui.MDDialog;
import com.bx.md.utils.CacheDataUtils;
import com.bx.md.utils.Strings;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作按钮分组
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Slf4j
public class MainActionGroup extends ActionGroup {
    /**
     * 单个表
     */
    private static AnAction mdAnAction;

    /**
     * 多个表
     */
    private static AnAction mdAllAnAction;
    /**
     * 缓存数据工具类
     */
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();

    /**
     * 是否不存在子菜单
     */
    private boolean notExistsChildren;

    /**
     * 是否分组按钮
     *
     * @return 是否隐藏
     */
    @Override
    public boolean hideIfNoVisibleChildren() {
        return this.notExistsChildren;
    }


    /**
     * 根据右键在不同的选项上展示不同的子菜单
     *
     * @param event 事件对象
     * @return 动作组
     */
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent event) {
        // 获取当前项目
        Project project = getEventProject(event);
        if (project == null) {
            return getEmptyAnAction();
        }

        //获取选中的PSI元素
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        DbTable selectDbTable = null;
        if (psiElement instanceof DbTable) {
            selectDbTable = (DbTable) psiElement;
        }
        if (selectDbTable == null) {
            return getEmptyAnAction();
        }
        //获取选中的所有表
        PsiElement[] psiElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return getEmptyAnAction();
        }
        List<DbTable> dbTableList = new ArrayList<>();
        for (PsiElement element : psiElements) {
            if (!(element instanceof DbTable)) {
                continue;
            }
            DbTable dbTable = (DbTable) element;
            dbTableList.add(dbTable);
        }
        if (dbTableList.isEmpty()) {
            return getEmptyAnAction();
        }

        //保存数据到缓存
        cacheDataUtils.setDbTableList(dbTableList);
        cacheDataUtils.setSelectDbTable(selectDbTable);
        this.notExistsChildren = false;
        return getMenuList();
    }

    /**
     * 初始化注册子菜单项目
     *
     * @return 子菜单数组
     */
    private AnAction[] getMenuList() {

        //导出 .md文件
        if(Strings.isEmpty(mdAnAction)) mdAnAction = new AnAction("Export MD File", "导出当前表的md文件", AllIcons.ToolbarDecorator.Export) {

            @SneakyThrows
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

                //创建承载List
                ArrayList<DasTable> dbTables = new ArrayList<>();

                //加入要打印的Table
                dbTables.add(CacheDataUtils.getInstance().getSelectDbTable());

                //显示弹框
                new MDDialog(dbTables).show();
            }
        };

        //导出 .md文件
        if(Strings.isEmpty(mdAllAnAction)) mdAllAnAction = new AnAction("Export MD File ALL", "导出所有表的md文件", AllIcons.ToolbarDecorator.Export) {

            @SneakyThrows
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

                //生成数据
                List<DasTable> dasTables = (List<DasTable>) DasUtil.getTables(CacheDataUtils.getInstance().getSelectDbTable().getDataSource()).toList();

                //显示弹框
                new MDDialog(dasTables).show();
            }
        };

        //加入所有菜单
        // 返回所有菜单
        return new AnAction[]{mdAnAction, mdAllAnAction};
    }


    /**
     * 获取空菜单组
     *
     * @return 空菜单组
     */
    private AnAction[] getEmptyAnAction() {
        this.notExistsChildren = true;
        return AnAction.EMPTY_ARRAY;
    }
}
