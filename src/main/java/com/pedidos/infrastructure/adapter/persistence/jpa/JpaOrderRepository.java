package com.pedidos.infrastructure.adapter.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedidos.infrastructure.adapter.persistence.entity.OrderEntity;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {

}
