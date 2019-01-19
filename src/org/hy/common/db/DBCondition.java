package org.hy.common.db;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StringHelp;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.context.MapContext;





/**
 * 占位符取值条件。当条件满足时，占位符对应的值对被填充应用到SQL语句中。 
 * 
 * 用于动态标识 <[ ... ]>之内的占位符，可以控制动态标识内的SQL段是否拼接到最终执行SQL上执行。 
 * 
 * 占位符X按某个设定的条件取值，当条件满足(True)时取值A，否则取值B。
 * 取值A、B可以如下四种取值类型：
 *      1.  占位符X映射的值 （条件满足时的默认值）；
 *      2.  另一个占位符Y映射的值，须以英文冒号为前缀开头；
 *      3.  常量字符串；
 *      4.  空指针NULL值（条件不满足时的默认值）。
 *      
 * 取值A、B中的占位符及占位符X均支持以下两如高级功能：
 *      1.  支持xx.yy.zz面向对象；
 *      2.  支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-08-03  从XSQLNode中剥离出来，独立后共用。
 * @version     v1.0
 */
public class DBCondition implements Serializable
{

   private static final long serialVersionUID = -6750777160123642579L;
    
    /** 条件满足时，真值的默认值。表示取占位符对应的值 */
    public static final String $DefautlTrueValue  = "THIS";
    
    /** 条件不满足时，假值的默认值。表示空指针，当位于动态标识 <[ ... ]>之内时，此动态标识内的SQL段将不参与整个SQL的执行 */
    public static final String $DefaultFalseValue = "NULL";
    
    /**
     * 表达式引擎的阻断符或是限定符。
     * 阻断符最终将被替换为""空字符。
     * 
     * 用于阻断.点符号。
     * 
     * 如，表达式  :Name.indexOf("B") >= 0 ，:Name.indexOf 也可能被解释为面向对象的属性值获取方法。
     *     而.indexOf("B")是Fel处理的，无须再加工。为了防止歧义，所以要阻断或限定一下，变成下面的样子。
     *     {:Name}.indexOf("B") >= 0
     */
    public static final String []  $Fel_BlockingUp = {"{" ,"}"};
    
    /** 表达式引擎 */
    private static final FelEngine $FelEngine = new FelEngineImpl();
    
    
    
    /** 占位符的名称。不包括：冒号。不区分大小写 */
    private String name;
    
    /** 
     * Fel条件表达式
     * 
     * 形式为带占位符的Fel条件，
     *    如：:c01=='1' && :c02=='2' 
     *    如：:c01==NULL || :c01==''  判定是否为NULL对象或空字符串
     */
    private String condition;
    
    /** 
     * 解释出来的Fel条件。与this.condition的区别是：它是没有占位符
     * 
     *    如：c01=='1' && c02=='2' 
     *    如：c01==NULL || c01==''  判定是否为NULL对象或空字符串
     */
    private String              conditionFel;
    
    /**
     * 占位符信息的集合
     * 
     * Map.key    为占位符。前缀为:符号
     * Map.Value  为占位符原文本信息
     */
    private Map<String ,Object> placeholders;
    
    /** 条件满足时的真值。也可以是另一个占位符，但必须以冒号开头。不区分大小写 */
    private String              trueValue;
    
    /** 条件不满足时的假值。也可以是另一个占位符，但必须以冒号开头。不区分大小写 */
    private String              falseValue;
    
    
    
    public DBCondition()
    {
        this.trueValue  = $DefautlTrueValue;
        this.falseValue = $DefaultFalseValue;
    }
    
    
    
    public DBCondition(String i_Name ,String i_Condition)
    {
        this(i_Name ,i_Condition ,$DefautlTrueValue);
    }
    
    
    
    public DBCondition(String i_Name ,String i_Condition ,String i_TrueValue)
    {
        this(i_Name ,i_Condition ,i_TrueValue ,$DefaultFalseValue);
    }
    
    
    
    public DBCondition(String i_Name ,String i_Condition ,String i_TrueValue ,String i_FalseValue)
    {
        this.setName      (i_Name);
        this.setCondition (i_Condition);
        this.setTrueValue (i_TrueValue);
        this.setFalseValue(i_FalseValue);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_ConditionValues   条件值的集合
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues)
    {
        return getValue(i_ConditionValues ,true);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction)
    {
        try
        {
            if ( this.isPass(i_ConditionValues) )
            {
                return getTrueFalseValue(this.trueValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
            else
            {
                return getTrueFalseValue(this.falseValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 通过条件判定后，获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @param i_IsPass                 条件是否通过
     * @return
     */
    public Object getValue(Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction ,boolean i_IsPass)
    {
        try
        {
            if ( i_IsPass )
            {
                return getTrueFalseValue(this.trueValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
            else
            {
                return getTrueFalseValue(this.falseValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_ConditionValues   条件值的对象
     * @return
     */
    public Object getValue(Object i_ConditionValues)
    {
        return getValue(i_ConditionValues ,true);
    }
    
    
    
    /**
     * 通过条件判定后（条件在此方法内部做判定），获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getValue(Object i_ConditionValues ,boolean i_IsPlaceholderFunction)
    {
        if ( MethodReflect.isExtendImplement(i_ConditionValues ,Map.class) )
        {
            return this.getValue((Map<String ,?>)i_ConditionValues);
        }
        
        try
        {
            if ( this.isPass(i_ConditionValues) )
            {
                return getTrueFalseValue(this.trueValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
            else
            {
                return getTrueFalseValue(this.falseValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 通过条件判定后，获取最终的数值。条件满足时返回真值；条件不满足时返回假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @param i_IsPass                 条件是否通过
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object getValue(Object i_ConditionValues ,boolean i_IsPlaceholderFunction ,boolean i_IsPass)
    {
        if ( MethodReflect.isExtendImplement(i_ConditionValues ,Map.class) )
        {
            return this.getValue((Map<String ,?>)i_ConditionValues ,i_IsPlaceholderFunction ,i_IsPass);
        }
        
        try
        {
            if ( i_IsPass )
            {
                return getTrueFalseValue(this.trueValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
            else
            {
                return getTrueFalseValue(this.falseValue ,i_ConditionValues ,i_IsPlaceholderFunction);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }
    
    
    
    /**
     * 获取条件满足时的真值，或条件不满足时的假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_TrueFalseValue         真值或假值
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getTrueFalseValue(String i_TrueFalseValue ,Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        if ( Help.isNull(i_TrueFalseValue) )
        {
            return i_TrueFalseValue;
        }
        else if ( $DefaultFalseValue.equals(i_TrueFalseValue) )
        {
            return null;
        }
        else if ( $DefautlTrueValue.equals(i_TrueFalseValue) )
        {
            return this.getValueByMap(this.name ,i_ConditionValues ,i_IsPlaceholderFunction);
        }
        else if ( i_TrueFalseValue.startsWith(":") )
        {
            return this.getValueByMap(i_TrueFalseValue.substring(1) ,i_ConditionValues ,i_IsPlaceholderFunction);
        }
        else
        {
            return i_TrueFalseValue;
        }
    }
    
    
    
    /**
     * 获取条件满足时的真值，或条件不满足时的假值。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_TrueFalseValue         真值或假值
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getTrueFalseValue(String i_TrueFalseValue ,Object i_ConditionValues ,boolean i_IsPlaceholderFunction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        if ( Help.isNull(i_TrueFalseValue) )
        {
            return i_TrueFalseValue;
        }
        else if ( $DefaultFalseValue.equals(i_TrueFalseValue) )
        {
            return null;
        }
        else if ( $DefautlTrueValue.equals(i_TrueFalseValue) )
        {
            return this.getValueByObject(this.name ,i_ConditionValues ,i_IsPlaceholderFunction);
        }
        else if ( i_TrueFalseValue.startsWith(":") )
        {
            return this.getValueByObject(i_TrueFalseValue.substring(1) ,i_ConditionValues ,i_IsPlaceholderFunction);
        }
        else
        {
            return i_TrueFalseValue;
        }
    }
    
    
    
    /**
     * 条件是否通过。
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-01-21
     * @version     v1.0
     *
     * @param i_ConditionValues  条件值的集合
     * @return
     */
    public boolean isPass(Map<String ,?> i_ConditionValues)
    {
        if ( Help.isNull(this.condition) 
          || Help.isNull(this.conditionFel)
          || Help.isNull(this.placeholders) )
        {
            return true;
        }
        
        String v_Placeholder = null;
        try
        {
            FelContext v_FelContext = new MapContext();
            
            for (String v_Key : this.placeholders.keySet())
            {
                v_Placeholder = v_Key;
                Object v_Value = getValueByMap(v_Placeholder ,i_ConditionValues ,true);
                
                v_Placeholder = StringHelp.replaceAll(v_Placeholder ,"." ,"_"); // "点" 原本就是Fel关键字，所以要替换 ZhengWei(HY) Add 2017-05-23
                
                v_FelContext.set(v_Placeholder ,v_Value);
            }
            
            return (Boolean) $FelEngine.eval(this.conditionFel ,v_FelContext);
        }
        catch (Exception exce)
        {
            throw new RuntimeException("Fel[" + this.condition + "] Placeholder[" + v_Placeholder + "] is error." + exce.getMessage());
        }
    }
    
    
    
    /**
     * 条件是否通过。
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-06
     * @version     v1.0
     *
     * @param i_ConditionValues  条件值的对象
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isPass(Object i_ConditionValues)
    {
        if ( Help.isNull(this.condition) 
          || Help.isNull(this.conditionFel)
          || Help.isNull(this.placeholders) )
        {
            return true;
        }
        
        if ( MethodReflect.isExtendImplement(i_ConditionValues ,Map.class) )
        {
            return this.isPass((Map<String ,?>)i_ConditionValues);
        }
        
        String v_Placeholder = null;
        try
        {
            FelContext v_FelContext = new MapContext();
            
            for (String v_Key : this.placeholders.keySet())
            {
                v_Placeholder = v_Key;
                Object v_Value = getValueByObject(v_Placeholder ,i_ConditionValues ,true);
                
                v_Placeholder = StringHelp.replaceAll(v_Placeholder ,"." ,"_"); // "点" 原本就是Fel关键字，所以要替换 ZhengWei(HY) Add 2017-05-23
                
                v_FelContext.set(v_Placeholder ,v_Value);
            }
            
            return (Boolean) $FelEngine.eval(this.conditionFel ,v_FelContext);
        }
        catch (Exception exce)
        {
            throw new RuntimeException("Fel[" + this.condition + "] Placeholder[" + v_Placeholder + "] is error." + exce.getMessage());
        }
    }
    
    
    
    /**
     * 获取某一占位符对应的数值
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_Placeholder            占位符名称
     * @param i_ConditionValues        条件值的集合
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getValueByMap(String i_Placeholder ,Map<String ,?> i_ConditionValues ,boolean i_IsPlaceholderFunction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Object v_Value = MethodReflect.getMapValue(i_ConditionValues ,i_Placeholder);
        
        if ( i_IsPlaceholderFunction && v_Value != null )
        {
            if ( MethodReflect.class.equals(v_Value.getClass()) )
            {
                v_Value = ((MethodReflect)v_Value).invoke();
            }
        }
        
        return v_Value; 
    }
    
    
    
    /**
     * 获取某一占位符对应的数值
     * 
     * 占位符支持xx.yy.zz面向对象。
     * 占位符支持函数型占位符。即占位符是一个方法的引用。占位符对应的数值，通过引用方法的返回值获取。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-08
     * @version     v1.0
     *
     * @param i_Placeholder            占位符名称
     * @param i_ConditionValues        条件值的对象
     * @param i_IsPlaceholderFunction  是否取占位符函数的返回值
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getValueByObject(String i_Placeholder ,Object i_ConditionValues ,boolean i_IsPlaceholderFunction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        MethodReflect v_MethodReflect = null;
        
        try
        {
            v_MethodReflect = new MethodReflect(i_ConditionValues ,i_Placeholder ,true ,MethodReflect.$NormType_Getter);
        }
        catch (Exception exce)
        {
            // 有些:xx占位符可能找到对应Java的Getter方法，所以忽略。 ZhengWei(HY) Add 2016-09-29
            // Nothing.
        }
        
        Object v_Value = null;
        
        if ( v_MethodReflect != null )
        {
            v_Value = v_MethodReflect.invoke();
            
            if ( i_IsPlaceholderFunction && v_Value != null )
            {
                if ( MethodReflect.class.equals(v_Value.getClass()) )
                {
                    v_Value = ((MethodReflect)v_Value).invoke();
                }
            }
        }
        
        return v_Value;
    }
    
    
    
    /**
     * 获取：占位符的名称。不包括：冒号。不区分大小写
     */
    public String getName()
    {
        return name;
    }
    

    
    /**
     * 获取：Fel条件表达式
     * 
     * 形式为带占位符的Fel条件，
     *    如：:c01=='1' && :c02=='2' 
     *    如：:c01==NULL || :c01==''  判定是否为NULL对象或空字符串
     */
    public String getCondition()
    {
        return condition;
    }
    

    
    /**
     * 获取：条件满足时的真值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     */
    public String getTrueValue()
    {
        return trueValue;
    }
    

    
    /**
     * 获取：条件不满足时的假值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     */
    public String getFalseValue()
    {
        return falseValue;
    }
    

    
    /**
     * 设置：占位符的名称。不包括：冒号。不区分大小写
     * 
     * @param i_Name 
     */
    public void setName(String i_Name)
    {
        this.name = Help.NVL(i_Name).trim();
    }
    
    
    
    /**
     * setCondition(...) 方法的别名
     *
     * @param i_Condition
     */
    public void setIf(String i_Condition)
    {
        this.setCondition(i_Condition);
    }
    
    
    
    /**
     * getCondition() 方法的别名
     */
    public String getIf()
    {
        return this.getCondition();
    }
    

    
    /**
     * 设置：Fel条件表达式
     * 
     * 形式为带占位符的Fel条件，
     *    如：:c01=='1' && :c02=='2' 
     *    如：:c01==NULL || :c01==''  判定是否为NULL对象或空字符串
     *    
     * @param i_Condition 
     */
    public void setCondition(String i_Condition)
    {
        this.condition    = i_Condition;
        this.conditionFel = i_Condition;
        this.placeholders = null;
        
        if ( !Help.isNull(this.condition) )
        {
            this.placeholders = StringHelp.parsePlaceholders(this.condition);
            
            for (String v_Key : this.placeholders.keySet())
            {
                this.conditionFel = StringHelp.replaceAll(this.conditionFel ,":" + v_Key ,StringHelp.replaceAll(v_Key ,"." ,"_"));
            }
            
            this.conditionFel = StringHelp.replaceAll(this.conditionFel ,$Fel_BlockingUp ,new String[]{""});
        }
    }
    
    
    
    /**
     * setTrueValue(...) 方法的别名
     *
     * @param i_TrueValue
     */
    public void setTrue(String i_TrueValue)
    {
        this.setTrueValue(i_TrueValue);
    }
    
    
    
    /**
     * getTrueValue() 方法的别名
     */
    public String getTrue()
    {
        return this.getTrueValue();
    }
    
    
    
    /**
     * setFalseValue(...) 方法的别名
     *
     * @param i_FalseValue
     */
    public void setFalse(String i_FalseValue)
    {
        this.setFalseValue(i_FalseValue);
    }
    
    
    
    /**
     * getFalseValue() 方法的别名
     */
    public String getFalse()
    {
        return this.getFalseValue();
    }
    

    
    /**
     * 设置：条件满足时的真值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     * 
     * @param i_TrueValue 
     */
    public void setTrueValue(String i_TrueValue)
    {
        this.trueValue = i_TrueValue;
        
        if ( i_TrueValue != null )
        {
            if ( i_TrueValue.trim().startsWith(":") )
            {
                this.trueValue = i_TrueValue.trim();
            }
            else if ( $DefautlTrueValue.equalsIgnoreCase(i_TrueValue.trim()) )
            {
                this.trueValue = $DefautlTrueValue;
            }
            else if ( $DefautlTrueValue.equalsIgnoreCase(i_TrueValue.trim()) )
            {
                this.trueValue = $DefautlTrueValue;
            }
        }
    }
    

    
    /**
     * 设置：条件不满足时的假值。也可以是另一个占位符，但必须以冒号开头。不区分大小写
     * 
     * @param i_FalseValue 
     */
    public void setFalseValue(String i_FalseValue)
    {
        this.falseValue = i_FalseValue;
        
        if ( i_FalseValue != null )
        {
            if ( i_FalseValue.trim().startsWith(":") )
            {
                this.falseValue = i_FalseValue.trim();
            }
            else if ( $DefautlTrueValue.equalsIgnoreCase(i_FalseValue.trim()) )
            {
                this.falseValue = $DefautlTrueValue;
            }
            else if ( $DefautlTrueValue.equalsIgnoreCase(i_FalseValue.trim()) )
            {
                this.falseValue = $DefautlTrueValue;
            }
        }
    }



    @Override
    public String toString()
    {
        return "if ( " + Help.NVL(this.condition) + " ) { " + this.trueValue + " } else { " + this.falseValue + " }";
    }
    
}
