package com.knightboost.preload;

/**
 * 预加载接口
 */
public interface PreloadDemander {
    /**
     * 配置所有需要预加载的类
     * @return
     */
    Class[] getPreloadClasses();
}
