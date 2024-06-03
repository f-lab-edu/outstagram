package com.outstagram.outstagram.mapper;


import com.outstagram.outstagram.dto.UserDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    int countByEmail(@Param("email") String email);

    int countByNickname(@Param("nickname") String nickname);

    int insertUser(UserDTO user);

    UserDTO findByEmailAndPassword(@Param("email") String email,
        @Param("password") String password);

    UserDTO findById(Long userId);

    List<UserDTO> findByNicknameContaining(String search);
}
