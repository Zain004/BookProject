package com.example.versjon2.ExamUserTask.DTO;

import com.example.versjon2.ExamUserTask.Entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String phone;
    private String email;

    public static UsersDTO convertToDTO(Users user) {
        return new UsersDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDob(),
                user.getPhone(),
                user.getEmail()
        );
    }

    public static List<UsersDTO> convertToDtoList(List<Users> users) {
        List<UsersDTO> dtoList = new ArrayList<>(); // bruker arraylist for bedre ytelse ved iterering
        for(Users user : users) {
            dtoList.add(convertToDTO(user)); // Gjenbruk av enkelt-DTO-Konvertering
        }
        return dtoList;
    }
}
