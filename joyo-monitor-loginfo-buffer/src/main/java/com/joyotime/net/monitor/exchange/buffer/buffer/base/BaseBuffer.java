package com.joyotime.net.monitor.exchange.buffer.buffer.base;

import java.util.ArrayList;
import java.util.List;

import com.joyotime.net.monitor.exchange.buffer.util.redis.RedisUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓冲服务基础类
 * 
 * @author nbin
 * @version $Id: BaseBuffer.java, v 0.1 2019年1月29日 下午5:25:05 nbin Exp $
 */
@Slf4j
@Setter
@Getter
public abstract class BaseBuffer {

    /**缓冲队列名称*/
    public String              bufferName;
    /**缓冲队列长度*/
    public int                 bufferSize       = 100;
    /**sql语句前缀*/
    public String              sqlPrefix;

    private String             bufferPrefixName = "bufferserver_";

    /**
     * 获取消费队列名称
     * @return
     */
    public static final String QueueKeyName     = "bufferserver_ConsumerQueue";

    /**
     * 获取消费队列名称(临时)
     * @return
     */
    public static final String QueueTempKeyName = "bufferserver_ConsumerQueueTemp";

    /**
     * 把消息转换为sql
     *
     * @param msg
     * @return
     */
    public abstract boolean appendSql(Object obj);

    /**
     * 获取设置数据缓冲脚本 ARGV[1] = 需要追加的数据
     *
     * @return
     */
    public String GetAppendScript() {
        return RedisUtil.GetAppendScript(getBufferName(), getSqlPrefix(), QueueKeyName, getBufferSize());
    }

    /**
     * 获取消费队列脚本（非阻塞） 脚本返回2个参数，参数1：实际数据，参数2：数据临时区字段名
     * @return
     */
    public String GetConsumerScript() {
        return RedisUtil.GetConsumerScript(BaseBuffer.QueueKeyName, BaseBuffer.QueueTempKeyName);
    }

    /**
     * 追加数据到缓冲区
     *
     * @param data
     * @return
     */
    public boolean AppendData(String data) {

        List<String> keys = new ArrayList<String>();
        List<String> args = new ArrayList<String>();
        args.add(data);
        String strVal = null;
        try {
            Object val = RedisUtil.ExeRedisScrpit(GetAppendScript(), keys, args);
            strVal = val.toString();
            if (strVal == null) {
                strVal = "";
            }
            log.info(getBufferName() + "数据缓冲成功结果：" + strVal);
        } catch (Exception e) {
            log.error(getBufferName() + "数据缓冲失败：" + e.getMessage(), e);
        }

        return strVal.equals("1") || strVal.equals("2");
    }

    public void setBufferName(String bufferName) {
        this.bufferName = bufferPrefixName + bufferName;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize < 0) {
            bufferSize = 100;
        }
        this.bufferSize = bufferSize;
    }

}
