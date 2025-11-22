//package com.exam.console;
//
//import com.exam.dao.TaskDAO;
//import com.exam.model.Task;
//
//import java.util.List;
//import java.util.Scanner;
//
//public class TaskConsoleApp {
//
//    public static void main(String[] args) {
//        TaskDAO dao = new TaskDAO();
//        Scanner sc = new Scanner(System.in);
//
//        while (true) {
//            System.out.println("\n===== TASK MANAGER =====");
//            System.out.println("1. Xem danh sách");
//            System.out.println("2. Thêm task");
//            System.out.println("3. Sửa task");
//            System.out.println("4. Xóa task");
//            System.out.println("0. Thoát");
//            System.out.print("Chọn: ");
//
//            int choice = sc.nextInt();
//            sc.nextLine();
//
//            if (choice == 0) break;
//
//            switch (choice) {
//                case 1 -> {
//                    List<Task> list = dao.getAll();
//                    System.out.println("\n--- TASK LIST ---");
//                    list.forEach(System.out::println);
//                }
//                case 2 -> {
//                    System.out.print("Tiêu đề: ");
//                    String title = sc.nextLine();
//
//                    System.out.print("Mô tả: ");
//                    String desc = sc.nextLine();
//
//                    System.out.print("Ưu tiên: ");
//                    int pr = sc.nextInt();
//
//                    dao.insert(new Task(title, desc, pr));
//                    System.out.println("✔ Thêm thành công!");
//                }
//                case 3 -> {
//                    System.out.print("Nhập ID task cần sửa: ");
//                    int id = sc.nextInt(); sc.nextLine();
//
//                    System.out.print("Tiêu đề mới: ");
//                    String title = sc.nextLine();
//
//                    System.out.print("Mô tả mới: ");
//                    String desc = sc.nextLine();
//
//                    System.out.print("Ưu tiên mới: ");
//                    int pr = sc.nextInt();
//
//                    dao.update(new Task(id, title, desc, pr));
//                    System.out.println("✔ Sửa thành công!");
//                }
//                case 4 -> {
//                    System.out.print("Nhập ID task cần xóa: ");
//                    int id = sc.nextInt();
//                    dao.delete(id);
//                    System.out.println("✔ Xóa thành công!");
//                }
//            }
//        }
//
//        sc.close();
//    }
//}
