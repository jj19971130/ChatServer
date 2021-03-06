package groupwork.server;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UDPPacketAnalysis {

    /**
     * 将传入的数据进行解析并且返回相应的数据，在调用函数中进行发送，具体格式见Readme.md
     *
     * @param data 接收到的数据
     * @return 准备返回的数据
     * @throws SQLException SQL查询的时候可能抛出异常
     * @throws IOException 实例转换字节数组的时候可能抛出异常
     * @throws ClassNotFoundException jdbc驱动未能成功加载时可能抛出异常
     */
    static byte[] packetAnalysis(byte[] data) throws SQLException, IOException, ClassNotFoundException {
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
        Map map = (Map) objIn.readObject();
        String s = (String) map.get("Type");
        MainService.log.println(new Date() + "[" + Thread.currentThread().getName() + "]:数据包类型为" + s + "，正在返回数据");
        byte[] response = new byte[1];
        synchronized (MainService.db) {
            switch (s) {
                case "SQL_Q": {
                    s = (String) map.get("SQL");
                    MainService.db.setSql(s);
                    List l = MainService.db.querySql();
                    response = Functions.objectToBytes(l);
                }
                break;
                case "Register": {
                    s = "INSERT INTO UserInfo(Username, Password) VALUES (\"" + map.get("Username") + "\",MD5(\"" + map.get("Password") + "\"))";
                    MainService.db.setSql(s);
                    int id = MainService.db.updateSql();
                    response = Functions.intToBytes(id);
                }
                break;
                case "Login": {
                    s = "SELECT * FROM UserInfo WHERE Id = " + map.get("Id") + " AND Password = MD5(\"" + map.get("Password") +"\")";
                    MainService.db.setSql(s);
                    List l = MainService.db.querySql();
                    if (l.size() != 0 && l.get(0) instanceof Map) {
                        String Username = (String) ((Map) l.get(0)).get("Username");
                        response = Username.getBytes();
                    } else {
                        response = "FALSE".getBytes();
                    }
                }
                break;
                case "Send_Message": {
                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    s = "INSERT INTO ChatMessage (Date,`From`,`To`,MessageType,Message,SubMessage) VALUES (\""
                            + date.format(new Date()) + "\",\""
                            + map.get("From") + "\",\""
                            + map.get("To") + "\",\""
                            + "Text" + "\",\""
                            + map.get("Message") + "\",\""
                            + "NULL" + "\""
                            + ")";
                    MainService.db.setSql(s);
                    MainService.db.updateSql();
                    response = ((String) map.get("PacketIdentify")).getBytes();
                }
                break;
                case "User_Info": {
                    s = "SELECT Username FROM UserInfo WHERE Id = " + map.get("Id");
                    MainService.db.setSql(s);
                    List l = MainService.db.querySql();
                    if (l.size() == 0) {
                        String name = "";
                        response = name.getBytes();
                    } else {
                        String name = (String) ((Map) l.get(0)).get("Username");
                        response = name.getBytes();
                    }
                }
                break;
                case "Change_Password": {
                    s = "SELECT * FROM UserInfo WHERE Id = " + map.get("Id") + " AND Password = MD5(\"" + map.get("Password") +"\")";
                    MainService.db.setSql(s);
                    List l = MainService.db.querySql();
                    if (l.size() != 0) {
                        s = "UPDATE UserInfo SET Password = MD5(\"" + map.get("NewPassword") + "\") WHERE Id = " + map.get("Id");
                        MainService.db.setSql(s);
                        MainService.db.updateSql();
                        response = "TRUE".getBytes();
                    } else {
                        response = "FALSE".getBytes();
                    }
                }
                break;
                case "Change_Username": {
                    s = "UPDATE UserInfo SET Username = \"" + map.get("NewUsername") + "\" WHERE Id = " + map.get("Id");
                    MainService.db.setSql(s);
                    MainService.db.updateSql();
                    response = "TRUE".getBytes();
                }
            }
        }
        return response;
    }
}
