package com.abietic.ap1.service;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.service.model.ItemModel;

import java.util.List;


public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount);

    // 库存回补
    boolean increaseStock(Integer itemId, Integer amount);

    // 异步扣减库存
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    //销量增加
    void increaseSales(Integer itemId, Integer amount);

    // item 和 promo model 的缓存模型
    ItemModel getItemByIdInCache(Integer id);

    // 初始化库存流水,记录本次事务的进行状态
    // 1.代表初始化, 2.代表库存扣减事务成功, 3.代表事务回滚
    String initStockLog(Integer itemId, Integer amount);

}
