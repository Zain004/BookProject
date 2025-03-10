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

    private static UsersDTO convertToDTO(Users user) {
        UsersDTO dto = new UsersDTO();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstName());
        dto.setLastname(user.getLastName());
        dto.setDob(user.getDob());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static List<UsersDTO> convertToDtoList(List<Users> users) {
        List<UsersDTO> dtoList = new ArrayList<>(); // bruker arraylist for bedre ytelse ved iterering
        for(Users user : users) {
            dtoList.add(convertToDTO(user)); // Gjenbruk av enkelt-DTO-Konvertering
        }
        return dtoList;
    }
}
