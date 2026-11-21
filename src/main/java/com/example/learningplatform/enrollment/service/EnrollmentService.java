package com.example.learningplatform.enrollment.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.enrollment.dto.EnrollmentResponse;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long studentId) {
        User student = userRepository.getOrThrow(studentId);
        student.requireRole(Role.STUDENT);
        Course course = courseRepository.getOrThrow(courseId);
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ConflictException("Студент уже записан на этот курс");
        }
        Enrollment saved = enrollmentRepository.save(new Enrollment(student, course));
        log.info("Студент id={} записан на курс id={}", studentId, courseId);
        return EnrollmentResponse.from(saved);
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        Enrollment enrollment = findEnrollment(courseId, studentId);
        enrollmentRepository.delete(enrollment);
        log.info("Студент id={} отписан от курса id={}", studentId, courseId);
    }

    @Transactional
    public EnrollmentResponse complete(Long courseId, Long studentId) {
        Enrollment enrollment = findEnrollment(courseId, studentId);
        enrollment.complete();
        return EnrollmentResponse.from(enrollment);
    }

    public List<EnrollmentResponse> findStudentsOfCourse(Long courseId) {
        courseRepository.getOrThrow(courseId);
        return enrollmentRepository.findAllByCourseIdOrderByEnrolledAtAsc(courseId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    public List<EnrollmentResponse> findCoursesOfStudent(Long studentId) {
        userRepository.getOrThrow(studentId);
        return enrollmentRepository.findAllByStudentIdOrderByEnrolledAtDesc(studentId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    private Enrollment findEnrollment(Long courseId, Long studentId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Студент id=%d не записан на курс id=%d", studentId, courseId)));
    }
}
