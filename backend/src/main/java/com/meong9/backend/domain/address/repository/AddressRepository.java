package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
