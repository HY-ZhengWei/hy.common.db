package org.hy.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.Return;




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
 * @author      ZhengWei(HY)
 * @createDate  2019-01-19
 * @version     v1.0
 */
public class DBConditions implements Serializable
{

    private static final long serialVersionUID = -5278851854622482181L;
    
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
            v_Buffer.append(Help.NVL(v_Condition.getCondition()));
            v_Buffer.append(" ) { ");
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
            v_Buffer.append(Help.NVL(v_Condition.getCondition()));
            v_Buffer.append(" ) { ");
            v_Buffer.append(v_Condition.getFalse());
            v_Buffer.append(" } ");
        }
        
        return v_Buffer.toString();
    }
    
}
