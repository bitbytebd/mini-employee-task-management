package com.fifo.employeetask.repository;

import com.fifo.employeetask.entity.Task;
import com.fifo.employeetask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByCreatedByOrAssignee(User createdBy, User assignee);
    List<Task> findByAssignee(User assignee);
    List<Task> findByCreatedBy(User createdBy);
    boolean existsByAssigneeOrCreatedBy(User assignee, User createdBy);
}
