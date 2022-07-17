package com.abietic.ap1.mapper;

import com.abietic.ap1.model.Sequence;
import org.apache.ibatis.jdbc.SQL;

public class SequenceSqlProvider {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    public String insertSelective(Sequence row) {
        SQL sql = new SQL();
        sql.INSERT_INTO("sequence_info");
        
        if (row.getName() != null) {
            sql.VALUES("name", "#{name,jdbcType=VARCHAR}");
        }
        
        if (row.getCurrentValue() != null) {
            sql.VALUES("current_value", "#{currentValue,jdbcType=INTEGER}");
        }
        
        if (row.getStep() != null) {
            sql.VALUES("step", "#{step,jdbcType=INTEGER}");
        }
        
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    public String updateByPrimaryKeySelective(Sequence row) {
        SQL sql = new SQL();
        sql.UPDATE("sequence_info");
        
        if (row.getCurrentValue() != null) {
            sql.SET("current_value = #{currentValue,jdbcType=INTEGER}");
        }
        
        if (row.getStep() != null) {
            sql.SET("step = #{step,jdbcType=INTEGER}");
        }
        
        sql.WHERE("name = #{name,jdbcType=VARCHAR}");
        
        return sql.toString();
    }
}