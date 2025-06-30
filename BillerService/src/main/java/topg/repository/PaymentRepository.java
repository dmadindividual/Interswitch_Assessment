package topg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import topg.model.PaymentTransaction;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction,Long > {
}
