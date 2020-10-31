package com.cvbotunion.cvtwipush.Model;

/**
 * 用于可更新的对象，如{@link User}、{@link Job}、{@link RTGroup}和{@link TwitterUser}
 */
public interface Updatable {

    /**
     * 更新
     * @return 是否成功更新
     */
    boolean update();
}
