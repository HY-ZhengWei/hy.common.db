package org.hy.common.db;

import org.hy.common.Help;
import org.hy.common.PartitionMap;
import org.hy.common.SplitSegment;
import org.hy.common.StringHelp;





/**
 * 分段SQL信息。
 * 
 * 通过 <[ ... ]> 分段的SQL
 * 
 * 同时解释占位符
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 *              v2.0  2014-07-30
 *              v3.0  2014-10-08  placeholders属性为有降序排序顺序的LinkedMap。
 *                                用于解决 :A、:AA 同时存在时的混乱。
 *              v4.0  2015-12-10  支持 :A.B.C 的解释（对点.的解释）。
 *              v5.0  2019-03-13  添加：占位符命名要求严格的规则。
 *                                      规则：占位符的命名，不能是小于等于2位的纯数字
 *                                             防止将类似于时间格式 00:00:00 的字符解释为占位符 
 *              v6.0  2020-06-08  修改：点位符的存储结构改为 TablePartitionLink
 * @createDate  2012-10-30
 */
public class DBSQL_Split extends SplitSegment
{
    
    private static final long serialVersionUID = -8451877528353835210L;

    
    
    /**
     * 占位符信息的集合
     * 
     * placeholders属性为有降序排序顺序的TablePartitionLink。
     *   用于解决 :A、:AA 同时存在时的混乱。
     * 
     * Map.key    为占位符。前缀不包含:符号
     * Map.Value  为占位符的顺序。下标从0开始
     */
    private PartitionMap<String ,Integer> placeholders;
    
    /**
     * 占位符信息的集合（保持占位符原顺序不变）
     * 
     * Map.key    为占位符。前缀不包含:符号
     * Map.Value  为占位符的顺序。下标从0开始
     */
    private PartitionMap<String ,Integer> placeholdersSequence;
    
    
    
    public DBSQL_Split(SplitSegment i_SplitSegment)
    {
        super(i_SplitSegment);
    }
    
    
    
    /**
     * 占位符信息的集合
     * 
     * placeholders属性为有降序排序顺序的TablePartitionLink。
     *   用于解决 :A、:AA 同时存在时的混乱。
     * 
     * Map.key    为占位符。前缀不包含:符号
     * Map.Value  为占位符的顺序。下标从0开始
     */
    public PartitionMap<String ,Integer> getPlaceholders()
    {
        return placeholders;
    }
    
    
    
    /**
     * 占位符信息的集合（保持占位符原顺序不变）
     * 
     * Map.key    为占位符。前缀不包含:符号
     * Map.Value  为占位符的顺序。下标从0开始
     */
    public PartitionMap<String ,Integer> getPlaceholdersSequence()
    {
        return placeholdersSequence;
    }
    
    
    
    /**
     * 解释占位符
     */
    public synchronized void parsePlaceholders()
    {
        if ( Help.isNull(this.info) )
        {
            return;
        }
        
        this.placeholdersSequence = StringHelp.parsePlaceholdersSequence(this.info ,true);
        this.placeholders         = Help.toReverse(this.placeholdersSequence);
    }
    
    
    
    public int getPlaceholderSize()
    {
        if ( this.placeholders == null )
        {
            return 0;
        }
        else
        {
            return this.placeholders.size();
        }
    }
    
}
