package com.fifo.employeetask.service;

import com.fifo.employeetask.entity.Role;
import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.User;
import com.fifo.employeetask.repository.TaskRepository;
import com.fifo.employeetask.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskReportService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public byte[] createTaskReport(User currentUser) {
        List<Task> tasks = tasksForReport(currentUser);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("FIFO Tech Task Report"));
            document.add(new Paragraph("Tasks: " + tasks.size()));

            PdfPTable table = new PdfPTable(6);
            addHeaders(table);
            for (Task task : tasks) {
                table.addCell(task.getTitle());
                table.addCell(task.getStatus().name());
                table.addCell(task.getPriority().name());
                table.addCell(task.getAssignee() != null ? task.getAssignee().getFullName() : "Unassigned");
                table.addCell(task.getCreatedBy() != null ? task.getCreatedBy().getFullName() : "Unknown");
                table.addCell(task.getDeadline().toString());
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new RuntimeException("Unable to generate task report", exception);
        } catch (java.io.IOException exception) {
            throw new RuntimeException("Unable to generate task report", exception);
        }
    }

    private List<Task> tasksForReport(User currentUser) {
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            return taskRepository.findAll();
        }
        if (!currentUser.getRoles().contains(Role.MANAGER)) {
            throw new AccessDeniedException("You are not allowed to generate task reports");
        }

        Set<Long> teamMemberIds = userRepository.findByManager(currentUser).stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        return taskRepository.findByCreatedBy(currentUser).stream()
                .filter(task -> task.getAssignee() != null && teamMemberIds.contains(task.getAssignee().getId()))
                .toList();
    }

    private void addHeaders(PdfPTable table) {
        table.addCell("Title");
        table.addCell("Status");
        table.addCell("Priority");
        table.addCell("Assignee");
        table.addCell("Assigned By");
        table.addCell("Deadline");
    }
}
