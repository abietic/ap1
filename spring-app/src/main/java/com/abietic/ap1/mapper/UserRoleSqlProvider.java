package com.abietic.ap1.mapper;

import com.abietic.ap1.model.UserRole;
import org.apache.ibatis.jdbc.SQL;

public class UserRoleSqlProvider {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_role
     *
     * @mbg.generated Sun Sep 18 12:34:13 UTC 2022
     */
    public String insertSelective(UserRole row) {
        SQL sql = new SQL();
        sql.INSERT_INTO("user_role");
        
        if (row.getId() != null) {
            sql.VALUES("id", "#{id,jdbcType=INTEGER}");
        }
        
        if (row.getUserId() != null) {
            sql.VALUES("user_id", "#{userId,jdbcType=INTEGER}");
        }
        
        if (row.getRole() != null) {
            sql.VALUES("role", "#{role,jdbcType=VARCHAR}");
        }
        
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_role
     *
     * @mbg.generated Sun Sep 18 12:34:13 UTC 2022
     */
    public String updateByPrimaryKeySelective(UserRole row) {
        SQL sql = new SQL();
        sql.UPDATE("user_role");
        
        if (row.getUserId() != null) {
            sql.SET("user_id = #{userId,jdbcType=INTEGER}");
        }
        
        if (row.getRole() != null) {
            sql.SET("role = #{role,jdbcType=VARCHAR}");
        }
        
        sql.WHERE("id = #{id,jdbcType=INTEGER}");
        
        return sql.toString();
    }
}