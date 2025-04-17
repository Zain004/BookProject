package com.example.versjon2.ExamUserTask.DTO;

import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersDBDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UsersDBDTO convertToDTO(UsersDB user) {
        return new UsersDBDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDob(),
                user.getPhone(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static List<UsersDBDTO> convertToDtoList(List<UsersDB> users) {
        List<UsersDBDTO> dtoList = new ArrayList<>(); // bruker arraylist for bedre ytelse ved iterering
        for(UsersDB user : users) {
            dtoList.add(convertToDTO(user)); // Gjenbruk av enkelt-DTO-Konvertering
        }
        return dtoList;
    }
}
