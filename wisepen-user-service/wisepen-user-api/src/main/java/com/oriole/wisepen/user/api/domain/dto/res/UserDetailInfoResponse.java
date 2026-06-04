package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.UserInfoBase;
import com.oriole.wisepen.user.api.domain.base.UserProfileBase;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class UserDetailInfoResponse implements Serializable {
    /** 当前用户 ID，与 userInfo 平级返回，便于前端识别登录用户 */
    private Long userId;

    UserInfoBase userInfo;
    UserProfileBase userProfile;
    List<String> readonlyFields;
}