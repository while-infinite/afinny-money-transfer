package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.Brokerage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BrokerageRepository extends JpaRepository<Brokerage, UUID> {
}