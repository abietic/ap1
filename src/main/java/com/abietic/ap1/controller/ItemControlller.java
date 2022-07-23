package com.abietic.ap1.controller;

import com.abietic.ap1.controller.BaseController;
import com.abietic.ap1.controller.view.ItemVO;
import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.response.CommonReturnType;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.model.ItemModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author manster
 * @Date 2021/5/24
 **/
@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class ItemControlller extends BaseController {

    @Autowired
    private ItemService itemService;

    @Autowired
    @Qualifier("cacheRedisStringRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    //商品创建
    @PostMapping(value = "/create", consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                               @RequestParam(name = "price") BigDecimal price,
                               @RequestParam(name = "stock")Integer stock,
                               @RequestParam(name = "description")String description,
                               @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);

        ItemVO itemVO = convertFromModel(itemModelForReturn);

        return CommonReturnType.create(itemVO);
    }

    //商品页面浏览
    @GetMapping(value = "/list")
    public CommonReturnType listItem(){

        List<ItemModel> itemModelList = itemService.listItem();

        //使用stream api 将list内的 itemModel 转化为 itemVO
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }



    //商品浏览
    @GetMapping(value = "/getItem")
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){
        // 尝试从redis缓存获取信息
        String stringValue = redisTemplate.opsForValue().get("item_" + id);
        ItemModel itemModel = null;
        if (stringValue != null && !StringUtils.isBlank(stringValue)) {
            try {
                itemModel =  objectMapper.readValue(stringValue, ItemModel.class);
            } catch (JsonMappingException e) {
                // Auto-generated catch block
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
        } 
        if (StringUtils.isBlank(stringValue)) {
            return CommonReturnType.create(null);
        }
        // 如果没有在redis缓存中找到相应的合法内容
        if (itemModel == null){
            itemModel = itemService.getItemById(id);
            stringValue = null;
            if (itemModel != null) {
                try {
                    stringValue = objectMapper.writeValueAsString(itemModel);
                    redisTemplate.opsForValue().set("item_" + id, stringValue, Duration.ofMinutes(10));
                } catch (JsonProcessingException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // 如果不存在相应数据或者是相应内容不合法，为了防止缓存击穿，设置一个空值
            if (stringValue == null) {
                stringValue = "";
                redisTemplate.opsForValue().set("item_" + id, stringValue, Duration.ofMinutes(10));
            }
        }
        ItemVO itemVO = convertFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    // 转换
    private ItemVO convertFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if(itemModel.getPromoModel()!=null){
            //有正在或即将进行的活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            itemVO.setPromoStatus(0);
        }

        return itemVO;
    }

}
