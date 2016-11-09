package com.wxm.camerajob.base.db;

/**
 * 数据变化提醒接口
 * Created by 123 on 2016/10/31.
 */
public interface IDataChangeNotice {
    /**
     * 数据更新提醒
     */
    void DataModifyNotice();

    /**
     * 数据新建提醒
     */
    void DataCreateNotice();

    /**
     * 数据删除提醒
     */
    void DataDeleteNotice();
}
