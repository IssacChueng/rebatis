package cn.jeff.study.applyer;

import cn.jeff.study.util.CollectionUtils;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Mapper 文件中的sql节点,转换成Node存放在 {@link Configuration#getSqlFragments()} 中
 *
 * @author swzhang
 * @date 2019/10/10
 */
public class SqlApplyer extends BaseApplyer {

    private Map<String, XNode> sqlFragments;

    public SqlApplyer(Configuration configuration, XNode mapperNode, String namespace) {
        super(configuration, mapperNode, namespace);
    }

    @Override
    protected void preApply() {

    }

    @Override
    protected void postApply() {

    }

    @Override
    protected void doApply() {
        applySql();
    }

    public void applySql() {
        sqlFragments = configuration.getSqlFragments();
        List<XNode> sqlNodes = mapperNode.evalNodes("/mapper/sql");

        if (CollectionUtils.isNotEmpty(sqlNodes)) {
            sqlNodes.forEach(sqlNode -> {
                String databaseId = sqlNode.getStringAttribute("databaseId");
                String id = sqlNode.getStringAttribute("id");
                id = builderAssistant.applyCurrentNamespace(id, false);
                if (databaseIdMatchesCurrent(id, databaseId, configuration.getDatabaseId())) {
                    //防止configuration中已经存在相同id
                    sqlFragments.remove(id);
                    sqlFragments.put(id, sqlNode);
                }
            });
        }
    }

    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }
}
