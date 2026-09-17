package com.studentmanagement;

import com.studentmanagement.entity.Student;
import com.studentmanagement.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Scanner;

public class MainApp {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                switch (readChoice()) {
                    case 1 -> addStudent();
                    case 2 -> viewStudent();
                    case 3 -> updateStudent();
                    case 4 -> deleteStudent();
                    case 5 -> running = false;
                    default -> System.out.println("Invalid choice. Please enter a number from 1 to 5.");
                }
            }
            System.out.println("Thank you for using the Student Management System.");
        } finally {
            SCANNER.close();
            JPAUtil.close();
        }
    }

    private static void printMenu() {
        System.out.println("\n====================================");
        System.out.println("       STUDENT MANAGEMENT SYSTEM");
        System.out.println("====================================");
        System.out.println("1. Add Student");
        System.out.println("2. View Student");
        System.out.println("3. Update Student");
        System.out.println("4. Delete Student");
        System.out.println("5. Exit");
    }

    private static int readChoice() {
        System.out.print("Enter your choice: ");
        String input = SCANNER.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static void addStudent() {
        System.out.println("\n--- Add Student ---");
        Student student = new Student(
                readRequired("Name: "),
                readRequired("Email: "),
                readRequired("Course: "),
                readRequired("Phone number: ")
        );

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(student);
            transaction.commit();
            System.out.println("Student added successfully. Student ID: " + student.getStudentId());
        } catch (RuntimeException exception) {
            rollback(transaction);
            System.out.println("Could not add student: " + exception.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private static void viewStudent() {
        Long id = readStudentId();
        if (id == null) return;

        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            Student student = entityManager.find(Student.class, id);
            printStudent(student);
        } finally {
            entityManager.close();
        }
    }

    private static void updateStudent() {
        Long id = readStudentId();
        if (id == null) return;

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Student student = entityManager.find(Student.class, id);
            if (student == null) {
                System.out.println("Student not found.");
                transaction.rollback();
                return;
            }

            student.setCourse(readRequired("New course: "));
            student.setPhoneNumber(readRequired("New phone number: "));
            transaction.commit();
            System.out.println("Student updated successfully.");
        } catch (RuntimeException exception) {
            rollback(transaction);
            System.out.println("Could not update student: " + exception.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private static void deleteStudent() {
        Long id = readStudentId();
        if (id == null) return;

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Student student = entityManager.find(Student.class, id);
            if (student == null) {
                System.out.println("Student not found.");
                transaction.rollback();
                return;
            }

            entityManager.remove(student);
            transaction.commit();
            System.out.println("Student deleted successfully.");
        } catch (RuntimeException exception) {
            rollback(transaction);
            System.out.println("Could not delete student: " + exception.getMessage());
        } finally {
            entityManager.close();
        }
    }

    private static Long readStudentId() {
        System.out.print("Enter Student ID: ");
        try {
            return Long.parseLong(SCANNER.nextLine().trim());
        } catch (NumberFormatException exception) {
            System.out.println("Student ID must be a number.");
            return null;
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field is required.");
        }
    }

    private static void printStudent(Student student) {
        System.out.println(student == null ? "Student not found." : "\n" + student);
    }

    private static void rollback(EntityTransaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
}
