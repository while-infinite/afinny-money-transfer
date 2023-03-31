package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.TemplateForPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateForPaymentRepository extends JpaRepository<TemplateForPayment, Integer> {
}