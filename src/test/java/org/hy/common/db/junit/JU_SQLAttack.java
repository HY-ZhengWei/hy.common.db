package org.hy.common.db.junit;

import org.hy.common.Date;
import org.hy.common.db.DBSQLSafe;
import org.hy.common.xml.log.Logger;
import org.junit.Test;






/**
 * 测试单元：SQL注入攻击
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-03-22
 * @version     v1.0
 */
public class JU_SQLAttack
{
    
    private static final Logger $Logger = new Logger(JU_SQLAttack.class ,true);
    
    
    
    /**
     * 性能测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-22
     * @version     v1.0
     */
    @Test
    public void test_001()
    {
        String v_SQL       = "[{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615880,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'HF','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'摩擦焊','processNo':1,'receiveQTY':1,'uID':'1'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','endTaskTime':'2018/03/14 21:36:25','finish':'2018/03/14 21:36:25','giveUpFlag':'0','id':4615881,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'R2','material':'2Cr13','operator':'李东玉','planCode':'1803-2-0567','planQTY':1,'processName':'不锈钢调质','processNo':2,'receiveQTY':1,'uID':'2'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615882,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'03','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'C620','processNo':3,'receiveQTY':1,'uID':'3'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615883,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'T1','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'UT探伤','processNo':4,'receiveQTY':1,'uID':'4'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615884,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'51','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'V16','processNo':5,'receiveQTY':1,'uID':'5'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615885,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'51','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'V16','processNo':6,'receiveQTY':1,'uID':'6'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615886,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'51','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'V16','processNo':7,'receiveQTY':1,'uID':'7'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615887,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'25','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'M131W','processNo':8,'receiveQTY':1,'uID':'8'},{'checkPerson':'谭学娟','checkPersonId':199,'departCode':'0709      ','departName':'调节阀事业部机加车间','drawingsRequired':'316.16,M12-6g,<IMG style=inWIDTH: 18px; HEIGHT: 18px; align: middlein src=inhttp://10.1.50.93:8080/mes/images/drawingMark/D.pngin width=24 height=24>35.74','endFlag':'1','giveUpFlag':'0','id':4615888,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'51','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'V16','processNo':9,'receiveQTY':1,'samplingNum':1,'saveTime':'2018/03/22 16:48:50','sumBadNum':0,'uID':'9','wclqx':'0   ','wclqxy':'0   ','wcltx':'0   ','wkphs':'0   ','wmcrl':'0   ','wxd':'0   ','ygjbwfh':'0   '},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615889,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'85','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'FAD滚压','processNo':10,'receiveQTY':1,'uID':'10'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615890,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'35','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'LC100','processNo':11,'receiveQTY':1,'uID':'11'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615891,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'Q9','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'清洁','processNo':12,'receiveQTY':1,'uID':'12'},{'departCode':'0709      ','departName':'调节阀事业部机加车间','endFlag':'0','giveUpFlag':'0','id':4615892,'itemCode':'1AP23421-2101-1307','itemName':'阀芯','machineID':'QB','material':'2Cr13','planCode':'1803-2-0567','planQTY':1,'processName':'检验','processNo':13,'receiveQTY':1,'uID':'13'}]";
        String v_SQLAttack = ",'testValue = AND 1 = 2";
        
        $Logger.info("-- " + Date.getNowTime().getFullMilli() + "  开始... ...");
        DBSQLSafe.isSafe(v_SQL);
        $Logger.info("-- " + Date.getNowTime().getFullMilli() + "  完成\n\n");
        
        $Logger.info("-- " + Date.getNowTime().getFullMilli() + "  开始... ...");
        DBSQLSafe.isSafe(v_SQL + v_SQLAttack);
        $Logger.info("-- " + Date.getNowTime().getFullMilli() + "  完成\n\n");
    }
    
    
    
    @Test
    public void test_002()
    {
        $Logger.info(DBSQLSafe.isSafe_SQLComment("DELETE FROM TLog_LogWeb WHERE logID = 'ZhengWei'--' AND name = 'HY'"));
        $Logger.info(DBSQLSafe.isSafe_SQLComment("DELETE FROM TLog_LogWeb WHERE logID = '--ZhengWei'--' AND name = 'HY'"));
    }
    
}
