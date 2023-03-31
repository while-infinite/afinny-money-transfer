package by.afinny.moneytransfer.repository;

import by.afinny.moneytransfer.entity.AdditionalParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalParametersRepository extends JpaRepository<AdditionalParameters, Integer> {
}