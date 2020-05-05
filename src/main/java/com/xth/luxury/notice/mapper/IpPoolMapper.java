package com.xth.luxury.notice.mapper;

import com.xth.luxury.notice.domain.IpPoolDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;

@Mapper
public interface IpPoolMapper {
    @Insert("INSERT INTO IP_POOL (ip, port) VALUES ( #{ip}, #{port})")
    void insert(IpPoolDO ipPoolDO);

    @Select("SELECT * FROM IP_POOL LIMIT 0,1")
    IpPoolDO getOne();

    @Select("SELECT COUNT(*) FROM IP_POOL")
    Integer getCount();

    @Delete("DELETE FROM IP_POOL WHERE id = #{id}")
    void deleteById(Integer id);
}
