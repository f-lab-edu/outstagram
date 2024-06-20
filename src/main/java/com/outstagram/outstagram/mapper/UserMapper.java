package com.outstagram.outstagram.mapper;


import com.outstagram.outstagram.dto.UserDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {

    int countByEmail(@Param("email") String email);

    int countByNickname(@Param("nickname") String nickname);

    int insertUser(UserDTO user);

    UserDTO findByEmailAndPassword(@Param("email") String email,
        @Param("password") String password);

    UserDTO findById(Long userId);

    List<UserDTO> findByNicknameContaining(String search);

    void editProfile(UserDTO currentUser);
}
