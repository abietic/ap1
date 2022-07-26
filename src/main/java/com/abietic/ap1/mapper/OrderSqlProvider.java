package com.abietic.ap1.mapper;

import com.abietic.ap1.model.Order;
import org.apache.ibatis.jdbc.SQL;

public class OrderSqlProvider {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    public String insertSelective(Order row) {
        SQL sql = new SQL();
        sql.INSERT_INTO("order_info");
        
        if (row.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=VARCHAR}");
        }
        
        if (row.getUserId() != null) {
            sql.VALUES("user_id", "#{userId,jdbcType=INTEGER}");
        }
        
        if (row.getItemId() != null) {
            sql.VALUES("item_id", "#{itemId,jdbcType=INTEGER}");
        }
        
        if (row.getItemPrice() != null) {
            sql.VALUES("item_price", "#{itemPrice,jdbcType=DOUBLE}");
        }
        
        if (row.getAmount() != null) {
            sql.VALUES("amount", "#{amount,jdbcType=INTEGER}");
        }
        
        if (row.getOrderPrice() != null) {
            sql.VALUES("order_price", "#{orderPrice,jdbcType=DOUBLE}");
        }
        
        if (row.getPromoId() != null) {
            sql.VALUES("promo_id", "#{promoId,jdbcType=INTEGER}");
        }
        
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    public String updateByPrimaryKeySelective(Order row) {
        SQL sql = new SQL();
        sql.UPDATE("order_info");
        
        if (row.getUserId() != null) {
            sql.SET("user_id = #{userId,jdbcType=INTEGER}");
        }
        
        if (row.getItemId() != null) {
            sql.SET("item_id = #{itemId,jdbcType=INTEGER}");
        }
        
        if (row.getItemPrice() != null) {
            sql.SET("item_price = #{itemPrice,jdbcType=DOUBLE}");
        }
        
        if (row.getAmount() != null) {
            sql.SET("amount = #{amount,jdbcType=INTEGER}");
        }
        
        if (row.getOrderPrice() != null) {
            sql.SET("order_price = #{orderPrice,jdbcType=DOUBLE}");
        }
        
        if (row.getPromoId() != null) {
            sql.SET("promo_id = #{promoId,jdbcType=INTEGER}");
        }
        
        sql.WHERE("id = #{id,jdbcType=VARCHAR}");
        
        return sql.toString();
    }
}