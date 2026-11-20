package com.iriskopecky.agon.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TerasRepository extends CrudRepository<Teras, Long> {
    List<Teras> findAllByPublicFacing(boolean publicFacing);
}
