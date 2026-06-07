package com.campus.backend.mapper;

import com.campus.backend.entity.UserBehavior;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserBehaviorMapper {

    @Insert("INSERT INTO user_behaviors (user_id, behavior_type, target_type, target_id) VALUES (#{userId}, #{behaviorType}, #{targetType}, #{targetId})")
    int insert(UserBehavior behavior);

    @Select("SELECT target_id FROM (SELECT target_id, MAX(created_at) as max_ts FROM user_behaviors WHERE user_id = #{userId} AND target_type = #{targetType} GROUP BY target_id ORDER BY max_ts DESC LIMIT #{limit}) t")
    List<Long> selectRecentTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("limit") int limit);
}
