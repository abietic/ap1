package com.abietic.ap1.mapper;

import com.abietic.ap1.model.UserPassword;
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
public interface UserPasswordMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @Delete({
        "delete from user_password",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @Insert({
        "insert into user_password (id, encrpt_password, ",
        "user_id)",
        "values (#{id,jdbcType=INTEGER}, #{encrptPassword,jdbcType=VARCHAR}, ",
        "#{userId,jdbcType=INTEGER})"
    })
    int insert(UserPassword row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @InsertProvider(type=UserPasswordSqlProvider.class, method="insertSelective")
    int insertSelective(UserPassword row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @Select({
        "select",
        "id, encrpt_password, user_id",
        "from user_password",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="encrpt_password", property="encrptPassword", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER)
    })
    UserPassword selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @UpdateProvider(type=UserPasswordSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(UserPassword row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Fri Jul 15 15:39:34 UTC 2022
     */
    @Update({
        "update user_password",
        "set encrpt_password = #{encrptPassword,jdbcType=VARCHAR},",
          "user_id = #{userId,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(UserPassword row);

    // 这些是手写的，暂时不知道如何自动生成
    @Select({
        "select",
        "id, encrpt_password, user_id",
        "from user_password",
        "where user_id = #{userId,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="encrpt_password", property="encrptPassword", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER)
    })
    UserPassword selectByUserId(Integer userId);
}