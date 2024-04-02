package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address,Long> {
}
