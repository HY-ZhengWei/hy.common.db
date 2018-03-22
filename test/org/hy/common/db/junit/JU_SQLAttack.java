package org.hy.common.db.junit;

import org.hy.common.Date;
import org.hy.common.db.DBSQLSafe;
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
        String v_SQL       = "[{'conclusion':'合格','docName':'16100391-wgjc-9818.JPG','giveUpFlag':'0','id':267,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:10:19','materialNo':'SR300FHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-3322.JPG','giveUpFlag':'0','id':267,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:10:19','materialNo':'SR300FHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-4999.JPG','giveUpFlag':'0','id':266,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:09:27','materialNo':'AB8CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-6950.JPG','giveUpFlag':'0','id':266,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:09:27','materialNo':'AB8CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-5908.JPG','giveUpFlag':'0','id':266,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:09:27','materialNo':'AB8CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-4409.JPG','giveUpFlag':'0','id':265,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:08:10','materialNo':'AB40CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-1301.JPG','giveUpFlag':'0','id':265,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:08:10','materialNo':'AB40CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16100391-wgjc-3039.JPG','giveUpFlag':'0','id':265,'lastEditPerson':'朱丽','lastEditTime':'2016/10/28 10:08:10','materialNo':'AB40CSCFHZ','orderNumber':'16100391','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060068-wgjc-1119.png','giveUpFlag':'0','id':264,'lastEditPerson':'超级管理员','lastEditTime':'2016/07/11 20:10:00','materialNo':'14R27437-201-0r4D-14T490-JX','orderNumber':'16060068','testItem':'sss','testPercent':'11%','testValue':'ss'},{'conclusion':'合格','giveUpFlag':'0','id':263,'lastEditPerson':'超级管理员','lastEditTime':'2016/07/11 20:08:40','materialNo':'执行机构','orderNumber':'16060067','testItem':'qqq','testPercent':'11%','testValue':'1'},{'conclusion':'合格','giveUpFlag':'0','id':262,'lastEditPerson':'超级管理员','lastEditTime':'2016/07/11 20:08:20','materialNo':'执行机构','orderNumber':'16060067','testItem':'111','testPercent':'1%','testValue':'1'},{'conclusion':'合格','giveUpFlag':'1','giveUpPerson':'admin','giveUpTime':'2016/07/11 20:10:15','id':261,'lastEditPerson':'超级管理员','lastEditTime':'2016/07/11 20:03:56','materialNo':'阀体组件','orderNumber':'16060066','testItem':'qqq','testPercent':'11%','testValue':'1111'},{'conclusion':'合格','docName':'16060066-wgjc-9441.JPG','giveUpFlag':'1','giveUpPerson':'admin','giveUpTime':'2016/07/11 20:08:05','id':260,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:34:37','materialNo':'阀体组件','orderNumber':'16060066','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060066-wgjc-8106.JPG','giveUpFlag':'1','giveUpPerson':'admin','giveUpTime':'2016/07/11 20:08:05','id':260,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:34:37','materialNo':'阀体组件','orderNumber':'16060066','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060242-wgjc-4595.JPG','giveUpFlag':'0','id':259,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:32:52','materialNo':'阀体组件','orderNumber':'16060242','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060190-wgjc-9570.JPG','giveUpFlag':'0','id':258,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:30:30','materialNo':'阀体组件','orderNumber':'16060190','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060190-wgjc-5326.JPG','giveUpFlag':'0','id':258,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:30:30','materialNo':'阀体组件','orderNumber':'16060190','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060190-wgjc-2124.JPG','giveUpFlag':'0','id':258,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:30:30','materialNo':'阀体组件','orderNumber':'16060190','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-1148.JPG','giveUpFlag':'0','id':257,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:24:30','materialNo':'Q41Y/PN25/DN25/RF/CF8','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-3181.JPG','giveUpFlag':'0','id':257,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:24:30','materialNo':'Q41Y/PN25/DN25/RF/CF8','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-7239.JPG','giveUpFlag':'0','id':256,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:23:04','materialNo':'Z41Y/PN25/DN15/RF/WCB','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-3600.JPG','giveUpFlag':'0','id':256,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:23:04','materialNo':'Z41Y/PN25/DN15/RF/WCB','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-3109.JPG','giveUpFlag':'0','id':255,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:22:03','materialNo':'H41Y/PN25/DN25/RF/CF8','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-5311.JPG','giveUpFlag':'0','id':255,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:22:03','materialNo':'H41Y/PN25/DN25/RF/CF8','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-2710.JPG','giveUpFlag':'0','id':254,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:21:00','materialNo':'GL41Y/PN25/DN15/RF/A105','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-8358.JPG','giveUpFlag':'0','id':254,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:21:00','materialNo':'GL41Y/PN25/DN15/RF/A105','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-2276.JPG','giveUpFlag':'0','id':253,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:19:56','materialNo':'CS41H/PN25/DN15/RF/WCB','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16051107-wgjc-3971.JPG','giveUpFlag':'0','id':253,'lastEditPerson':'朱丽','lastEditTime':'2016/06/17 10:19:56','materialNo':'CS41H/PN25/DN15/RF/WCB','orderNumber':'16051107','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060100-wgjc-4098.JPG','giveUpFlag':'0','id':252,'lastEditPerson':'朱丽','lastEditTime':'2016/06/08 18:04:58','materialNo':'阀体组件','orderNumber':'16060100','testItem':'外观','testPercent':'100%','testValue':''},{'conclusion':'合格','docName':'16060159-wgjc-7436.JPG','giveUpFlag':'0','id':251,'lastEditPerson':'朱丽','lastEditTime':'2016/06/08 18:03:40','materialNo':'阀体组件','orderNumber':'16060159','testItem':'外观','testPercent':'100%','testValue':''}]";
        String v_SQLAttack = ",'testValue = AND 1 = 2";
        
        System.out.println("-- " + Date.getNowTime().getFullMilli() + "  开始... ...");
        DBSQLSafe.isSafe(v_SQL);
        System.out.println("-- " + Date.getNowTime().getFullMilli() + "  完成\n\n");
        
        System.out.println("-- " + Date.getNowTime().getFullMilli() + "  开始... ...");
        DBSQLSafe.isSafe(v_SQL + v_SQLAttack);
        System.out.println("-- " + Date.getNowTime().getFullMilli() + "  完成\n\n");
    }
    
}
