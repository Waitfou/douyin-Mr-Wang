package com.wangguo.service;

import com.wangguo.utils.PagedGridResult;

public interface FansService {

    public void doFollow(String myId, String vlogerId);

    public void doCancel(String myId, String vlogerId);

    public boolean queryDoIFollowVloger(String myId, String vlogerId);

    public boolean queryDoIFollowVloger(String myId);

    public PagedGridResult queryMyFollows(String myId,
                                          Integer page,
                                          Integer pageSize);

    public PagedGridResult queryMyFans(String myId,
                                       Integer page,
                                       Integer pageSize);

}
