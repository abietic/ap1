package com.abietic.ap1.mapper;

import com.abietic.ap1.model.StockLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface StockLogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @Delete({
        "delete from stock_log",
        "where stock_log_id = #{stockLogId,jdbcType=VARCHAR}"
    })
    int deleteByPrimaryKey(String stockLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @Insert({
        "insert into stock_log (stock_log_id, item_id, ",
        "amount, status)",
        "values (#{stockLogId,jdbcType=VARCHAR}, #{itemId,jdbcType=INTEGER}, ",
        "#{amount,jdbcType=INTEGER}, #{status,jdbcType=INTEGER})"
    })
    int insert(StockLog row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @InsertProvider(type=StockLogSqlProvider.class, method="insertSelective")
    int insertSelective(StockLog row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @Select({
        "select",
        "stock_log_id, item_id, amount, status",
        "from stock_log",
        "where stock_log_id = #{stockLogId,jdbcType=VARCHAR}"
    })
    @Results({
        @Result(column="stock_log_id", property="stockLogId", jdbcType=JdbcType.VARCHAR, id=true),
        @Result(column="item_id", property="itemId", jdbcType=JdbcType.INTEGER),
        @Result(column="amount", property="amount", jdbcType=JdbcType.INTEGER),
        @Result(column="status", property="status", jdbcType=JdbcType.INTEGER)
    })
    StockLog selectByPrimaryKey(String stockLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @UpdateProvider(type=StockLogSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(StockLog row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Sun Jul 31 13:16:33 UTC 2022
     */
    @Update({
        "update stock_log",
        "set item_id = #{itemId,jdbcType=INTEGER},",
          "amount = #{amount,jdbcType=INTEGER},",
          "status = #{status,jdbcType=INTEGER}",
        "where stock_log_id = #{stockLogId,jdbcType=VARCHAR}"
    })
    int updateByPrimaryKey(StockLog row);
}