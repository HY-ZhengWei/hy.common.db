package org.hy.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.Return;
import org.hy.common.StringHelp;




/**
 * 条件组。即对DBCondition的组合，多个条件顺次判定，直到某个条件为真时返回，其后的条件均不再判定。
 * 当条件组中所有条件均未满足时，返回最后一个条件中设定为假的情况的值。
 * 
 * 因为条件组整体是假的情况（所有条件均不为真时）时，只取条件组最后一个条件中设定的假情况的值，
 * 所以相当于最后条件之前的所有条件的假情况的设定均不生效，这点须注意。
 * 
 * 本类主要用于实现Java编程语言中的 if .. else if ... else ... 的多条件复杂判定。
 * DBCondition只能实现Java编程语言中的 if ... else ... 的条件判定。
 * 
 * XML配置举例：
 * <ConditionGroup>
 *     <name>占位符名称。不含前缀的冒号</name>
 *     <if>条件1</if>
 *     <true>条件1满足时执行的真值</true>
 *     <if>条件2</if>
 *     <true>条件2满足时执行的真值</true>
 *     ... ...
 *     <false>所有条件均不满足时的假值</false>
 * </ConditionGroup>
 * 
 * <if></if><true></true> 可以有无数个，即不限制出现的个数。
 * <false></false>        相当于Java语言中的 else 语法，所以只在最后出现一次即可。
 * 
 * 上面XML配置翻译成Java语言为：
 *     if ( 条件1 )
 *     {
 *         条件1满足时执行的真值
 *     }
 *     else if ( 条件2 )
 *     {
 *         条件2满足时执行的真值
 *     }
 *     else
 *     {
 *         所有条件均不满足时的假值
 *     }
 * 
 * @author      ZhengWei(HY)
 * @createDate  2019-01-19
 * @version     v1.0
 */
public class DBConditions implements Serializable
{

    private static final long serialVersionUID = -5278851854622482181L;
    
    /** 占位符的名称。不包括：冒号。不区分大小写 */
    private String            name;
    
    /** 顺次判定的条件集合 */
    private List<DBCondition> conditions;
    
    
    
    public DBConditions()
    {
        this.conditions = new ArrayList<DBCondition>();
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues)
    {
        return this.getValue(i_ConditionValues ,true);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @return
     */
    public Object getValue(Object i_ConditionValues)
    {
        return this.getValue(i_ConditionValues ,true);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction)
    {
        Return<DBCondition> v_IsPass = this.isPass(i_ConditionValues);
        
        return this.getValue(i_ConditionValues ,i_IsPlaceholderFunction ,v_IsPass);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     */
    public Object getValue(Object i_ConditionValues ,boolean i_IsPlaceholderFunction)
    {
        Return<DBCondition> v_IsPass = this.isPass(i_ConditionValues);
        
        return this.getValue(i_ConditionValues ,i_IsPlaceholderFunction ,v_IsPass);
    }
    
    
    
    /**
     * 通过条件判定后，获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @param i_IsPass                 条件是否通过
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction ,Return<DBCondition> i_IsPass)
    {
        return i_IsPass.paramObj.getValue(i_ConditionValues ,i_IsPlaceholderFunction ,i_IsPass.booleanValue());
    }
    
    
    
    /**
     * 通过条件判定后，获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @param i_IsPass                 条件是否通过
     * @return
     */
    public Object getValue(Object i_ConditionValues ,boolean i_IsPlaceholderFunction ,Return<DBCondition> i_IsPass)
    {
        return i_IsPass.paramObj.getValue(i_ConditionValues ,i_IsPlaceholderFunction ,i_IsPass.booleanValue());
    }
    
    
    
    /**
     * 条件是否通过。
     * 
     * 条件集合中，哪个条件通过返回那个条件，一但前面的条件通过后，其后的条件均不再判定。
     * 当所有条件均未通过时，返回条件集合中的最后一个条件，并设置Return为false。
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues  条件值的集合
     * @return                   Return           表示条件是否通过
     *                           Return.paramObj  表示是哪个条件
     */
    public Return<DBCondition> isPass(Map<String ,?> i_ConditionValues)
    {
        Return<DBCondition> v_Ret       = new Return<DBCondition>(false);
        DBCondition         v_Condition = null;
        
        for (DBCondition v_Item : this.conditions)
        {
            v_Condition = v_Item;
            if ( v_Condition.isPass(i_ConditionValues) )
            {
                v_Ret.set(true);
                break;
            }
        }
        
        return v_Ret.setParamObj(v_Condition);
    }
    
    
    
    /**
     * 条件是否通过。
     * 
     * 条件集合中，哪个条件通过返回那个条件，一但前面的条件通过后，其后的条件均不再判定。
     * 当所有条件均未通过时，返回条件集合中的最后一个条件，并设置Return为false。
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_ConditionValues  条件值的集合
     * @return                   Return           表示条件是否通过
     *                           Return.paramObj  表示是哪个条件
     */
    public Return<DBCondition> isPass(Object i_ConditionValues)
    {
        Return<DBCondition> v_Ret       = new Return<DBCondition>(false);
        DBCondition         v_Condition = null;
        
        for (DBCondition v_Item : this.conditions)
        {
            v_Condition = v_Item;
            if ( v_Condition.isPass(i_ConditionValues) )
            {
                v_Ret.set(true);
                break;
            }
        }
        
        return v_Ret.setParamObj(v_Condition);
    }

    
    
    /**
     * 添加条件，形成多条件判定组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_DBCondition
     */
    public void addCondition(DBCondition i_DBCondition)
    {
        if ( i_DBCondition == null )
        {
            throw new NullPointerException("DBCondition is null.");
        }
        if ( i_DBCondition.getName() == null )
        {
            throw new NullPointerException("DBCondition.getName() is null.");
        }
        
        this.conditions.add(i_DBCondition);
    }
    
    
    
    /**
     * 获取条件的数量
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @return
     */
    public int size()
    {
        return this.conditions.size();
    }
    
    
    
    /**
     * 获取某个位置上的条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_Index
     * @return
     */
    public DBCondition get(int i_Index)
    {
        return this.conditions.get(i_Index);
    }
    
    
    
    /**
     * 删除某个位置上的条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     *
     * @param i_Index
     * @return
     */
    public DBCondition remove(int i_Index)
    {
        return this.conditions.remove(i_Index);
    }
    
    
    
    /**
     * 清空条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-01-19
     * @version     v1.0
     */
    public void clear()
    {
        this.conditions.clear();
    }
    
    
    
    /**
     * 获取：占位符的名称。不包括：冒号。不区分大小写
     */
    public String getName()
    {
        return name;
    }


    
    /**
     * 设置：占位符的名称。不包括：冒号。不区分大小写
     * 
     * @param i_Name 
     */
    public void setName(String i_Name)
    {
        this.name = StringHelp.replaceAll(i_Name ,":" ,"");
        
        if ( Help.isNull(this.conditions) )
        {
            return;
        }
        
        for (DBCondition v_Item : this.conditions)
        {
            v_Item.setName(this.name);
        }
    }
    
    
    
    /**
     * 设置：Fel条件表达式
     * 
     * 形式为带占位符的Fel条件，
     *    如：:c01=='1' && :c02=='2' 
     *    如：:c01==NULL || :c01==''  判定是否为NULL对象或空字符串
     *    
     * 注意：遇到<if>就创建一个新的条件对象，并顺次添加到条件组中。
     *    
     * @param i_Condition 
     */
    public void setIf(String i_Condition)
    {
        this.conditions.add(new DBCondition(this.name ,i_Condition));
    }
    
    
    
    /**
     * 设置：条件满足时的真值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     * 
     * 注意：每次只设置最后一个条件对象的真值。这样才能保证顺次的设置条件组中的多个条件。
     * 
     * @param i_TrueValue 
     */
    public void setTrue(String i_TrueValue)
    {
        if ( Help.isNull(this.conditions) )
        {
            throw new NullPointerException("Please set the <if>...</if> first.");
        }
        
        this.conditions.get(this.conditions.size() - 1).setTrueValue(i_TrueValue);
    }
    
    
    
    /**
     * 设置：条件不满足时的假值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     * 
     * 注意：每次只设置最后一个条件对象的假值。这样才能保证顺次的设置条件组中的多个条件。
     * 
     * @param i_FalseValue
     */
    public void setFalseValue(String i_FalseValue)
    {
        if ( Help.isNull(this.conditions) )
        {
            throw new NullPointerException("Please set the <if>...</if> first.");
        }
        
        this.conditions.get(this.conditions.size() - 1).setTrueValue(i_FalseValue);
    }
    
    
    
    @Override
    public String toString()
    {
        StringBuilder v_Buffer    = new StringBuilder();
        int           v_Size      = this.conditions.size();
        DBCondition   v_Condition = null;
        
        for (int i=0; i<v_Size-1; i++)
        {
            v_Condition = this.conditions.get(i);
            
            if ( i == 0 )
            {
                v_Buffer.append("if ( ");
            }
            else
            {
                v_Buffer.append("else if ( ");
            }
            
            v_Buffer.append(Help.NVL(v_Condition.getCondition()));
            v_Buffer.append(" ) { ");
            v_Buffer.append(v_Condition.getTrue());
            v_Buffer.append(" } ");
        }
        
        if ( v_Size == 1 )
        {
            v_Condition = this.conditions.get(0);
            v_Buffer.append("if ( ");
            v_Buffer.append(Help.NVL(v_Condition.getCondition()));
            v_Buffer.append(" ) { ");
            v_Buffer.append(v_Condition.getTrue());
            v_Buffer.append(" } else ( ");
            v_Buffer.append(v_Condition.getFalse());
            v_Buffer.append(" } ");
        }
        else if ( v_Size >= 2 )
        {
            v_Condition = this.conditions.get(v_Size - 1);
            v_Buffer.append("else if ( ");
            v_Buffer.append(Help.NVL(v_Condition.getCondition()));
            v_Buffer.append(" ) { ");
            v_Buffer.append(v_Condition.getTrue());
            v_Buffer.append(" } else ( ");
            v_Buffer.append(v_Condition.getFalse());
            v_Buffer.append(" } ");
        }
        
        return v_Buffer.toString();
    }
    
}
