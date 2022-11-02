package org.hy.common.db;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





/**
 * 解释圆括号SQL语句。
 * 
 * 将SQL语句中的所有圆括号SQL及圆括号的层次解释出来
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-12-06
 */
public class DBSQL_RoundBrackets
{
	/** 圆括号SQL语句 */
	private String                     sqlPart;
	
	/** 将this.sqlPart中的圆括号替换成方括号的SQL语句 */
	private String                     sqlPart_SquareBrackets;
	
	/** 相对于完整SQL语句this.sqlPart的开始位置 */
	private int                        sql_StartPosition;
	
	/** 相对于完整SQL语句this.sqlPart的结束位置 */
	private int                        sql_EndPosition;
	
	/** this.sqlPart中包含的圆括号SQL语句 */
	private List<DBSQL_RoundBrackets>  childs;
	
	
	
	/**
	 * 解释圆括号SQL语句。
	 * 
	 * 将SQL语句中的所有圆括号SQL及圆括号的层次解释出来
	 * 
	 * @param i_SQLText
	 * @return
	 */
	public static List<DBSQL_RoundBrackets> parse(String i_SQLText)
	{
		List<DBSQL_RoundBrackets> v_DBSQL_RBList = new ArrayList<DBSQL_RoundBrackets>();
		List<DBSQL_RoundBrackets> v_Ret          = new ArrayList<DBSQL_RoundBrackets>();
		
		
		parse(i_SQLText ,v_DBSQL_RBList);
		
		
		// 整理层级
		for (int x=0; x<v_DBSQL_RBList.size(); x++)
		{
			DBSQL_RoundBrackets v_RB        = v_DBSQL_RBList.get(x);
			boolean             v_IsInclude = false;
			
			for (int y=x+1; y<v_DBSQL_RBList.size() && !v_IsInclude; y++)
			{
				DBSQL_RoundBrackets v_RBTemp = v_DBSQL_RBList.get(y);
				
				if ( v_RB.getSQL_StartPosition() > v_RBTemp.getSQL_StartPosition() && v_RB.getSQL_EndPosition() < v_RBTemp.getSQL_EndPosition()  )
				{
					v_RBTemp.addChild(v_RB);
					
					v_IsInclude = true;
				}
			}
			
			
			if ( !v_IsInclude )
			{
				v_Ret.add(v_RB);
			}
			
			v_IsInclude = false;
		}
		
		
		return v_Ret;
	}
	
	
	
	/**
	 * 将SQL语句中的所有圆括号SQL解释出来(递归解释)
	 * 
	 * @param i_SQLText
	 * @param io_DBSQL_RBList
	 */
	private static void parse(String i_SQLText ,List<DBSQL_RoundBrackets> io_DBSQL_RBList)
	{
		String  v_SQLText        = i_SQLText;
		Pattern v_Pattern        = Pattern.compile("\\([\\s \\u0021-\\u0027 \\u002A-\\uFFFF]+\\)");  // 匹配圆括号
		Matcher v_Matcher        = v_Pattern.matcher(v_SQLText);
		int     v_MatcheEndIndex = 0;
		
		
		while ( v_Matcher.find() )
		{
			int    v_StartIndex = v_Matcher.start();
			int    v_EndIndex   = v_Matcher.end();
			String v_MetcheStr  = v_SQLText.substring(v_StartIndex ,v_EndIndex);
			
			DBSQL_RoundBrackets v_DBSQL_RB = new DBSQL_RoundBrackets(v_MetcheStr ,v_StartIndex ,v_EndIndex);
			
			io_DBSQL_RBList.add(v_DBSQL_RB);
			
			v_MatcheEndIndex = v_EndIndex;
			
			v_SQLText = v_SQLText.substring(0 ,v_StartIndex) 
			          + v_DBSQL_RB.getSQLPart_SquareBrackets()
			          + v_SQLText.substring(v_EndIndex); 
		}
		
		
		if ( v_MatcheEndIndex == 0 )
		{
			return;
		}
		else
		{
			parse(v_SQLText ,io_DBSQL_RBList);
		}
	}
	
	
	
	/**
	 * 反向。
	 * 
	 * 对解释出的圆括号SQL语句重新拼接成完整的SQL
	 * 
	 * @param i_SQLText  原始完整的SQL
	 * @param i_RBList
	 * @return
	 */
	public static String reverse(String i_SQLText ,List<DBSQL_RoundBrackets> i_RBList)
	{
		String v_Ret = new String(i_SQLText);
		
		for (int i=0; i<i_RBList.size(); i++)
		{
			DBSQL_RoundBrackets v_RB = i_RBList.get(i);
			
			v_Ret = v_Ret.substring(0 ,v_RB.getSQL_StartPosition())
			      + v_RB.getSQLFull()
			      + v_Ret.substring(v_RB.getSQL_EndPosition());
		}
		
		return v_Ret;
	}
	
	
	
	/**
	 * 私有的构造器
	 * 
	 * @param i_SQLPart
	 * @param i_SQL_StartPosition
	 * @param i_SQL_EndPosition
	 */
	private DBSQL_RoundBrackets(String i_SQLPart ,int i_SQL_StartPosition ,int i_SQL_EndPosition)
	{
		this.childs            = new ArrayList<DBSQL_RoundBrackets>();
		this.sqlPart           = i_SQLPart;
		this.sql_StartPosition = i_SQL_StartPosition;
		this.sql_EndPosition   = i_SQL_EndPosition;
		
		this.sqlPart_SquareBrackets = "["
						            + this.sqlPart.substring(1  ,this.sqlPart.length() - 1)
						            + "]";
	}
	
	
	
	public int getChildSize()
	{
		return this.childs.size();
	}
	
	
	
	public DBSQL_RoundBrackets getChild(int i_Index)
	{
		if ( i_Index < 0 || i_Index >= this.childs.size() )
		{
			throw new ArrayIndexOutOfBoundsException("DBSQL_RoundBrackets.getChild(int) is error.");
		}
		
		return this.childs.get(i_Index);
	}
	
	
	
	public void addChild(DBSQL_RoundBrackets i_RB)
	{
		if ( i_RB == null )
		{
			throw new NullPointerException("DBSQL_RoundBrackets is null.");
		}
		
		this.childs.add(i_RB);
	}
	
	
	
	public List<DBSQL_RoundBrackets> getChilds()
	{
		return this.childs;
	}
	
	
	
	public int getSQL_EndPosition() 
	{
		return sql_EndPosition;
	}
	
	
	
	public int getSQL_StartPosition() 
	{
		return sql_StartPosition;
	}
	
	
	
	public String getSQLPart() 
	{
		return sqlPart;
	}
	
	
	
	public String getSQLPart_SquareBrackets()
	{
		return this.sqlPart_SquareBrackets;
	}
	
	
	
	/**
	 * 对解释出的圆括号SQL语句重新拼接成一个大的SQL
	 * 
	 * @return
	 */
	public String getSQLFull()
	{
		String v_SQLFull = new String(this.sqlPart);
		
		for (int i=this.childs.size() - 1; i>=0; i--)
		{
			DBSQL_RoundBrackets v_RB = this.childs.get(i);
			
			v_SQLFull = v_SQLFull.substring(0 ,v_RB.getSQL_StartPosition() - this.getSQL_StartPosition())
			          + v_RB.getSQLFull()
			          + v_SQLFull.substring(v_RB.getSQL_EndPosition() - this.getSQL_StartPosition());
		}
		
		return v_SQLFull;
	}
	
	
	
	/**
	 * 对解释出的圆括号SQL语句重新拼接成一个已方括号SQL语句的大SQL
	 * 
	 * @return
	 */
	public String getSQLFull_SquareBrackets()
	{
		String v_SQLFull = new String(this.sqlPart_SquareBrackets);
		
		for (int i=this.childs.size() - 1; i>=0; i--)
		{
			DBSQL_RoundBrackets v_RB = this.childs.get(i);
			
			v_SQLFull = v_SQLFull.substring(0 ,v_RB.getSQL_StartPosition() - this.getSQL_StartPosition())
			          + v_RB.getSQLFull_SquareBrackets()
			          + v_SQLFull.substring(v_RB.getSQL_EndPosition() - this.getSQL_StartPosition());
		}
		
		return v_SQLFull;
	}
	
	
	
	/**
	 * 对解释出的圆括号SQL语句重新拼接成一个已方括号SQL语句的大SQL
	 * 
	 * 但，只对包含的子圆括号SQL进行方括号的转换
	 * 
	 * @return
	 */
	public String getSQLFull_SquareBracketsOnlyChilds()
	{
		String v_SQLFull = new String(this.sqlPart);
		
		for (int i=this.childs.size() - 1; i>=0; i--)
		{
			DBSQL_RoundBrackets v_RB = this.childs.get(i);
			
			v_SQLFull = v_SQLFull.substring(0 ,v_RB.getSQL_StartPosition() - this.getSQL_StartPosition())
			          + v_RB.getSQLFull_SquareBrackets()
			          + v_SQLFull.substring(v_RB.getSQL_EndPosition() - this.getSQL_StartPosition());
		}
		
		return v_SQLFull;
	}
	
	
	
	public static void main(String args[])
	{
	    StringBuilder v_SQL = new StringBuilder();
		
		v_SQL.append("SELECT  * From (");
		v_SQL.append("SELECT  * ");
		v_SQL.append("  FROM  Dual");
		v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  Statue    = :Statue").append("\n");
		v_SQL.append("   AND  UserName  = ':UserName'");
		v_SQL.append("   AND  CardNo    = ':CardNo'");
		v_SQL.append("   AND  CityCode  = 0910");
		v_SQL.append(") HY");
		
		List<DBSQL_RoundBrackets> v_RBList = DBSQL_RoundBrackets.parse(v_SQL.toString());
		
		String v_ReverseSQL = DBSQL_RoundBrackets.reverse(v_SQL.toString() ,v_RBList);
		
		System.out.println(v_ReverseSQL);
	}
	
}
