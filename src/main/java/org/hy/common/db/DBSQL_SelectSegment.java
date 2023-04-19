package org.hy.common.db;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.StringHelp;





/**
 * 解释Select语句。
 * 
 * 将SQL语句中的所有Select语句及层次解释出来
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2012-12-11
 */
public class DBSQL_SelectSegment
{

    /** Select语句的SQL */
    private String                     selectSQL;
    
    private DBSQL_RoundBrackets        rb;
    
    /** this.selectSQL中包含的Select语句 */
    private List<DBSQL_SelectSegment>  childs;
    
    
    
    public static List<DBSQL_SelectSegment> parse(String i_SQLText)
    {
        List<DBSQL_RoundBrackets> v_RBList = DBSQL_RoundBrackets.parse(i_SQLText);
        List<DBSQL_SelectSegment> v_SSList = DBSQL_SelectSegment.parse(v_RBList);
        
        return v_SSList;
    }
    
    
    
    private static List<DBSQL_SelectSegment> parse(List<DBSQL_RoundBrackets> i_RBList)
    {
        List<DBSQL_SelectSegment> v_SSList = new ArrayList<DBSQL_SelectSegment>();
        List<DBSQL_SelectSegment> v_Ret    = new ArrayList<DBSQL_SelectSegment>();
        
        
        parse(i_RBList ,v_SSList);
        
        
        // 整理层级
        for (int x=v_SSList.size() - 1; x>=0; x--)
        {
            DBSQL_SelectSegment v_SS        = v_SSList.get(x);
            boolean             v_IsInclude = false;
            
            for (int y=x-1; y>=0 && !v_IsInclude; y--)
            {
                DBSQL_SelectSegment v_SSTemp = v_SSList.get(y);
                
                if ( v_SS.getSQL_StartPosition() > v_SSTemp.getSQL_StartPosition() && v_SS.getSQL_EndPosition() < v_SSTemp.getSQL_EndPosition()  )
                {
                    v_SSTemp.getChilds().add(v_SS);
                    
                    v_IsInclude = true;
                }
            }
            
            
            if ( !v_IsInclude )
            {
                v_Ret.add(v_SS);
            }
            
            v_IsInclude = false;
        }
        
        
        return v_Ret;
    }
    
    
    
    private static void parse(List<DBSQL_RoundBrackets> i_RBList ,List<DBSQL_SelectSegment> io_SSList)
    {
        for (int i=0; i<i_RBList.size(); i++)
        {
            DBSQL_RoundBrackets v_RB = i_RBList.get(i);
            
            String  v_SQLText = v_RB.getSQLFull_SquareBracketsOnlyChilds();
            Pattern v_Pattern = Pattern.compile("\\([\\s ]*[Ss][Ee][Ll][Ee][Cc][Tt][\\s ]+[\\s \\u0021-\\u0027 \\u002A-\\uFFFF]+\\)");  // 匹配"(Select ...)"形式
            Matcher v_Matcher = v_Pattern.matcher(v_SQLText);
            
            if ( v_Matcher.find() )
            {
                DBSQL_SelectSegment v_SS = new DBSQL_SelectSegment(v_RB);
                
                io_SSList.add(v_SS);
            }
            
            if ( v_RB.getChildSize() >= 1 )
            {
                parse(v_RB.getChilds() ,io_SSList);
            }
        }
    }
    
    
    
    /**
     * 反向。
     * 
     * 对解释出的Select语句重新拼接成完整的SQL
     * 
     * @param i_SQLText  原始完整的SQL
     * @param i_SSList
     * @return
     */
    public static String reverse(String i_SQLText ,List<DBSQL_SelectSegment> i_SSList)
    {
        String v_Ret = new String(i_SQLText);
        
        for (int i=0; i<i_SSList.size(); i++)
        {
            DBSQL_SelectSegment v_SS = i_SSList.get(i);
            
            v_Ret = v_Ret.substring(0 ,v_SS.getSQL_StartPosition())
                  + v_SS.getSelectSQL()
                  + v_Ret.substring(v_SS.getSQL_EndPosition());
        }
        
        return v_Ret;
    }
    
    
    
    private DBSQL_SelectSegment(DBSQL_RoundBrackets i_RB)
    {
        this.childs    = new ArrayList<DBSQL_SelectSegment>();
        this.rb        = i_RB;
        this.selectSQL = this.rb.getSQLFull();
        
        this.parse_SelectToFrom();
    }
    
    
    
    /**
     * 解释Select语句中Select到From之间的内容
     */
    private void parse_SelectToFrom()
    {
        if ( this.rb == null )
        {
            return;
        }
        
        String v_SQL = this.selectSQL;
        
        for (int i=0; i<this.rb.getChildSize(); i++)
        {
            DBSQL_RoundBrackets v_RB = this.rb.getChild(i);
            
            v_SQL = v_SQL.substring(0 ,v_RB.getSQL_StartPosition() - this.getSQL_StartPosition())
                  + "["
                  + StringHelp.lpad(" " ,v_RB.getSQLPart().length() - 2 ," ")
                  + "]"
                  + v_SQL.substring(v_RB.getSQL_EndPosition() - this.getSQL_StartPosition());
        }
        
        
        System.out.println(v_SQL);
    }
    
    
    
    public List<DBSQL_SelectSegment> getChilds()
    {
        return childs;
    }
    
    
    
    public String getSelectSQL()
    {
        return this.selectSQL;
    }
    
    
    
    public int getSQL_EndPosition()
    {
        return this.rb.getSQL_EndPosition();
    }
    
    
    
    public int getSQL_StartPosition()
    {
        return this.rb.getSQL_StartPosition();
    }
    
    
    
    public static void main(String args[])
    {
        StringBuilder v_SQL = new StringBuilder();
        
        v_SQL.append("SELECT  * From (");
        v_SQL.append("SELECT  AcceptTime ,Logic_No ,UserName");
        v_SQL.append("  FROM  Dual");
        v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  Statue    = :Statue").append("\n");
        v_SQL.append("   AND  UserName  = ':UserName'");
        v_SQL.append("   AND  CardNo    = ':CardNo'");
        v_SQL.append("   AND  Logic_No  = (Select Logic_No From T_C_PUB_Product A Where RowNum = 1)");
        v_SQL.append("   AND  CityCode  = 0910");
        v_SQL.append(") HY");
        
        List<DBSQL_SelectSegment> v_SSList = DBSQL_SelectSegment.parse(v_SQL.toString());
        
        String v_ReverseSQL = DBSQL_SelectSegment.reverse(v_SQL.toString() ,v_SSList);
        
        System.out.println(v_ReverseSQL);
    }
    
}
