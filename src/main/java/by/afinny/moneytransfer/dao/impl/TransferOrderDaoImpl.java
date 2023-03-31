package by.afinny.moneytransfer.dao.impl;

import by.afinny.moneytransfer.dao.TransferOrderDao;
import by.afinny.moneytransfer.dto.FilterOptionsDto;
import by.afinny.moneytransfer.entity.TransferOrder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class TransferOrderDaoImpl implements TransferOrderDao {

    EntityManager entityManager;

    public List<TransferOrder> getTransferOrderByFilterOptions(FilterOptionsDto filterOptions) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TransferOrder> criteriaQuery = criteriaBuilder.createQuery(TransferOrder.class);
        Root<TransferOrder> transferOrderRoot = criteriaQuery.from(TransferOrder.class);
        Set<Predicate> predicates = new HashSet<>();
        addPredicatesByFilterOption(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<TransferOrder> query = entityManager.createQuery(criteriaQuery);
        List<TransferOrder> transferOrders = query.getResultList();
        int fromIndex = filterOptions.getPageNumber() * filterOptions.getPageSize();
        int toIndex =  Math.min(fromIndex + filterOptions.getPageSize(), transferOrders.size());
        if (fromIndex < toIndex)
            transferOrders = transferOrders.subList(fromIndex, toIndex);
        else
            transferOrders = new ArrayList<>();
        return transferOrders;
    }

    private void addPredicatesByFilterOption(CriteriaBuilder criteriaBuilder,
                                             Root<TransferOrder> transferOrderRoot,
                                             Set<Predicate> predicates,
                                             FilterOptionsDto filterOptions) {

        addClientId(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        if (!addSum(criteriaBuilder, transferOrderRoot, predicates, filterOptions)) {
            addPurpose(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
            addMinSum(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
            addMaxSum(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        }
        addFrom(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        addTo(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        addRemitterCardNumber(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        addTypeName(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
        addOperationType(criteriaBuilder, transferOrderRoot, predicates, filterOptions);
    }

    private void addClientId(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getClientId() != null) {
            predicates.add(criteriaBuilder.equal(transferOrderRoot.get("clientId"), filterOptions.getClientId()));
        }
    }

    private boolean addSum(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getSum() != null) {
            predicates.add(criteriaBuilder.equal(transferOrderRoot.get("sum"), filterOptions.getSum()));
            return true;
        }
        return false;
    }

    private void addPurpose(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getPurpose() != null) {
            predicates.add(criteriaBuilder.like(transferOrderRoot.get("purpose"), "%" + filterOptions.getPurpose() + "%"));
        }
    }

    private void addMinSum(CriteriaBuilder criteriaBuilder,
                            Root<TransferOrder> transferOrderRoot,
                            Set<Predicate> predicates,
                            FilterOptionsDto filterOptions) {
        if (filterOptions.getMin_sum()   != null) {
            predicates.add(criteriaBuilder.ge(transferOrderRoot.get("sum"), filterOptions.getMin_sum()));
        }
    }

    private void addMaxSum(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getMax_sum() != null) {
            predicates.add(criteriaBuilder.le(transferOrderRoot.get("sum"), filterOptions.getMax_sum()));
        }
    }

    private void addFrom(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getFrom() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(transferOrderRoot.get("createdAt"), filterOptions.getFrom()));
        }
    }

    private void addTo(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getTo() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(transferOrderRoot.get("createdAt"), filterOptions.getTo()));
        }
    }

    private void addRemitterCardNumber(CriteriaBuilder criteriaBuilder,
                           Root<TransferOrder> transferOrderRoot,
                           Set<Predicate> predicates,
                           FilterOptionsDto filterOptions) {
        if (filterOptions.getRemitterCardNumber() != null) {
            predicates.add(criteriaBuilder.equal(transferOrderRoot.get("remitterCardNumber"), filterOptions.getRemitterCardNumber()));
        }
    }

    private void addTypeName(CriteriaBuilder criteriaBuilder,
                             Root<TransferOrder> transferOrderRoot,
                             Set<Predicate> predicates,
                             FilterOptionsDto filterOptions) {
        if (filterOptions.getType_name() != null) {
            predicates.add(criteriaBuilder.equal(
                    transferOrderRoot.get("transferType").get("transferTypeName"), filterOptions.getType_name()));
        }
    }

    private void addOperationType(CriteriaBuilder criteriaBuilder,
                             Root<TransferOrder> transferOrderRoot,
                             Set<Predicate> predicates,
                             FilterOptionsDto filterOptions) {
        if (filterOptions.getOperationType() != null) {
            predicates.add(criteriaBuilder.equal(
                    transferOrderRoot.get("operationType"), filterOptions.getOperationType()));
        }
    }

}
