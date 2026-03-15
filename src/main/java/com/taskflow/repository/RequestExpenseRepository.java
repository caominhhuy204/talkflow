package com.taskflow.repository;

import com.taskflow.entity.RequestExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestExpenseRepository extends JpaRepository<RequestExpense, Long> {
}
