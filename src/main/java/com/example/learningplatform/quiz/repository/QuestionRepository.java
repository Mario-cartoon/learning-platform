package com.example.learningplatform.quiz.repository;

import com.example.learningplatform.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("select q from Question q left join fetch q.options where q.quiz.id = :quizId order by q.id")
    List<Question> findAllWithOptionsByQuizId(@Param("quizId") Long quizId);
}
