package org.hy.common.db.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.StringHelp;
import org.hy.common.xml.log.Logger;





public class JU_SQLParser
{
    private static final Logger $Logger = new Logger(JU_SQLParser.class ,true);
    
    
    
    public final static String [] $KEYS = {
                                           "SELECT"
                                          ,"FROM"
                                          ,"WHERE"
                                          ,"AND"
                                          ,"OR"
                                          ,"GROUP BY"
                                          ,"HAVING"
                                          ,"ORDER BY"
                                          };
    
    public static List<String>    $KEYS_PATTERN = new ArrayList<String>();
    
    private List<DBSQL_Unit>      sqlUnitList;
    
    private String                sqlFrame;
    
    
    
    static
    {
        for (int i=0; i<$KEYS.length; i++)
        {
            String        v_Key    = $KEYS[i];
            StringBuilder v_Buffer = new StringBuilder();
            
            v_Buffer.append("[\\( ]+");
            
            for (int c=0; c<v_Key.length(); c++)
            {
                String v_OneChar = v_Key.substring(c ,c+1);
                
                if ( " ".equals(v_OneChar) )
                {
                    v_Buffer.append("[ ]+");
                }
                else
                {
                    v_Buffer.append("[");
                    v_Buffer.append(v_OneChar.toUpperCase());
                    v_Buffer.append(v_OneChar.toLowerCase());
                    v_Buffer.append("]");
                }
            }
            
            v_Buffer.append("[\\) ]+");
            
            $KEYS_PATTERN.add(v_Buffer.toString());
        }
    }
    
    
    
    public static void verify()
    {
        for (int i=0; i<$KEYS_PATTERN.size(); i++)
        {
            $Logger.info($KEYS_PATTERN.get(i));
        }
    }
    
    
    
    public JU_SQLParser()
    {
        this.sqlUnitList = new ArrayList<DBSQL_Unit>();
    }
    
    
    
    public void hy(String i_SQLText)
    {
        String  v_SQLText        = i_SQLText;
        Pattern v_Pattern        = Pattern.compile("\\([\\s ]*[Ss][Ee][Ll][Ee][Cc][Tt][ ]+[\\w\\s\\-\\[\\]|*$'+=%:, ]+\\)");
        Matcher v_Matcher        = v_Pattern.matcher(v_SQLText);
        int     v_MatcheEndIndex = 0;
        
        while ( v_Matcher.find() )
        {
            int    v_StartIndex = v_Matcher.start();
            int    v_EndIndex   = v_Matcher.end();
            String v_MetcheStr  = v_SQLText.substring(v_StartIndex ,v_EndIndex);
            
            // $Logger.info(v_SQLText.substring(v_MatcheEndIndex ,v_StartIndex));
            // $Logger.info("Match [" + v_MetcheStr + "] at positions " + v_StartIndex + "-" + v_EndIndex);
            
            this.sqlUnitList.add(new DBSQL_Unit(v_MetcheStr ,v_StartIndex ,v_EndIndex));
            
            v_SQLText = v_SQLText.substring(0 ,v_StartIndex)
                      + StringHelp.rpad("[" ,v_MetcheStr.length() - 1 ," ")
                      + "]"
                      + v_SQLText.substring(v_EndIndex);
            
            // $Logger.info(v_SQLText);
            
            v_MatcheEndIndex = v_EndIndex;
        }
        
        if ( v_MatcheEndIndex > 0 && v_MatcheEndIndex < v_SQLText.length() )
        {
            // $Logger.info(v_SQLText.substring(v_MatcheEndIndex));
        }
        
        if ( v_MatcheEndIndex > 0 )
        {
            this.hy(v_SQLText);
        }
        else
        {
            this.sqlFrame = v_SQLText;
        }
    }
    
    
    
    public String hy_Union()
    {
        String v_RetSQL = this.sqlFrame;
        
        for (int i=this.sqlUnitList.size()-1; i>=0; i--)
        {
            DBSQL_Unit v_SQLUnit = this.sqlUnitList.get(i);
            
            v_RetSQL = v_RetSQL.substring(0 ,v_SQLUnit.getStartPosition())
                     + v_SQLUnit.getSQL()
                     + v_RetSQL.substring(v_SQLUnit.getEndPosition());
        }
        
        return v_RetSQL;
    }
    
    
    
    public void hy_Verify()
    {
        $Logger.info(this.sqlFrame);
        
        for (int i=this.sqlUnitList.size()-1; i>=0; i--)
        {
            DBSQL_Unit v_SQLUnit = this.sqlUnitList.get(i);
            String     v_Spaces  = StringHelp.rpad(" " ,v_SQLUnit.getStartPosition() ," ");
            
            for (int x=0; x<v_SQLUnit.getSQLSegments().size(); x++)
            {
                DBSQL_Unit_Segment v_Segment = v_SQLUnit.getSQLSegments().get(x);
                
                if ( v_Segment.getStartPosition() == 0 )
                {
                    $Logger.info(v_Spaces + v_Segment.getSQLSegment());
                }
                else
                {
                    $Logger.info(v_Spaces + StringHelp.rpad(" " ,v_Segment.getStartPosition() ," ") + v_Segment.getSQLSegment());
                }
            }
        }
    }
    
    
    
    public static void main(String [] args)
    {
        StringBuilder v_SQL = new StringBuilder();
        
        v_SQL.append("SELECT  * ");
        v_SQL.append("  FROM  Dual");
        v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  Statue    = :Statue").append("\n");
        v_SQL.append("   AND  UserName  = ':UserName'");
        v_SQL.append("   AND  CardNo    = ':CardNo'");
        v_SQL.append("   AND  CityCode  = 0910");
        
        JU_SQLParser v_JU_SQLParser = new JU_SQLParser();
        
        v_JU_SQLParser.hy("Select * From (" + v_SQL.toString() + ") HY");
        //v_QQ.hy("Select * From (Select * From (Select * From v$log_history where 1=1 ) B) A Where exists (Select 1 From XX) ");
        
        $Logger.info(v_JU_SQLParser.hy_Union());
        
        v_JU_SQLParser.hy_Verify();
    }
    
}





class DBSQL_Unit
{
    private String                     sql;
    
    private int                        startPosition;
    
    private int                        endPosition;
    
    private List<DBSQL_Unit_Segment>   sqlSegments;
    
    
    
    public DBSQL_Unit(String i_SQL ,int i_StartPosition ,int i_EndPosition)
    {
        this.sql           = i_SQL;
        this.startPosition = i_StartPosition;
        this.endPosition   = i_EndPosition;
        this.sqlSegments   = new ArrayList<DBSQL_Unit_Segment>();
        
        this.parser();
    }
    
    
    
    private void parser()
    {
        int v_FindStartIndex = 0;
        
        while ( v_FindStartIndex < this.sql.length() && v_FindStartIndex >= 0 )
        {
            DBSQL_Key v_TempKey = this.findNextKeyStartIndex(v_FindStartIndex);
            
            if ( v_TempKey == null )
            {
                this.sqlSegments.add(new DBSQL_Unit_Segment(this.sql.substring(v_FindStartIndex) ,false ,v_FindStartIndex));
                return;
            }
            
            if ( v_TempKey.getStartPosition() == v_FindStartIndex )
            {
                this.sqlSegments.add(new DBSQL_Unit_Segment(v_TempKey.getKey() ,true ,v_TempKey.getStartPosition()));
            }
            else
            {
                this.sqlSegments.add(new DBSQL_Unit_Segment(this.sql.substring(v_FindStartIndex ,v_TempKey.getStartPosition()) ,false ,v_FindStartIndex));
                this.sqlSegments.add(new DBSQL_Unit_Segment(v_TempKey.getKey() ,true ,v_TempKey.getStartPosition()));
            }
            
            v_FindStartIndex = v_TempKey.getEndPosition();
        }
    }
    
    
    
    private DBSQL_Key findNextKeyStartIndex(int i_FindStartIndex)
    {
        DBSQL_Key v_RetKey = null;
        
        for (int v_KeyIndex=0; v_KeyIndex<JU_SQLParser.$KEYS_PATTERN.size(); v_KeyIndex++)
        {
            DBSQL_Key v_TempKey = this.findKeyStartIndex(v_KeyIndex ,i_FindStartIndex);
            
            if ( v_TempKey != null && v_TempKey.getStartPosition() >= 0 )
            {
                if ( v_RetKey == null || v_TempKey.getStartPosition() < v_RetKey.getStartPosition() )
                {
                    v_RetKey = v_TempKey;
                }
            }
        }
        
        return v_RetKey;
    }
    
    
    
    private DBSQL_Key findKeyStartIndex(int i_KeyIndex ,int i_FindStartIndex)
    {
        Pattern v_Pattern = Pattern.compile(JU_SQLParser.$KEYS_PATTERN.get(i_KeyIndex));
        Matcher v_Matcher = v_Pattern.matcher(this.sql);
        
        if ( v_Matcher.find(i_FindStartIndex) )
        {
            return new DBSQL_Key(v_Matcher.group() ,v_Matcher.start() ,v_Matcher.end());
        }
        else
        {
            return null;
        }
    }
    
    
    
    public String getSQL()
    {
        return sql;
    }
    
    
    
    public int getStartPosition()
    {
        return startPosition;
    }
    
    
    
    public int getEndPosition()
    {
        return endPosition;
    }
    
    
    
    public List<DBSQL_Unit_Segment> getSQLSegments()
    {
        return sqlSegments;
    }
    
    
    
    public String getSQL_Union()
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        for (int i=0; i<this.sqlSegments.size(); i++)
        {
            v_Buffer.append(this.sqlSegments.get(i).getSQLSegment());
        }
        
        return v_Buffer.toString();
    }
    
}





class DBSQL_Unit_Segment
{
    private String  sqlSegment;
    
    private boolean isKey;
    
    private int     startPosition;
    
    private int     endPosition;
    
    
    
    public DBSQL_Unit_Segment(String i_SQLSegment ,boolean i_IsKey ,int i_StartPosition)
    {
        this.sqlSegment    = i_SQLSegment;
        this.isKey         = i_IsKey;
        this.startPosition = i_StartPosition;
        this.endPosition   = i_StartPosition + i_SQLSegment.length() - 1;
    }
    
    
    
    public boolean isKey()
    {
        return isKey;
    }
    
    
    
    public String getSQLSegment()
    {
        return sqlSegment;
    }
    
    
    
    public int getStartPosition()
    {
        return startPosition;
    }
    
    
    
    public int getEndPosition()
    {
        return endPosition;
    }
    
}




class DBSQL_Key
{
    private String key;
    
    private int    startPosition;
    
    private int    endPosition;
    
    
    
    public DBSQL_Key(String i_Key ,int i_StartPosition ,int i_EndPosition)
    {
        this.key           = i_Key;
        this.startPosition = i_StartPosition;
        this.endPosition   = i_EndPosition;
    }
    
    
    
    public String getKey()
    {
        return key;
    }
    
    
    
    public int getStartPosition()
    {
        return startPosition;
    }
    
    
    
    public int getEndPosition()
    {
        return endPosition;
    }
    
}
