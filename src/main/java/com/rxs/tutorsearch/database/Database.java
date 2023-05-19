package com.rxs.tutorsearch.database;

import com.rxs.tutorsearch.database.models.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
@Component
public class Database {
    private UserRepository userRepository;
    private TutorRepository tutorRepository;
    private CategoryRepository categoryRepository;

    public static List<String> CATEGORIES_NAMES;

    public Database(UserRepository userRepository, TutorRepository tutorRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
        this.categoryRepository = categoryRepository;

        CATEGORIES_NAMES = new ArrayList<>();
        categoryRepository.findAll().forEach(category -> CATEGORIES_NAMES.add(category.getCategoryName()));
    }

    public void deleteTutorFromCategories(long tutorId) {
        categoryRepository.findAll().forEach(category -> {
            String oldTutorIds = Optional.ofNullable(category.getTutorIds()).orElse("");
            if (oldTutorIds.contains(String.valueOf(tutorId))) {
                String newTutorIds = "";
                if (!oldTutorIds.equals(tutorId + ", ")) {
                    newTutorIds = oldTutorIds.substring(0, oldTutorIds.indexOf(", " + tutorId)) +
                            oldTutorIds.substring(oldTutorIds.indexOf(", " + tutorId) + 2 + String.valueOf(tutorId).length());
                }
                category.setTutorIds(newTutorIds);
                categoryRepository.save(category);
            }
        });
    }

    public long getChatIdWithTgName(String tgName) {
        List<User> users = (List<User>) userRepository.findAll();
        for (User user : users) {
            String userTgName = Optional.ofNullable(user.getTgName()).orElse("");
            if (userTgName.equals(tgName)) {
                return user.getUserId();
            }
        }
        return -1;
    }
}
