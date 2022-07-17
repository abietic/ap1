package com.abietic.ap1.service.impl;

import com.abietic.ap1.mapper.ItemMapper;
import com.abietic.ap1.mapper.ItemStockMapper;
import com.abietic.ap1.model.Item;
import com.abietic.ap1.model.ItemStock;
import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.PromoService;
import com.abietic.ap1.service.model.ItemModel;
import com.abietic.ap1.service.model.PromoModel;
import com.abietic.ap1.validator.ValidationResult;
import com.abietic.ap1.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private ValidatorImpl validator;

    /**
     * 将商品领域模型转为orm映射对象
     * @param itemModel 领域模型
     * @return 数据对象
     */
    private Item convertItemFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        Item item = new Item();
        BeanUtils.copyProperties(itemModel, item);
        //BeanUtils不会copy不同类型的属性，价格需要我们自己来
        item.setPrice(itemModel.getPrice().doubleValue());

        return item;
    }

    /**
     * 将库存领域模型转为orm映射对象
     * @param itemModel 领域模型
     * @return 库存数据对象
     */
    private ItemStock convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStock itemStock = new ItemStock();
        itemStock.setItemId(itemModel.getId());
        itemStock.setStock(itemModel.getStock());

        return itemStock;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //转化itemmodel -> entity
        Item item = convertItemFromItemModel(itemModel);

        //写入数据库
        System.out.println(item.getId());
        itemMapper.insertSelective(item);
        System.out.println(item.getId());
        itemModel.setId(item.getId());

        ItemStock itemStock = convertItemStockFromItemModel(itemModel);
        itemStockMapper.insertSelective(itemStock);
        //返回创建完成的对象
        return getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<Item> itemDOList = itemMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(item -> {
            ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
            ItemModel itemModel = convertModelFromEntity(item, itemStock);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        Item itemDO = itemMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }
        //操作获得库存
        ItemStock itemStock = itemStockMapper.selectByItemId(itemDO.getId());

        //将entity -> model
        ItemModel itemModel = convertModelFromEntity(itemDO, itemStock);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        //该商品有活动且活动未结束
        if(promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockMapper.decreaseStock(itemId, amount);
        if(affectedRow > 0){
            //更新库存成功
            return true;
        }else {
            //更新库存失败
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemMapper.increaseSales(itemId, amount);
    }

    private ItemModel convertModelFromEntity(Item item, ItemStock itemStock){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item, itemModel);
        itemModel.setPrice(BigDecimal.valueOf(item.getPrice()));
        itemModel.setStock(itemStock.getStock());

        return itemModel;
    }
}
