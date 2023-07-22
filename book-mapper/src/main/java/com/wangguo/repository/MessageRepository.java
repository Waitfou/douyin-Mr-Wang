package com.wangguo.repository;

import com.wangguo.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<MessageMO, String> {
    // 通过实现Repository，自定义条件查询
    // 在这里是通过消息的接受方的id查询消息的
    List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(String toUserId,
                                                                 Pageable pageable);

}
