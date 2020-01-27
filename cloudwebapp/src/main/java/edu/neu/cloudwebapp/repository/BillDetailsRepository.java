package edu.neu.cloudwebapp.repository;

import edu.neu.cloudwebapp.model.BillDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillDetailsRepository extends CrudRepository<BillDetails, String> {

    @Query("Select bills from BillDetails bills where bills.owner_id=?1")
    public BillDetails findBillDetailsByOwner_id(String owner_id);

    @Query("Select bills from BillDetails bills where bills.id=?1")
    public BillDetails findBillDetailsById(String id);
}
